package com.mapkc.nsfw.handler;

import com.mapkc.nsfw.model.Fragment;
import com.mapkc.nsfw.model.RenderContext;
import io.netty.handler.codec.http.HttpHeaders;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by chy on 14-8-9.
 */
public class FragmentRPCHandler extends PageRPCHandler {
    @Override
    protected Fragment getFragment(RenderContext rc) throws MalformedURLException {
        String path = rc.param("fk-rpc-pagepath");
        if (path == null) {

            String url = rc.getHeader(HttpHeaders.Names.REFERER);
            java.net.URL u = new URL(url);
            path = u.getPath();
        }

        Fragment reqh = rc.getSite().getFragment(path);
        if (!(reqh instanceof Fragment)) {
            throw new RuntimeException("Cannot find fragment:" + path);
        }


        return reqh;

    }
}
