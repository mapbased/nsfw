package com.mapkc.nsfw.component;

import com.mapkc.nsfw.model.LoadContext;
import com.mapkc.nsfw.model.RenderContext;
import com.mapkc.nsfw.model.Renderable;
import com.mapkc.nsfw.model.Xmlable;
import org.jsoup.nodes.Element;

import java.lang.reflect.Field;

/**
 * @author chy
 */
public abstract class Component implements Renderable, Xmlable {

    protected String componentId;

    public String getComponentId() {
        return componentId;
    }

    public void setComponentId(String cid) {
        this.componentId = cid;
    }


    @Override
    public void clean() {


    }

    @Override
    final public String getRenderValue(RenderContext rc) {

        RenderContext rcchild = rc.createSubRenderContext();
        this.render(rcchild);

        return rcchild.getRenderedString();

    }

    /**
     * 可以触发的服务器端事件名字列表
     *
     * @return
     */
    public String[] getEventNames() {
        return null;
    }

    /**
     * 处理事件
     *
     * @param rc
     * @param evtName
     */
    public boolean handleAjaxEvent(RenderContext rc, String evtName) {

        return false;
    }

    /**
     * todo
     *
     * @param name
     * @param value
     */
    final public void setProperty(String name, Object value) {
        try {
            Field f = this.getClass().getDeclaredField(name);
            f.setAccessible(true);
            f.set(this, value);
        } catch (Exception e) {
            throw new java.lang.RuntimeException(e);
        }
    }

    final public Object getProperty(String name) {
        try {
            Field f = this.getClass().getDeclaredField(name);
            f.setAccessible(true);
            return f.get(this);
        } catch (Exception e) {
            throw new java.lang.RuntimeException(e);
        }
    }

    public final void parseProperties(Property[] properties, Element ele,
                                      LoadContext lc) {

        for (Property p : properties) {
            p.parseXml(ele, lc, this);
        }

    }

    public final void xmlProperties(Property[] properties, StringBuilder xc) {
        for (Property p : properties) {
            xc.append(" ");
            p.toXml(xc, this);
        }

    }

    public final void xmlProperties(Property[] properties, XmlContext xc) {
        for (Property p : properties) {
            xc.sb.append(" ");
            p.toXml(xc.sb, this);
        }

    }

    @Override
    public void designRender(RenderContext rc) {
        this.render(rc);

    }

}
