package com.mapkc.nsfw.handler;

import com.mapkc.nsfw.model.RenderContext;
import com.mapkc.nsfw.model.ReqHandler;
import com.mapkc.nsfw.model.XEnum;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * 指定一个路径，这个路径下可以相应N个jsrpc的调用。<br />
 * 继承该类，添加@JsRPCMethod的方法即可。<br/>
 * 注意：每个方法内部独立鉴权。
 *
 * @author chy
 */
public class JSRpcHandler extends XEnum implements ReqHandler {

    private Map<String, MethodBag> methods = new java.util.HashMap<String, MethodBag>();
    JsonFactory jsf = new JsonFactory();
    ObjectMapper objMapper = new ObjectMapper(jsf);

    public JSRpcHandler() {
        this.jsonpInit();
    }

    @Override
    public void handle(RenderContext rc) {

        String fn = rc.param("fk-rpc-fn");
        String pvs = rc.param("fk-rpc-pvs");


        MethodBag m = this.methods.get(fn);
        JSRpcResp resp = new JSRpcResp();

        try {
            if (m == null) {

                throw new java.lang.RuntimeException("Cannot find method:" + fn);

            }
            PageRPCHandler.filterAccess(m, rc);
            Class<?>[]  paraClasses= m.getMethod().getParameterTypes();
            Object[] ps = new Object[paraClasses.length];


            JsonParser jp = jsf.createParser(pvs);
            //int i=0;
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
            Object o = m.method.invoke(this, ps);
            resp.setOk(true);
            resp.setResult(o);


        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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


        // rc.getTarget
    }

    protected void jsonpInit() {
        Method[] ms = this.getClass().getMethods();
        for (Method m : ms) {
            JsRPCMethod jrpcm = m.getAnnotation(JsRPCMethod.class);
            if (jrpcm != null) {
                MethodBag mb = new MethodBag(m);
                mb.acceseMode = jrpcm.access();
                mb.privilege=jrpcm.privilege();


                this.methods.put(m.getName(), mb);
            }
        }

    }

}
