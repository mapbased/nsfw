package com.mapkc.nsfw.util;

import com.google.common.base.Splitter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by chy on 14-7-10.
 */
public class TagSplitter {

    static public java.util.List<String> split(String id) {


        if (id == null) {
            return Collections.EMPTY_LIST;
        }
        return Splitter.on(' ').trimResults().omitEmptyStrings().splitToList(id);

    }

    /**
     * Merge 多个tag
     *
     * @param ss：每个都是用空格隔开的多个tag
     * @return
     */
    public static java.util.Collection<String> merge(String... ss) {

        Map<String, Object> map = new HashMap<>();
        for (String s : ss) {
            if (s == null || s.length() == 0) continue;

            for (String t : split(s)) {
                map.put(t, t);
            }
//            split(s).forEach((t) -> {
//                map.put(t, t);
//            });
        }
        return map.keySet();
    }

    /**
     * Merge 多个tag
     *
     * @param ss 每个多是多个tag组成的数组
     * @return
     */
    public static java.util.Collection<String> merge(String[]... ss) {
        Map<String, Object> map = new HashMap<>();
        for (String[] s : ss) {
            if (s == null || s.length == 0) continue;
            for (String string : s) {
                map.put(string, string);
            }
        }
        return map.keySet();
    }
}
