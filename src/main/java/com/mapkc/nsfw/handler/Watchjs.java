package com.mapkc.nsfw.handler;

import com.mapkc.nsfw.model.ActionHandler;
import com.mapkc.nsfw.model.RenderContext;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.net.URL;

/**
 * Created by chy on 15/5/6.
 */
public class Watchjs implements ActionHandler {
    @Override
    public boolean filterAction(RenderContext rc) {
        String ref = rc.getReferer();
        try {
            java.net.URL url = new URL(ref);


            rc.getSite().getModifyManager().listen(url.getPath(), rc);
        } catch (Exception e) {

            rc.sendError(HttpResponseStatus.BAD_REQUEST, e.getMessage());

        }
        return true;
    }
}
