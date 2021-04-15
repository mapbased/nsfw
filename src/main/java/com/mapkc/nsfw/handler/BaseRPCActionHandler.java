package com.mapkc.nsfw.handler;

import com.mapkc.nsfw.model.*;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 带有rpc功能的ActionHandler，大部分需要ajax功能的页面，都应该继承这个类
 *
 * @author chy
 */
public class BaseRPCActionHandler implements ActionHandler {

    @Unsafe
    public static final Map<String, MethodBag> getMethods(RenderContext renderContext, String id) {
        XEnum x = renderContext.getSite().getXEnum(id);
        Map<String, MethodBag> methods = new HashMap<>();
        if (x instanceof Fragment) {
            Fragment f = (Fragment) x;
            ActionHandler actionHandler = f.getHandler();
            if (actionHandler instanceof BaseRPCActionHandler) {
                methods.putAll(((BaseRPCActionHandler) actionHandler).methods);
            } else {
                //TODO add code rpc
            }
        }

        return methods;
    }

    @Override
    public boolean filterAction(RenderContext rc) {

        return false;
    }

    // TODO 不是很节省，应该每个类一个，所有实例共享
    protected Map<String, MethodBag> methods = new HashMap<String, MethodBag>(
            10);


    final public Object [] getRpcMethods(){
        return this.methods.values().stream().sorted().toArray();
    }


    public BaseRPCActionHandler() {
        this.jsonpInit();
    }

    final public MethodBag getMehthodBag(String methodName) {

        return this.methods.get(methodName);

    }

    protected void jsonpInit() {
        Method[] ms = this.getClass().getMethods();
        for (Method m : ms) {
            JsRPCMethod jrpcm = m.getAnnotation(JsRPCMethod.class);
            if (jrpcm != null) {
                MethodBag mb = new MethodBag(m);
                mb.acceseMode = jrpcm.access();
                mb.privilege= jrpcm.privilege();

                this.methods.put(m.getName(), mb);

            }
        }

    }

}
