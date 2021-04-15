package com.mapkc.nsfw.component;

import com.mapkc.nsfw.binding.Binding;
import com.mapkc.nsfw.model.LoadContext;
import com.mapkc.nsfw.model.RenderGroup;
import com.mapkc.nsfw.model.Renderable;
import com.mapkc.nsfw.util.Strings;
import com.mapkc.nsfw.vl.ValueList;
import com.mapkc.nsfw.vl.ValueListFactory;
import org.jsoup.nodes.Element;

/**
 * 控件的属性支持设计时自动生成表单
 *
 * @param <T>
 * @author Administrator
 */
public abstract class Property<T> {
    final String name;
    final String caption;

    public Property(String name) {
        this.name = name;
        this.caption = name;
    }

    public Property(String name, String caption) {
        this.name = name;
        this.caption = caption;
    }

    protected abstract T parse(String val, LoadContext lc);

    protected String format(T v) {
        return String.valueOf(v);
    }

    // public final void parseReq(RenderContext rc, Component com) {
    // String v = rc.req.param(name);
    // if (v != null) {
    // com.setProperty(name, this.parse(v));
    // }
    // }

    final public void parseXml(Element ele, LoadContext lc, Component com) {
        String s = lc.fetchAttribute(ele, name);
        if (s != null) {
            com.setProperty(name, this.parse(s, lc));
        }
    }

    @SuppressWarnings("unchecked")
    final public void toXml(StringBuilder xc, Component com) {
        T property = (T) com.getProperty(name);
        if (property != null) {
            xc.append(this.name).append("\"")
                    .append(Strings.toXmlAttributeSafe(format(property)))
                    .append("\"");
        }
    }

    public static class Str extends Property<String> {

        public Str(String name, String caption) {
            super(name, caption);

        }

        @Override
        protected String parse(String val, LoadContext lc) {

            return val;
        }

    }

    public static class Long extends Property<java.lang.Long> {

        public Long(String name, String caption) {
            super(name, caption);

        }

        @Override
        protected java.lang.Long parse(String val, LoadContext lc) {
            return java.lang.Long.parseLong(val);
        }

    }

    public static class Int extends Property<Integer> {

        public Int(String name, String caption) {
            super(name, caption);

        }

        @Override
        protected Integer parse(String val, LoadContext lc) {

            return Integer.parseInt(val);
        }

    }

    public static class Bool extends Property<Boolean> {

        public Bool(String name, String caption) {
            super(name, caption);
        }

        @Override
        protected Boolean parse(String val, LoadContext lc) {

            return Boolean.parseBoolean(val);

        }

    }

    public static class BindingPro extends Property<Binding> {

        public BindingPro(String name, String caption) {
            super(name, caption);
            // TODO Auto-generated constructor stub
        }

        @Override
        protected Binding parse(String val, LoadContext lc) {
            LoadContext clc = lc.createChild();
            clc.parseBinding(val);
            return clc.getRenderGroup().getFirstBinding();

        }

    }

    public static class ValueListPro extends Property<ValueList> {
        public ValueListPro(String name) {
            super(name);

        }

        public ValueListPro(String name, String caption) {
            super(name, caption);

        }

        @Override
        protected ValueList parse(String val, LoadContext lc) {
            return ValueListFactory.from(val, lc.site);
        }

    }


    @Deprecated
    // use RenderablePro
    public static class RenderGroupPro extends Property<RenderGroup> {

        public RenderGroupPro(String name) {
            super(name);

        }

        public RenderGroupPro(String name, String cap) {
            super(name, cap);

        }

        @Override
        protected RenderGroup parse(String val, LoadContext lc) {
            LoadContext c = lc.createChild();
            c.parseBinding(val);
            return c.getRenderGroup();

        }

    }

    public static class RenderablePro extends Property<Renderable> {

        public RenderablePro(String name) {
            super(name);

        }

        public RenderablePro(String name, String cap) {
            super(name, cap);

        }

        @Override
        protected Renderable parse(String val, LoadContext lc) {

            LoadContext c = lc.createChild();
            c.parseBinding(val);
            return c.getRenderable();

        }

    }

}
