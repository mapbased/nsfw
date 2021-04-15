package com.mapkc.nsfw.handler;

import com.mapkc.nsfw.FKNames;
import com.mapkc.nsfw.model.FakeRenderContext;
import com.mapkc.nsfw.model.Page;
import com.mapkc.nsfw.model.RenderContext;
import com.mapkc.nsfw.model.XEnum;
import com.mapkc.nsfw.util.VolatileBag;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;

import java.util.Map;

/**
 * 字段测试所有页面
 *
 * @author chy
 */
public class GetTest extends BaseRPCActionHandler {

    public String result(RenderContext rc) {


        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, VolatileBag<XEnum>> e : rc.getSite().getEnums()
                .entrySet()) {
            XEnum x = e.getValue() == null ? null : e.getValue().getValue();
            if (x instanceof Page) {
                Page p = (Page) x;
                if (p == rc.getPage()) {
                    continue;
                }

                String uri = e.getKey();
                if (p.getTestParam() != null) {
                    uri += "?" + p.getTestParam();
                }


                FakeRenderContext fr = new FakeRenderContext(rc.getSite(),
                        new DefaultFullHttpRequest(HttpVersion.HTTP_1_1,
                                HttpMethod.GET, uri));
                fr.setCookie(rc);


                sb.append(fr.req.getUri()).append(" \t");
                try {
                    p.handle(fr);
                    if (fr.getVar(FKNames.FK_EXCEPTION) == null) {
                        sb.append("ok");
                    } else {
                        sb.append(fr.getVar(FKNames.FK_EXCEPTION));
                    }
                } catch (Exception ee) {
                    sb.append(ee.toString());
                }
                sb.append("\t").append(fr.getResp().status().code())
                        .append("\t");
                sb.append("\n");

            }

        }

        return sb.toString();
    }

}
