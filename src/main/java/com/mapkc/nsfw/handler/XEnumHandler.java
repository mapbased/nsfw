package com.mapkc.nsfw.handler;

import com.mapkc.nsfw.model.RenderContext;
import com.mapkc.nsfw.model.ReqHandler;
import com.mapkc.nsfw.model.XEnum;
import com.mapkc.nsfw.util.Strings;

import java.util.Collections;
import java.util.List;

/**
 * @author chy
 * @deprecated:应该使用RPC的方法来做，这个是一开始的做法
 */
public class XEnumHandler extends XEnum implements ReqHandler {


    @Override
    public void handle(RenderContext rc) {

        String id = rc.param("id");
        if (id == null) {
            id = "/";
        }
        XEnum xe = rc.getSite().getXEnum(id);
        List<XEnum> es = Collections.EMPTY_LIST;
        if (xe != null) {
            es = xe.getChildren();
            Collections.sort(es);
        }

        // rc.setDateHeader(name, date)("Content-Type", "application/json");
        rc.getResp()
                .headers().set ("Content-Type", "application/json;charset=UTF-8");

        StringBuilder sb = new StringBuilder();
        sb.append("[");
        // String[] fz = new String[] { "id", "screenName", "hasChild" };
        for (XEnum x : es) {
            // sb.append(x.toJson(fz)).append(",");
            sb.append("{\"id\":\"").append(x.getId()).append("\",\"text\":");
            Strings.quoteJson(sb, x.getName());
            sb.append(",");
            Strings.quoteJson(sb, "state");
            sb.append(":");
            Strings.quoteJson(sb, x.getItems().size() > 0 ? "closed" : "open");
            sb.append("},");

        }
        if (sb.charAt(sb.length() - 1) == ',') {
            sb.setLength(sb.length() - 1);
        }
        sb.append("]");
        rc.response(sb.toString());

    }

}
