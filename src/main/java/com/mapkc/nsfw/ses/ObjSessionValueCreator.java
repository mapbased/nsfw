package com.mapkc.nsfw.ses;

import com.mapkc.nsfw.model.RenderContext;

import java.lang.reflect.Method;

/**
 * Created by chy on 14-7-16.
 */
public class ObjSessionValueCreator implements SessionValueCreator {
    @Override
    public Object initSessionValue(String key, RenderContext rc) {
        try {
            Method m = this.getClass().getDeclaredMethod(key, RenderContext.class);
            m.setAccessible(true);
            return m.invoke(this, rc);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
