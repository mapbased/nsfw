package com.mapkc.nsfw.handler;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.mapkc.nsfw.binding.TypeTranslator;
import com.mapkc.nsfw.input.ParamFormModel;
import com.mapkc.nsfw.model.FormModel;
import com.mapkc.nsfw.model.ParamField;
import com.mapkc.nsfw.model.RenderContext;
import com.mapkc.nsfw.model.Site;
import com.mapkc.nsfw.util.json.DateTimeJsonSerializer;
import com.mapkc.nsfw.util.logging.ESLogger;
import com.mapkc.nsfw.util.logging.Loggers;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.util.concurrent.Future;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;

/**
 * Created by chy on 15/8/4.
 */
public class ApiBaseHandler extends BaseRPCActionHandler {

    final static ESLogger log = Loggers.getLogger(ApiBaseHandler.class);
    JsonFactory jsf = new JsonFactory();
    ObjectMapper objMapper = new ObjectMapper(jsf);

    {
        SimpleModule module = new SimpleModule();
        module.addSerializer(java.sql.Timestamp.class, new DateTimeJsonSerializer());
        objMapper.registerModule(module);

    }

    @Override
    public boolean filterAction(RenderContext rc) {


        String m = rc.param("fk_params");
        if (m != null && m.endsWith(".json")) {
            //   StringHelper.filterEmoji(rc);
            rc.getResp().headers().set(HttpHeaders.Names.CONTENT_TYPE, "application/json;charset=UTF-8");
            m = m.substring(0, m.length() - 5);
            ApiResp resp = new ApiResp();

            MethodBag bag = this.getMehthodBag(m);
            if (bag == null) {
                rc.sendNotFound();
                return true;
            }
            //  String[] ps = bag.getParaNames();


            // Object[] os = rc.paramMulti(bag.getParaNames());
            if (rc.getUserIdAsInt() <= 0 && bag.getAcceseMode() != AccessMode.Public) {
                resp.setMsg("未登录");
                resp.setCode(2003);
            } else if ((!rc.can(bag.privilege)) || (bag.getAcceseMode() == AccessMode.Admin && !rc.isAdmin())) {
                resp.setMsg("权限不足");
                resp.setCode(2004);
            } else {


                try {


                    Parameter[] ps = bag.getMethod().getParameters();
                    String[] paramnames = bag.getParaNames();
                    Class[] cs = bag.getMethod().getParameterTypes();
                    Object[] os = new Object[ps.length];
                    Site site = rc.getSite();

                    for (int i = 0; i < ps.length; i++) {

                        Parameter parameter = ps[i];
                        if (parameter.getAnnotation(ParamField.class) != null) {

                            ParamFormModel paramFormModel = parameter.getType().getAnnotation(ParamFormModel.class);
                            if (paramFormModel != null) {
                                FormModel formModel = (FormModel) rc.getSite().getXEnum(paramFormModel.value());
                                if (formModel != null) {
                                    os[i] = formModel.valueFromReq(rc, parameter.getType());
                                    continue;
                                }
                            }

                            os[i] = rc.paramObj(parameter.getType());
                            continue;

                        }


                        if (cs[i] == RenderContext.class) {
                            os[i] = rc;
                            continue;
                        }
                        TypeTranslator typeTranslator = TypeTranslator.get(cs[i]);
                        if (typeTranslator == null) {
                            throw new RuntimeException("Cannot translate type:" + paramnames[i]);
                        }
                        os[i] = typeTranslator.translate(rc.p(paramnames[i]), site);
                    }
                    if (rc.hasError()) {
                        resp.setCode(1000);
                        resp.setData(rc.getErrorMap());
                        resp.setMsg("错误:" + rc.getErrorMap().values().toArray()[0]);
                        // return resp;
                    } else {


                        Object ret = bag.getMethod().invoke(this, os);
                        if (ret instanceof SkippedApiResp) {
                            return true;
                        } else if (ret instanceof ApiResp) {
                            resp = (ApiResp) ret;
                        } else if (ret instanceof Future) {
                            Future future = (Future) ret;

                            future.addListener((f) -> {
                                if (f.isSuccess()) {
                                    Object o = f.getNow();
                                    if (o instanceof ApiResp) {
                                        writeResp((ApiResp) o, rc);
                                    } else {

                                        ApiResp apiResp = new ApiResp();
                                        apiResp.setData(o);
                                        writeResp(apiResp, rc);
                                    }

                                } else {
                                    ApiResp apiResp = new ApiResp(1000, f.cause().getMessage());
                                    writeResp(apiResp, rc);
                                    log.error("Api Error:{}", f.cause(), rc.getGuid());
                                }
                            });


                            return true;


                        } else {
                            resp.setData(ret);
                        }
                    }
                } catch (RespException re) {
                    resp = re.apiResp;
                } catch (InvocationTargetException invocationTargetException) {
                    Throwable t = invocationTargetException.getCause();
                    if (t instanceof RespException) {
                        resp = ((RespException) t).apiResp;
                    } else {
                        resp.setCode(1000);
                        resp.setMsg("错误:" + invocationTargetException.getCause().getMessage()
                        );
                        log.error("Api Error:{}", invocationTargetException, m);
                    }
                } catch (Exception e) {
                    resp.setCode(1000);
                    resp.setMsg("服务器接口错误:" + e.getMessage());
                    log.error("Api Error:{}", e, m);
                    // e.printStackTrace();
                }
            }

            writeResp(resp, rc);

            return true;

        }
        return super.filterAction(rc);
    }

    private void writeResp(ApiResp resp, RenderContext rc) {
        String s = null;
        try {

            if (rc.isDevelopeMode()) {


                s = this.objMapper.writerWithDefaultPrettyPrinter().writeValueAsString(resp);
            } else {
                s = this.objMapper.writeValueAsString(resp);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        rc.sendResponse(s);
    }

    static public class RespException extends RuntimeException {
        public ApiResp apiResp;

        public RespException(ApiResp apiResp) {
            this.apiResp = apiResp;
        }

        public RespException(String errorMsg) {
            super(errorMsg);
            this.apiResp = new ApiResp(1000, errorMsg);

        }

        public RespException(String errorMsg, int errorCode) {
            super(errorMsg);
            this.apiResp = new ApiResp(errorCode, errorMsg);
        }


        public RespException(String errorMsg, int errorCode, Object data) {
            super(errorMsg);
            this.apiResp = new ApiResp(errorCode, errorMsg);
            this.apiResp.data = data;
        }




    }

    static public class ApiResp<T> {

        int code;
        String msg; //返回给用户的友好信息
        String debugMsg;
        T data;
        //String sessionId;

        public ApiResp() {

        }



        public ApiResp(int code, String msg) {
            this.code = code;
            this.msg = msg;
            //this.sessionId = sessionId;
        }

        public String getDebugMsg() {
            return debugMsg;
        }

        public void setDebugMsg(String debugMsg) {
            this.debugMsg = debugMsg;
        }


        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public T getData() {
            return data;
        }

        public void setData(T data) {
            this.data = data;
        }


    }

    public static class SkippedApiResp extends ApiResp {

        public static final SkippedApiResp INSTANCE = new SkippedApiResp();
    }
}
