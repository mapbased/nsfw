package com.mapkc.nsfw.site;

import com.mapkc.nsfw.model.RenderContext;
import io.netty.handler.codec.http.HttpHeaders;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by chy on 15/5/6.
 */
public class ModifyManager {

    Executor pool = Executors.newFixedThreadPool(1);
    ConcurrentHashMap<String, List<RenderContext>> listeners = new ConcurrentHashMap<>();

    public void modified(final String path) {

        pool.execute(new Runnable() {
            @Override
            public void run() {
                List<RenderContext> a = listeners.remove(path);
                if (a != null) {

//                a.forEach((rc) -> {
//                    rc.setContentType("application/javascript");
//                    rc.getResp().headers().set(HttpHeaders.Names.CACHE_CONTROL, "no-cache, must-revalidate");
//
//                    rc.sendResponse("location.reload()");
//                });

                    for (RenderContext rc : a) {
                        rc.setContentType("application/javascript");
                        rc.setHeader(HttpHeaders.Names.CACHE_CONTROL, "no-cache, must-revalidate");

                        rc.sendResponse("location.reload()");
                    }
                }
            }
        });
    }

    public void listen(String path, RenderContext rc) {
        List<RenderContext> renderContextList = new LinkedList<>();
        renderContextList.add(rc);
        List<RenderContext> ret = this.listeners.putIfAbsent(path, renderContextList);
        if (ret != null) {
            synchronized (ret) {
                ret.add(rc);
            }
        }
        // this.listeners.getTarget(path);
    }

}
