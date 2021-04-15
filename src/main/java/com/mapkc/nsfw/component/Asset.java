package com.mapkc.nsfw.component;

import com.mapkc.nsfw.model.RenderContext;
import com.mapkc.nsfw.model.Renderable;

/**
 * Created by chy on 14/12/11.
 */
public class Asset implements Renderable {

    private String path;

    public Asset(String path) {
        this.path = path;

    }

    @Override
    public void render(RenderContext rc) {
        rc.write(this.path);

    }

    @Override
    public void designRender(RenderContext rc) {
        rc.write(this.path);

    }

    @Override
    public String getRenderValue(RenderContext rc) {
        return this.path;
    }

    @Override
    public void toXml(XmlContext xc) {

    }
}
