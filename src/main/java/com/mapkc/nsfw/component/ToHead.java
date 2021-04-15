package com.mapkc.nsfw.component;

import com.mapkc.nsfw.model.ActionHandler;
import com.mapkc.nsfw.model.LoadContext;
import com.mapkc.nsfw.model.RenderContext;
import com.mapkc.nsfw.model.Renderable;
import org.jsoup.nodes.Element;

public class ToHead extends Component implements ActionHandler {

    static final Property[] properties = new Property[]{

            new Property.RenderablePro("key", "key"),
            new Property.Bool("include", "模板")

    };
    Renderable key;
    boolean include = true;
    Renderable content;

    @Override
    public void render(RenderContext rc) {
        // TODO Auto-generated method stub

    }

    @Override
    public void parseXml(Element ele, LoadContext lc) {
        super.parseProperties(properties, ele, lc);
        if (this.include) {
            this.content = lc.createChild().parseElement(ele).getRenderable();
        } else {
            this.content = lc.createChild().parseElementChildren(ele)
                    .getRenderable();
        }

    }

    @Override
    public void toXml(XmlContext xc) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean filterAction(RenderContext rc) {

        String s = this.content.getRenderValue(rc);
        String k = s;
        if (this.key != null) {
            k = key.getRenderValue(rc);
        }
        // rc.addHead(k, s);
        addContent(rc, k, s);
        return false;
    }

    protected void addContent(RenderContext rc, String key, String value) {
        rc.addHeadContent(key, value);
    }

}
