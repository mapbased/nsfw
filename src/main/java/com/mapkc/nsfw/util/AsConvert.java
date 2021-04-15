package com.mapkc.nsfw.util;

import com.mapkc.nsfw.binding.TypeTranslator;
import com.mapkc.nsfw.model.Site;

import java.lang.reflect.Field;
import java.util.Map;

public class AsConvert {

    /**
     * TODO :缓存Class，优化性能
     *
     * @param vs
     * @param t
     * @param site
     * @param <T>
     * @throws IllegalAccessException
     */
    public static <T> void toObject(Map<String, Object> vs, T t, Site site) throws IllegalAccessException {


        Field[] fz = t.getClass().getDeclaredFields();
        for (Field f : fz) {
            Object o = vs.get(f.getName());
            f.setAccessible(true);
            if (o != null) {
                if (o instanceof String) {

                    TypeTranslator tt = TypeTranslator.get(f.getType());
                    if (tt != null) {
                        o = tt.translate((String) o, site);
                        f.set(t, o);
                        continue;
                    }
                }


                f.set(t, o);


            }
        }

    }

    public static boolean asBool(Object o, boolean defaultvalue) {
        if (o instanceof Boolean) {
            return (Boolean) o;
        }
        if (o == null) {
            return defaultvalue;
        }
        return Boolean.parseBoolean(o.toString());
    }

    public static int asInt(Object o, int defaultvalue) {
        if (o instanceof java.lang.Number) {
            return ((java.lang.Number) o).intValue();
        }
        if (o == null) {
            return defaultvalue;
        }
        String s = o.toString();
        try {
            return Integer.parseInt(s);
        } catch (java.lang.NumberFormatException e) {
            return defaultvalue;
        }
    }

    public static long asLong(Object o, long defaultvalue) {
        if (o instanceof java.lang.Number) {
            return ((java.lang.Number) o).longValue();
        }
        if (o == null) {
            return defaultvalue;
        }
        String s = o.toString();
        try {
            return Long.parseLong(s);
        } catch (java.lang.NumberFormatException e) {
            return defaultvalue;
        }
    }

    public static void main(String[] ss) {

    }

}
