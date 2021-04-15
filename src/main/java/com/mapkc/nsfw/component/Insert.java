package com.mapkc.nsfw.component;

import com.mapkc.nsfw.model.*;
import org.jsoup.nodes.Element;

/**
 * 用在masterpage中插入实际的body部分
 *
 * @author chy
 */
public class Insert implements Renderable, Xmlable {

    private String name;
    RenderGroup defaultv;

    // private RenderGroup getRenderGroup(RenderContext rc) {
    // Page p = rc.getCurrentMasterClient();
    // RenderGroup r = (p == null) ? null : p.getRenderGroup(this.name);
    // if (r == null) {
    // r = this.defaultv;
    //
    // } else {
    // rc.decreaseMasterClientIndex();
    // }
    //
    // return r;
    //
    // }

    @Override
    public void render(RenderContext rc) {
        int back = rc.getMasterClientIndex();
        Fragment old = rc.getCurrentFragment();
        Page p = rc.getCurrentMasterClient();
        RenderGroup r = (p == null) ? null : p.getRenderGroup(this.name);
        if (r == null) {
            r = this.defaultv;

        } else {
            rc.decreaseMasterClientIndex();
            rc.setCurrentFragment(p);
        }

        if (r != null) {
            r.render(rc);
        }
        rc.setMasterClientIndex(back);
        rc.setCurrentFragment(old);

    }

    @Override
    public String getRenderValue(RenderContext rc) {
        String v = "";
        int back = rc.getMasterClientIndex();
        Fragment old = rc.getCurrentFragment();
        Page p = rc.getCurrentMasterClient();
        RenderGroup r = (p == null) ? null : p.getRenderGroup(this.name);
        if (r == null) {
            r = this.defaultv;

        } else {
            rc.decreaseMasterClientIndex();
            rc.setCurrentFragment(p);
        }

        if (r != null) {

            v = r.getRenderValue(rc);
        }
        rc.setMasterClientIndex(back);
        rc.setCurrentFragment(old);
        return v;
    }

    @Override
    public void parseXml(Element ele, LoadContext lc) {
        name = lc.fetchAttribute(ele, "name");
        LoadContext child = lc.createChild();
        child.parseElementChildren(ele);
        this.defaultv = child.getRenderGroup();

    }

    @Override
    public void toXml(XmlContext xb) {

        StringBuilder sb = xb.sb;
        sb.append("<div jid=\"Insert\" name=\"").append(this.name)
                .append("\" >");
        if (this.defaultv != null) {
            this.defaultv.toXml(xb);
        }
        sb.append("</div>");

    }

    @Override
    public void designRender(RenderContext rc) {
        this.render(rc);

    }

    @Override
    public void clean() {
        // TODO Auto-generated method stub

    }

}