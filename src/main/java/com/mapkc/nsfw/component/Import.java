package com.mapkc.nsfw.component;

import com.mapkc.nsfw.FKNames;
import com.mapkc.nsfw.binding.Binding;
import com.mapkc.nsfw.model.*;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * 倒入代码片段，代码片段外部被修改后，自动生效
 *
 * @author chy
 */
public class Import implements Renderable, Xmlable, ActionHandler {


    private String fragmentPath;
    /**
     * 嵌套的import[自己引入自己],必须设置为true
     */
    private boolean recursive;
    private boolean noerror;

    FieldBag<Fragment> bag;

    Bag[] vars;

    @Override
    public void render(RenderContext rc) {

        Fragment f = bag.get(rc);// rc.getSite().getFragment(this.fragmentPath);
        Object[] backedvars = null;
        if (f != null) {
            int vl = this.vars.length;
            if (vl > 0) {
                backedvars = new Object[vl];

                for (int i = 0; i < vl; i++) {
                    backedvars[i] = rc.vars().put(vars[i].name,
                            vars[i].getValue(rc));
                }
            }


            f.render(rc);
            if (vl > 0) {
                for (int i = 0; i < vl; i++) {

                    rc.vars().put(vars[i].name, backedvars[i]);
                }
            }
        } else if (!this.noerror) {
            rc.append("<b>").append("Cannot find page:")
                    .append(this.fragmentPath).append("</b>");
            rc.setVar(FKNames.FK_EXCEPTION, "Canod find page for import:" + this.fragmentPath);
        }

    }

    @Override
    public void parseXml(Element ele, LoadContext lc) {

        fragmentPath = lc.fetchAttribute(ele, "page");
        if (fragmentPath == null) {
           fragmentPath= lc.fetchAttribute(ele, "href");
        }
        this.recursive = lc.fetchBooleanAttribute(ele, "recursive");
        this.noerror = lc.fetchBooleanAttribute(ele, "noerror");
        Attributes as = ele.attributes();
        //vars=new TreeMap<String, Object>();
        List<Bag> vs = new ArrayList<Bag>(2);

        for (Attribute a : as) {
            String k = a.getKey();
            String v = a.getValue();
            Renderable r = LoadContext.getRenderable(v);
            if (r instanceof StrRender) {
                StringBag b = new StringBag();
                b.value = ((StrRender) r).value;
                b.name = k;
                vs.add(b);
            } else if (r instanceof Binding) {
                BindingBag b = new BindingBag();
                b.value = (Binding) r;
                b.name = k;
                vs.add(b);

            } else {
                RenderableBag b = new RenderableBag();
                b.value = r;
                b.name = k;
                vs.add(b);
            }


        }
        this.vars = vs.toArray(new Bag[vs.size()]);


        final Fragment lf = lc.getLoadingFragment();
        if (this.recursive) {
            this.bag = new FieldBag<Fragment>() {

                @Override
                public Fragment get(RenderContext rc) {
                    return lf;
                }

            };

        } else {

            if (fragmentPath == null) {
                throw new java.lang.RuntimeException(
                        "Must provide page attribute for Import component");
            }
            this.bag = FieldBag.from(fragmentPath, Fragment.class, lc);
        }
        // this.defaultPagebag = lc.getSite().getEnum(fragmentPath);

    }

    @Override
    public void toXml(XmlContext xc) {
        String s = this.fragmentPath;
        xc.sb.append("<div jid=\"import\" page=\"").append(s).append("\" >")
                .append("</div>");

    }

    @Override
    public boolean filterAction(RenderContext rc) {

        if (this.recursive) {
            return false;
        }
        Fragment f = bag.get(rc);// rc.getSite().getFragment(this.fragmentPath);
        if (f != null) {
//			for (int i = 0; i < this.vars.length; i++) {
//				  rc.vars().put(vars[i].name,
//						vars[i].getValue(rc));
//			}
            return f.doActions(rc);
        }

        return false;

    }

    public String getFragmentPath() {
        return fragmentPath;
    }

    public boolean isRecursive() {
        return recursive;
    }

    public boolean isNoerror() {
        return noerror;
    }

    @Override
    public String getRenderValue(RenderContext rc) {
        throw new java.lang.UnsupportedOperationException();
    }

    @Override
    public void designRender(RenderContext rc) {
        this.render(rc);

    }

    @Override
    public void clean() {
        fragmentPath = null;

    }

    private static class Bag {
        String name;

        public Object getValue(RenderContext rc) {
            return null;
        }

    }

    private static class StringBag extends Bag {
        String value;

        @Override
        public Object getValue(RenderContext rc) {
            return value;
        }
    }

    private static class BindingBag extends Bag {
        Binding value;

        @Override
        public Object getValue(RenderContext rc) {
            return value.getValue(rc);
        }
    }

    private static class RenderableBag extends Bag {
        Renderable value;

        @Override
        public Object getValue(RenderContext rc) {
            return value.getRenderValue(rc);
        }
    }

}
