package com.mapkc.nsfw.component;

import com.mapkc.nsfw.model.LoadContext;
import com.mapkc.nsfw.model.RenderContext;
import com.mapkc.nsfw.model.Renderable;
import com.mapkc.nsfw.model.Xmlable;
import org.jsoup.nodes.Element;

public class Head implements Xmlable, Renderable {

    public static final Head INSTANCE = new Head();

    private Head() {

    }

    @Override
    public void parseXml(Element ele, LoadContext lc) {

    }

    @Override
    public void toXml(XmlContext xc) {
        xc.sb.append("<link jid=\"Head\" />");

    }

    @Override
    public void render(RenderContext rc) {

        rc.writeHeadContents();

    }

    @Override
    public void designRender(RenderContext rc) {
        this.render(rc);

    }

    @Override
    public String getRenderValue(RenderContext rc) {
        return rc.getHeadStr();
    }

    @Override
    public void clean() {
        // TODO Auto-generated method stub

    }

}