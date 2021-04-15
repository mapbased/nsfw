package com.mapkc.nsfw.component;

import com.mapkc.nsfw.binding.Binding;
import com.mapkc.nsfw.input.FormField;
import com.mapkc.nsfw.model.LoadContext;
import com.mapkc.nsfw.model.RenderContext;
import com.mapkc.nsfw.model.Renderable;
import org.jsoup.nodes.Element;

/**
 * 条件判断
 */
public class If extends Component {

    static final Property[] properties = new Property[]{

            new Property.BindingPro("test", "条件"),
            new Property.Bool("include", "include"),
            new Property.Bool("reverse", "reverse")

    };

    Binding test;
    Renderable rg;
    Renderable elsee;

    @FormField(caption = "include", input = "checkbox")
    boolean include = true;

    boolean reverse = false;

    @Override
    public void render(RenderContext rc) {
        boolean v = this.test.getBooleanRenderValue(rc);
        if (reverse) {
            v = !v;
        }
        if (v) {

            rg.render(rc);
        } else if (elsee != null) {
            elsee.render(rc);
        }

    }

    @Override
    public void parseXml(Element ele, LoadContext lc) {
        this.parseProperties(properties, ele, lc);
        if (lc.fetchBooleanAttribute(ele, "r")) {
            this.reverse = true;
        }

        for (Element e : ele.children()) {
            // Element e = (Element) o;
            if ("else".equalsIgnoreCase(e.attr("jid"))) {
                lc.fetchAttribute(e, "jid");
                String incStr = e.attr("include");
                boolean inc = this.include;
                if (!"".equals(incStr)) {
                    inc = lc.fetchBooleanAttribute(e, "include");
                }

                elsee = inc ? lc.createChild().parseElement(e)
                        .getRenderable() : lc.createChild()
                        .parseElementChildren(e).getRenderable();
                e.remove();
            }
        }
        rg = this.include ? lc.createChild().parseElement(ele).getRenderable()
                : lc.createChild().parseElementChildren(ele).getRenderable();

    }

    @Override
    public void toXml(XmlContext xc) {
        throw new java.lang.UnsupportedOperationException("If.toXml");
    }

}
