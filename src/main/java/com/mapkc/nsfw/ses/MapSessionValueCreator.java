package com.mapkc.nsfw.ses;

import com.mapkc.nsfw.binding.Binding;
import com.mapkc.nsfw.model.LoadContext;
import com.mapkc.nsfw.model.RenderContext;
import com.mapkc.nsfw.util.logging.ESLogger;
import com.mapkc.nsfw.util.logging.Loggers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by chy on 14-7-16.
 */
public class MapSessionValueCreator implements SessionValueCreator {

    ESLogger log = Loggers.getLogger(MapSessionValueCreator.class);
    Map<String, Binding> map = new ConcurrentHashMap();

    public void add(String key, String exp) {

        map.put(key, LoadContext.getBinding(exp));
    }

    @Override
    public Object initSessionValue(String key, RenderContext rc) {
        Binding b = map.get(key);
        if (b == null) {
            log.warn("Cannot find session key:{} for create", key);
            return null;
        }
        return b.getValue(rc);


    }
}
