package com.mapkc.nsfw.valid;

import com.mapkc.nsfw.model.Site;
import com.mapkc.nsfw.util.DynamicClassLoader;
import com.mapkc.nsfw.util.KVParser;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 格式：<br/>
 * <p>
 * validname<atrname=values;anotherattrname=value;>
 *
 * @author chy
 */
public class ValidatorParser {

    static Map<String, ValidatorCreator> creators = new ConcurrentHashMap<String, ValidatorCreator>();

    public static Validator get(final String type, Map<String, String> m,
                                Site site) {
        ValidatorCreator vc = creators.get(type);
        if (vc == null) {
            vc = new ValidatorCreator() {

                @Override
                public Validator create(Map<String, String> as, Site site) {
                    Object o;
                    try {
                        o = DynamicClassLoader.load(type, site);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                    if (o instanceof Validator) {
                        return (Validator) o;
                    }
                    return null;
                }
            };
        }
        Validator v = vc.create(m, site);
        return v;

    }

    public static Validator parse(String vs, Site site) {
        Map<String, String> m = new java.util.HashMap<String, String>();
        String type = KVParser.parser(vs, m);

        return get(type, m, site);
    }

}
