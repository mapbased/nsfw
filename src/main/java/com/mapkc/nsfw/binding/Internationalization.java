package com.mapkc.nsfw.binding;

import com.mapkc.nsfw.model.RenderContext;
import com.mapkc.nsfw.model.Site;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chy on 14-7-17.
 * <p>
 * 设想中的用法为：
 *
 * @{_l.screenName.msg('DefaultValue',"position")}
 * @{_l.screenName.msg('DefaultValue')}
 */
public class Internationalization extends ValueGetter {
    private int index = -1;

    @Override
    Object get(RenderContext rc, String key) {

        Local local = rc.getLocal();
        if(local==null){
            return  null;
        }

        if (index < 0) {
            //	index = local.index(key)

        }
        return local.msg(index);


    }

    public static class StringInt {
        public String str;
        public final int idx;

        public StringInt(String str, int idx) {
            this.str = str;
            this.idx = idx;
        }

    }

    public static class Locals {
        final Site site;

        Map<String, Integer> map = new HashMap<>(1024);
        List<String> defaultMsgs = new ArrayList<>(1024);


        public final synchronized int index(String name, String defaultValue) {
            Integer si = map.get(name);
            if (si == null) {
                int i = defaultMsgs.size();
                defaultMsgs.add(defaultValue);
                map.put(name, i);
                return i;
            }
            return si;
        }

        public Locals(Site site) {
            this.site = site;
        }


    }

    public static class Local {
        final Locals locals;
        public List<String> msgs;

        public Local(Locals locals) {
            this.locals = locals;
            this.msgs = new ArrayList<>(this.locals.defaultMsgs.size());
        }

        int index(String name, String defaultMsg) {
            return this.locals.index(name, defaultMsg);

        }

        public String msg(int index) {
            return null;
        }

    }
}
