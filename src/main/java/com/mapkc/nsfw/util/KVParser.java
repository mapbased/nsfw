package com.mapkc.nsfw.util;

import java.util.HashMap;
import java.util.Map;

/**
 * 解析类似下面格式的内容：<br>
 * <p>
 * itemname&lt;attribute1=value1;attribute2=value2;attrinuten=valuen;&gt;
 *
 * @author chy
 * TODO 简单实现，以后优化，特别要加入字符转译
 */
public class KVParser {

    public static void main(String[] ss) {
        Map<String, String> m = new HashMap();
        parser("typeahead<path=/handler/rpcmisc?xtype=schemafield>", m);
        System.out.println(m);
    }

    public static String parser(String ins, Map<String, String> attributes) {

        int si = ins.indexOf('<');
        if (si > 0) {
            String ret = ins.substring(0, si);
            String as = ins.substring(si + 1, ins.length() - 1);
            String[] ass = as.split(";");
            for (String s : ass) {
                //String[] ss = s.split("=");
                //attributes.put(ss[0].trim(), ss[1].trim());
                int qidx = s.indexOf('=');
                if (qidx > 0) {
                    String k = s.substring(0, qidx).trim();
                    String v = s.substring(qidx + 1).trim();
                    attributes.put(k, v);
                } else {
                    throw new RuntimeException("Cannot parse attribute :" + ins);
                }

            }

            return ret;

        } else {
            return ins.trim();
        }
    }
}
