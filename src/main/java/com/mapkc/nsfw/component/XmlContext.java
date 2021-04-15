package com.mapkc.nsfw.component;

import com.mapkc.nsfw.model.Page;

public class XmlContext {
    public StringBuilder sb = new StringBuilder();
    public Page currentClientPage;
    public Page page;

    public XmlContext createChild() {
        XmlContext c = new XmlContext();
        // c.sb = new StringBuilder();
        c.currentClientPage = this.currentClientPage;
        c.page = this.page;
        return c;
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return sb.toString();
    }

}
