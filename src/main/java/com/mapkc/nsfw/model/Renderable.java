package com.mapkc.nsfw.model;

import com.mapkc.nsfw.component.XmlContext;

public interface Renderable {
    /**
     * 正常的渲染
     *
     * @param rc
     */
    void render(RenderContext rc);

    /**
     * 设计时渲染
     *
     * @param rc
     */

    void designRender(RenderContext rc);

    String getRenderValue(RenderContext rc);

    void toXml(XmlContext xc);


}
