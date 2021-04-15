package com.mapkc.nsfw.vl;

import com.mapkc.nsfw.model.RenderContext;

public interface ValueList {

    String getScreenNameByValue(String value, RenderContext rc);

    Object getSrcByValue(String value, RenderContext rc);

    java.util.Iterator<Value> iterator(RenderContext rc);

    @Override
    String toString();

}
