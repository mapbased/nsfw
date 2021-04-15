package com.mapkc.nsfw.component;

import com.mapkc.nsfw.model.RenderContext;
import com.mapkc.nsfw.model.Renderable;

public class StrRender implements Renderable {

    final public String value;

    public StrRender(String v) {
        this.value = v;
    }

    @Override
    public void render(RenderContext rc) {
        rc.write(this.value);

    }

    @Override
    public String getRenderValue(RenderContext rc) {

        return value;
    }

    @Override
    public void toXml(XmlContext xc) {
        xc.sb.append(value);
    }

    @Override
    public void designRender(RenderContext rc) {
        this.render(rc);

    }
}
