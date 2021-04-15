package com.mapkc.nsfw.component;

import com.mapkc.nsfw.binding.Binding;
import com.mapkc.nsfw.model.*;
import com.mapkc.nsfw.util.DynamicClassLoader;
import com.mapkc.nsfw.util.VolatileBag;
import com.mapkc.nsfw.util.logging.ESLogger;
import com.mapkc.nsfw.util.logging.Loggers;

/**
 * 获取固定、动态或者从一个类中构造
 *
 * @param <T>
 * @author chy
 */
public abstract class FieldBag<T> {

    final static ESLogger log = Loggers.getLogger(FieldBag.class);

    public abstract T get(RenderContext rc);

    public static <T> FieldBag<T> from(String s, Class<T> c, LoadContext lc) {

        // LoadContext clc = lc.createChild();
        // clc.parseBinding(s);
        Renderable r = lc.getRenderableStr(s);// clc.getRenderable();
        if (r instanceof StrRender) {
            StrRender sr = (StrRender) r;
            String v = sr.value;
            // TODO add a method getXEnumBagCreateIfEmpty()?
            String rv = lc.getPath(v); // 计算相对路径
            VolatileBag<XEnum> xb = lc.site.getXEnumBag(rv);
            if (xb != null && xb.getValue() != null) {
                VBag<T> vb = new VBag<T>();
                vb.vb = xb;
                return vb;

            }
            // for buildin formmodels TODO
            // remove this

            Object fm = lc.site.getXEnum(v);
            if (fm == null && v.indexOf('.') > 0) {
                String fmname = "freemodel:";
                if (v.startsWith(fmname)) {

                    Class fc;
                    try {
                        fc = DynamicClassLoader.loadClass(
                                v.substring(fmname.length()), lc.site);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                    if (fc != null)
                        fm = FormModel.fromFreeClass(fc, lc.site);

                } else
                    try {
                        fm = DynamicClassLoader.load(v, lc.site);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException("Error while loading:" + lc.getLoadingFragment().getPath(), e);
                    }

                // fm.getClass().isAssignableFrom(T);
            }
            if (fm != null) {


                CBag<T> cb = new CBag<T>();
                cb.t = (T) fm;
                return cb;

            }
            // if(xo!=null)
        } else if (r instanceof Binding) {
            BBag<T> d = new BBag<T>();
            d.r = (Binding) r;
            return d;
        } else if (r instanceof RenderGroup) {
            GBag<T> d = new GBag<>();
            d.r = r;
            return d;
        } else {
            DBag<T> d = new DBag<T>();
            d.r = r;
            return d;
        }

        // log.error("Cannot load:{}", s);
        // return null;
        throw new RuntimeException("Cannot load:" + s);
    }

    static class VBag<T> extends FieldBag<T> {
        VolatileBag<XEnum> vb;

        @Override
        public T get(RenderContext rc) {
            return (T) vb.getValue();
        }

    }

    static class CBag<T> extends FieldBag<T> {
        T t;

        @Override
        public T get(RenderContext rc) {
            return t;
        }

    }

    /**
     * TODO 不是很完善，需要整理哪些Bag可用
     *
     * @param <T>
     * @author chy
     */
    static class BBag<T> extends FieldBag<T> {
        Binding r;

        @Override
        public T get(RenderContext rc) {
            Object v = r.getValue(rc);
            if (v instanceof XEnum) {
                return (T) v;
            }
            return (T) rc.getSite().getXEnum(String.valueOf(v));


        }
    }

    static class GBag<T> extends FieldBag<T> {
        Renderable r;

        @Override
        public T get(RenderContext rc) {
            String v = r.getRenderValue(rc);

            return (T) rc.getSite().getXEnum(v);


        }
    }

    static class DBag<T> extends FieldBag<T> {
        Renderable r;

        @Override
        public T get(RenderContext rc) {

            String path = r.getRenderValue(rc);
            return (T) rc.getSite().getXEnum(path);
        }

    }

}
