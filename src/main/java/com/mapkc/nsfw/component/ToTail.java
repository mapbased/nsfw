package com.mapkc.nsfw.component;

import com.mapkc.nsfw.model.RenderContext;

public class ToTail extends ToHead {
    @Override
    protected void addContent(RenderContext rc, String key, String value) {
        rc.addTail(key, value);


    }
}
