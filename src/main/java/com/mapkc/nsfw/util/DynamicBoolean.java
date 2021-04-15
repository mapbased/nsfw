package com.mapkc.nsfw.util;

import com.mapkc.nsfw.binding.Binding;
import com.mapkc.nsfw.model.LoadContext;
import com.mapkc.nsfw.model.RenderContext;


/**
 * Created by chy on 14-7-30.
 */
public class DynamicBoolean {
    int value = -1;
    Binding b;

    public DynamicBoolean(String s) {
        if (s != null) {
            String ts = s.trim().toLowerCase();
            if (ts.equals("true")) {
                value = 1;
            } else if (ts.equals("false")) {
                value = -1;
            } else if (ts.startsWith("@")) {
                b = LoadContext.getBinding(s);
                value = 0;
            } else {
                throw new RuntimeException("Cannot parse :" + s);
            }

        }
    }

    @Override
    public String toString() {
        return value > 0 ? "true" : value < 0 ? "false" : String.valueOf(b);
    }

    public boolean get(RenderContext rc) {
        return value > 0 || value >= 0 && b != null && b.getBooleanRenderValue(rc);
    }
}
