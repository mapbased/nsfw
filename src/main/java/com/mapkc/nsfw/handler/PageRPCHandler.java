package com.mapkc.nsfw.handler;

import com.mapkc.nsfw.component.Code;
import com.mapkc.nsfw.component.Component;
import com.mapkc.nsfw.util.logging.ESLogger;
import com.mapkc.nsfw.util.logging.Loggers;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mapkc.nsfw.model.*;
import io.netty.handler.codec.http.HttpHeaders;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 * 将调用转到相应的Page或者Fragment的BaseRPCActionHandler上面。 <br/>
 * 请求的鉴权需要各个method自己独立来做
 *
 * @author chy
 */
public class PageRPCHandler extends XEnum implements ReqHandler {

    final static ESLogger log = Loggers.getLogger(PageRPCHandler.class);
    JsonFactory jsf = new JsonFactory();
    ObjectMapper objMapper = new ObjectMapper(jsf);

    protected void preCheck(RenderContext rc) {

    }

    protected Fragment getFragment(RenderContext rc) throws MalformedURLException {

        String path = rc.param("fk-rpc-pagepath");
        if (path == null) {

            String url = rc.getHeader(HttpHeaders.Names.REFERER);
            java.net.URL u = new URL(url);
            path = u.getPath();
        }

        ReqHandler reqh = rc.getSite().getReqHandlerWithPathFix(path, rc);
        if (!(reqh instanceof Fragment)) {
            throw new RuntimeException("Cannot find fragment:" + path);
        }

        Fragment f = (Fragment) reqh;
        return f;


    }

    public static void filterAccess(MethodBag bag, RenderContext rc) {
        int uid = rc.getUserIdAsInt();
        if (uid == 0 && bag.getAcceseMode() != AccessMode.Public) {
            throw new RuntimeException("您尚未登录，请登录");
        }
        if ((!rc.can(bag.privilege))||( bag.getAcceseMode() == AccessMode.Admin && !rc.isAdmin())) {
            throw new RuntimeException("您没有权限操作");
        }
    }


    @Override
    public void handle(RenderContext rc) {


        String fn = rc.param("fk-rpc-fn");
        String pvs = rc.param("fk-rpc-pvs");


        JSRpcResp resp = new JSRpcResp();
        try {
            this.preCheck(rc);


            Fragment f = this.getFragment(rc);
            rc.setCurrentFragment(f);
            rc.setPage(f.getPage());
            ActionHandler h = f.getHandler();
            Object o = null;
            boolean ok = false;

            if (h instanceof BaseRPCActionHandler) {


                BaseRPCActionHandler bh = (BaseRPCActionHandler) h;
                MethodBag m = bh.getMehthodBag(fn);
                if (m != null) {
                    filterAccess(m, rc);

                    Class[] paraClasses = m.getMethod().getParameterTypes();

                    Object[] ps = new Object[paraClasses.length];

                    JsonParser jp = jsf.createParser(pvs);

                    // int i=0;
                    jp.nextToken();

                    for (int i = 0; i < ps.length; i++) {
                        Class c = paraClasses[i];
                        if (c == RenderContext.class) {
                            ps[i] = rc;
                        } else {
                            jp.nextToken();
                            ps[i] = this.objMapper.readValue(jp, c);
                        }
                    }
                    o = m.method.invoke(bh, ps);
                    ok = true;
                }
            }
            if (!ok) {

                Component com = f.getComponent(fn);
                if (com instanceof Code) {
                    Code code = (Code) com;
                    Object[] ps = this.objMapper.readValue(pvs, Object[].class);
                    rc.setVar("rpcparam", ps);
                    o = code.call(rc);
                } else if (com != null) {


                    Map<String, Object> ps = this.objMapper.readValue(pvs, Map.class);
                    rc.extractParams(rc.getReferer());
//                    ps.forEach((k, v) -> {
//                        if (v != null)
//                            rc.setParam(k, v.toString());
//                    });
                    for (Map.Entry<String, Object> e : ps.entrySet()) {
                        rc.setParam(e.getKey(), e.getValue().toString());
                    }

                    f.doActions(rc);
                    o = com.getRenderValue(rc);
                } else
                    throw new RuntimeException(
                            "Cannot locate code with name:".concat(fn));
            }
            resp.setOk(true);
            resp.setResult(o);


        } catch (java.lang.reflect.InvocationTargetException e) {
            resp.setOk(false);
            if (e.getCause() instanceof ApiBaseHandler.RespException) {
                resp.setErrorMsg(((ApiBaseHandler.RespException) e.getCause()).apiResp.msg);
            } else {

                log.error("Error while RPC", e);
                resp.setErrorMsg(e.getCause().getLocalizedMessage());
            }


        } catch (Exception e) {

            log.error("Error while RPC", e);
            resp.setOk(false);
            resp.setErrorMsg(e.getLocalizedMessage());
        }

        try {
            String s = this.objMapper.writeValueAsString(resp);
            rc.sendResponse(s);
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            rc.sendResponse("{\"ok\":false}");
        }

    }


}
