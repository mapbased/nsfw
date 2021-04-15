package com.mapkc.nsfw.component;

import com.mapkc.nsfw.model.LoadContext;
import com.mapkc.nsfw.model.RenderContext;
import org.jsoup.nodes.Element;

// @edu.umd.cs.findbugs.annotations.SuppressFBWarnings({"URF_UNREAD_FIELD"})
public class Depend extends Component {

    public static final Property[] properties = new Property[]{

            new Property.Bool("include", "include")

    };
    String[] on;
    boolean include = true;

    @Override
    public void render(RenderContext rc) {

    }

    @Override
    public void parseXml(Element ele, LoadContext lc) {
        on = lc.fetchStringsAttribute(ele, "on");


    }

    @Override
    public void toXml(XmlContext xc) {

    }

}
