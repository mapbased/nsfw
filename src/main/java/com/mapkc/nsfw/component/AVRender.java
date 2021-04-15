package com.mapkc.nsfw.component;

import com.mapkc.nsfw.binding.Binding;
import com.mapkc.nsfw.model.LoadContext;
import com.mapkc.nsfw.model.RenderContext;
import com.mapkc.nsfw.model.Renderable;
import com.mapkc.nsfw.model.Xmlable;
import org.jsoup.nodes.Element;

public class AVRender implements Renderable, Xmlable {

    String name;
    final Binding binding;
    final String value;

    public AVRender(String name, Binding b) {
        this.name = name;
        this.binding = b;
        value = name + "=\"" + name + "\"";
    }

    @Override
    public void clean() {
        name = null;

    }

    @Override
    public void parseXml(Element ele, LoadContext lc) {

    }

    @Override
    public void toXml(XmlContext xc) {

        xc.sb.append(" ").append(name).append("-=\"");
        this.binding.toXml(xc);
        xc.sb.append("\" ");
    }

    @Override
    public void render(RenderContext rc) {
        if (this.binding.getBooleanRenderValue(rc)) {
            rc.write(this.value);
        }
    }

    @Override
    public String getRenderValue(RenderContext rc) {
        if (this.binding.getBooleanRenderValue(rc)) {
            return this.value;
        }
        return "";
    }

    @Override
    public void designRender(RenderContext rc) {
        this.render(rc);

    }

}
