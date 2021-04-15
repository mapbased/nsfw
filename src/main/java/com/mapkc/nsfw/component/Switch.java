package com.mapkc.nsfw.component;

import com.mapkc.nsfw.input.FormField;
import com.mapkc.nsfw.model.LoadContext;
import com.mapkc.nsfw.model.RenderContext;
import com.mapkc.nsfw.model.Renderable;
import org.jsoup.nodes.Element;

/**
 * Created by chy on 14/11/20.
 */
public class Switch extends Component {

      static final Property[] properties = new Property[]{

            new Property.RenderablePro("value", "条件"),
            new Property.Bool("include", "include"),


    };

    Renderable value;


    @FormField(caption = "include", input = "checkbox")
    boolean include = true;
    Renderable rg;

    @Override
    public void render(RenderContext rc) {





    }

    @Override
    public void parseXml(Element ele, LoadContext lc) {
        this.parseProperties(properties, ele, lc);

        for (Element element : ele.children()) {
            String casejid=lc.fetchAttribute(element,"jid");
            if(casejid.equalsIgnoreCase("when")){

            }
           // if(lc.fetchAttribute())

        }
        if(include){
            rg = this.include ? lc.createChild().parseElement(ele).getRenderable()
                    : lc.createChild().parseElementChildren(ele).getRenderable();
        }



    }

    @Override
    public void toXml(XmlContext xc) {

    }
}
