/**
 *
 */
package com.mapkc.nsfw.util;

import com.mapkc.nsfw.model.Site;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author chy
 */
public class EasyMap extends TreeMap<String, Object> implements
        Map<String, Object> {

    public EasyMap() {
        super();
    }

    public EasyMap(String k, Object v) {
        super();
        this.put(k, v);
    }

    public static final EasyMap make(String k, Object v) {
        return new EasyMap(k, v);
    }

    public static final EasyMap make() {
        return new EasyMap();
    }

    static public EasyMap copy(EasyMap map) {
        EasyMap e = new EasyMap();
        e.putAll(map);
        return e;
    }

    static public EasyMap copy(Map<String, String> map) {
        EasyMap e = new EasyMap();
        e.putAll(map);
        return e;
    }

    public static Map<String, String> toStrMap(Map<String, Object> im) {
        Map<String, String> m = new java.util.HashMap<String, String>(
                (int) (1.25 * im.size()));

        for (Map.Entry<String, Object> e : im.entrySet()) {
            String s = null;
            Object o = e.getValue();
            if (o != null) {
                s = String.valueOf(o);
            }

            m.put(e.getKey(), s);
        }
        return m;
    }

    public <T> void toObject(T t, Site s) {

        try {
            AsConvert.toObject(this, t, s);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

    }

    public Map<String, String> toStrMap() {

        return toStrMap(this);
    }

    public HashMap<String, Object> toHashMap() {
        HashMap<String, Object> m = new java.util.HashMap<>(
                (int) (1.25 * this.size()));

        for (Map.Entry<String, Object> e : this.entrySet()) {


            m.put(e.getKey(), e.getValue());
        }
        return m;
    }

    public EasyMap add(String k, Object v) {
        this.put(k, v);
        return this;
    }

    public EasyMap add(String k, Object v, boolean doadd) {
        if (doadd) {
            this.put(k, v);
        }
        return this;
    }


}