package com.mapkc.nsfw.handler;

import com.mapkc.nsfw.model.RenderContext;

public class RpcTest extends JSRpcHandler {

    public RpcTest() {
    }

    @JsRPCMethod
    public int add(int a, int b) {
        return a + b;
    }

    @JsRPCMethod
    public RpcTest add2(int a, int b) {
        return this;
    }

    @JsRPCMethod
    public String add3(int a, String b, RenderContext rc) {

        return a + "  " + b + rc.getPath() + "/中午 \\+@#$##%$^%^%&\'\" ";
    }

}
