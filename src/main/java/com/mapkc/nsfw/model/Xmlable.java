package com.mapkc.nsfw.model;

import com.mapkc.nsfw.component.XmlContext;
import org.jsoup.nodes.Element;

public interface Xmlable {

    void parseXml(Element ele, LoadContext lc);

    void toXml(XmlContext xc);

    /**
     * 运行时减肥，一旦clean，就不能再toxml
     */
    void clean();
}
