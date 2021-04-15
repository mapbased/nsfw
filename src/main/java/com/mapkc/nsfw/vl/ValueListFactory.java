package com.mapkc.nsfw.vl;

import com.mapkc.nsfw.model.*;
import com.mapkc.nsfw.util.DynamicClassLoader;
import com.mapkc.nsfw.util.StringPair;
import com.mapkc.nsfw.util.logging.ESLogger;
import com.mapkc.nsfw.util.logging.Loggers;

import java.util.ArrayList;
import java.util.List;

/*
 值列表， <br/>
 * 1.可以方便的便利所有的值 <br/>
 * 2.可以快速的由值获取对应的显示名称
 * 
 * 
 * 
 * 可以方便的从一个字符串构造<br/>
 * 
 * 可能的格式包括 <br/>
 * 1.[screennamefile:valuefield]@{binding} -->从计算的列表中通过给定的字段名获取值和名称<br/>
 * 2.[]{namevalue1,namevalue2,namevaluen} -->从给的值列表中获取
 * 
 * 3.[]{name1:value1,name2:value2,namen:valuen} -->从给定的值列表获取<br/>
 * 
 * 4.[screennamefile:valuefield]/XEnum/Path -->从X枚举路径获取<br/>
 * 5.[namefieldname:valuefieldname]schema:/path/to/schema:sql
 * 6.[screennamefile:valuefield]com.mapbased.nwfs.EnumClass -->从枚举类获取<br/>
 */
public class ValueListFactory {

    final static ESLogger log = Loggers.getLogger(ValueListFactory.class);

    /**
     * 关于缓存：bindingValueList缓存会有问题，因为bingding本身就不好缓存。
     * 另外一个就是动态加载的类，缓存也可能有问题
     * 其他类型的Valuelist应该缓存。
     *
     * @param vs
     * @param site
     * @return
     */

    public static ValueList from(String vs, Site site) {

        try {
            ValueList vl = fromInner(vs, site);
            if (vl instanceof AbstractValueList) {
                ((AbstractValueList) vl).src = vs;
            }
            return vl;
        } catch (Exception e) {
            log.error("Error while load VL:{}", e, vs);
            return null;

        }
    }

    private static ValueList fromInner(String vs, Site site) {
        if (vs == null) {
            return null;
        }

        vs = vs.trim();
        final String srcvl = vs;
        if (vs.length() == 0) {
            return null;
        }
        //  log.info("ValueList:{}", vs);
        String fmap = null;
        if (vs.startsWith("[")) {
            int i = vs.indexOf(']');
            if (i < 0) {
                throw new java.lang.RuntimeException("Cannot parse value list:"
                        + vs);
            }
            fmap = vs.substring(1, i);
            vs = vs.substring(i + 1);

        }
        String nameExp = null;
        String valueExp = null;
        if (fmap != null) {
            String[] ss = fmap.split("\\:");
            nameExp = ss[0];
            valueExp = ss[1];
        }

        if (vs.startsWith("@")) {


            return new BindingValueList(nameExp, valueExp,
                    LoadContext.getBinding(vs));

        } else if (vs.startsWith("{")) {
            vs = vs.substring(1, vs.length() - 1);
            String[] ss = vs.split(",");
            List<StringPair> sps = new ArrayList<StringPair>(ss.length);
            for (String s : ss) {
                String n = s;
                String v = s;
                if (s.indexOf(':') > 0) {
                    String[] nvs = s.split("\\:");
                    n = nvs[0];
                    v = nvs[1];
                }
                StringPair sp = new StringPair(n, v);
                sps.add(sp);

            }
            return new FixedValueList(sps);
        } else if (vs.startsWith("/")) {
            XEnum xEnum = site == null ? null : site.getXEnum(vs);
            if (xEnum instanceof ValueListConf) {
                return new ValueListConf.PathValueList(vs, site);
            }


            return new XEnumValueList(vs, nameExp, valueExp);


        } else if (vs.startsWith("schema:") || vs.startsWith("sql:")) {
            // vs.substring(beginIndex, endIndex)
            int sqlidx = vs.lastIndexOf(':');
            if (sqlidx < 4) {
                throw new java.lang.IllegalStateException("ValueList:" + vs);
            }
            SchemaValueList svl = site.valueListCache.get(srcvl);
            if (svl != null) {
                return svl;
            }
            int headindex = vs.indexOf(":");
            String schemaname = vs.substring(headindex + 1, sqlidx);
            String sql = vs.substring(sqlidx + 1);

            // TODO

            Renderable r = LoadContext.getRenderable(sql);


            svl = vs.startsWith("schema:") ? new SchemaValueList(nameExp,
                    valueExp, site.getXEnumBagCreateIfEmpty(schemaname), r)
                    : new SqlValueList(nameExp, valueExp,
                    site.getXEnumBagCreateIfEmpty(schemaname), r);
            SchemaValueList back = site.valueListCache.putIfAbsent(srcvl, svl);
            if (back != null) {
                return back;
            }

            return svl;

        } else {

            Class c;
            try {
                c = DynamicClassLoader.loadClass(vs, site);
            } catch (ClassNotFoundException e1) {

                throw new RuntimeException(e1);
            }
            if (c == null) {

                log.warn("Cannot load object from : " + vs);
                return null;
            }
            if (c.isEnum()) {
                // if (nameExp == null) {
                // nameExp = "toString()";
                // }
                // if (valueExp == null) {
                // valueExp = "name()";
                // }

                return new EnumValueList(nameExp, valueExp, c);
            }

            Object o;
            try {
                o = c.newInstance();
            } catch (Exception e) {
                log.error("Cannot create object from :{}", c, e);
                return null;
            }

            if (o instanceof ValueList) {
                return ((ValueList) o);
            }

            return null;

        }


    }

    // public static void main(String ss[]) {
    //
    // Object o = DynamicClassLoader
    // .load(com.dianziq.nsfw.input.FormInputType.class.getName());
    // System.out.println(o);
    // SiteManager siteManager = new SiteManager();
    // System.out.println(from("[ss:dd]@{_p.cok}" ));
    // System.out.println(from("[ss:dd]@{_p.cok}"));
    //
    // }

}
