package com.mapkc.nsfw.model;

import com.mapkc.nsfw.input.FormField;
import com.mapkc.nsfw.util.StringPair;
import com.mapkc.nsfw.util.VolatileBag;
import com.mapkc.nsfw.vl.Value;
import com.mapkc.nsfw.vl.ValueList;

import java.util.*;

public class ValueListConf extends XEnum implements ValueList {

    @FormField(caption = "值列表", required = true, msg = "值<空格>显示名的形式配置多个值列表", input = "textarea<autosize=autosize-transition>")
    String vls;
    List<StringPair> stringPairList;
    Map<String, String> map = new HashMap();

    @Override
    protected String defaultIcon() {
        return "fa fa-th";
    }

    @Override
    protected void init(Site site) {
        super.init(site);

        stringPairList = new ArrayList<>();

        String[] ss = vls.split("\n");
        for (String s : ss) {
            String ts = s.trim();
            if (ts.length() == 0) {
                continue;
            }
            int i = ts.indexOf(' ');
            if (i < 0) {

                if (!map.containsKey(ts)) {
                    stringPairList.add(new StringPair(ts, ts));
                    map.put(ts, ts);
                }
                continue;
            }
            String value = ts.substring(0, i).trim();
            String name = ts.substring(i + 1).trim();
            StringPair stringPair = new StringPair(name, value);
            if (!map.containsKey(value)) {
                map.put(value, name);
                stringPairList.add(stringPair);
            }
        }
    }

    @Override

    public String getScreenNameByValue(String value, RenderContext rc) {

        return map.get(value);
    }

    @Override
    public Object getSrcByValue(String value, RenderContext rc) {
        return null;
    }

    @Override
    public Iterator<Value> iterator(RenderContext rc) {
        return new Iterator<Value>() {
            int i = 0;

            @Override
            public boolean hasNext() {
                return i < stringPairList.size();
            }

            @Override
            public Value next() {

                StringPair stringPair = stringPairList.get(i);
                i += 1;
                return new Value() {
                    @Override
                    public String getScreenName() {
                        return stringPair.name;
                    }

                    @Override
                    public String getValue() {
                        return stringPair.value;
                    }

                    @Override
                    public Object getSrc() {
                        return null;
                    }
                };
            }
        };
    }

    public static class PathValueList implements ValueList {
        String path;
        Site site;
        VolatileBag<ValueListConf> volatileBag;

        public PathValueList(String path, Site site) {
            this.path = path;
            this.site = site;
            volatileBag = (VolatileBag) site.getXEnumBag(path);
        }

        @Override
        public String getScreenNameByValue(String value, RenderContext rc) {
            return volatileBag.value.getScreenNameByValue(value, rc);
        }

        @Override
        public Object getSrcByValue(String value, RenderContext rc) {
            return null;
        }

        @Override
        public String toString() {
            return path;
        }

        @Override
        public Iterator<Value> iterator(RenderContext rc) {
            return volatileBag.value.iterator(rc);
        }
    }
}
