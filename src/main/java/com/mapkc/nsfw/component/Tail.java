package com.mapkc.nsfw.component;

import com.mapkc.nsfw.model.LoadContext;
import com.mapkc.nsfw.model.RenderContext;
import com.mapkc.nsfw.model.Renderable;
import com.mapkc.nsfw.model.Xmlable;
import org.jsoup.nodes.Element;

public class Tail implements Xmlable, Renderable {


    public static final Tail INSTANCE = new Tail();

    @Override
    public void parseXml(Element ele, LoadContext lc) {
        // TODO Auto-generated method stub

    }

    @Override
    public void toXml(XmlContext xc) {
        // TODO Auto-generated method stub

    }

    @Override
    public void clean() {
        // TODO Auto-generated method stub

    }

    @Override
    public void render(RenderContext rc) {

        rc.writeTailContents();


    }

    @Override
    public void designRender(RenderContext rc) {
        this.render(rc);

    }

    @Override
    public String getRenderValue(RenderContext rc) {
        return rc.getTailStr();
    }

}
