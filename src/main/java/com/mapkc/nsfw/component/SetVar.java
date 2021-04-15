package com.mapkc.nsfw.component;

import com.mapkc.nsfw.binding.Binding;
import com.mapkc.nsfw.model.LoadContext;
import com.mapkc.nsfw.model.RenderContext;
import com.mapkc.nsfw.model.Renderable;
import org.jsoup.nodes.Element;

public class SetVar extends Component {

    public static final Property[] properties = new Property[]{

            new Property.RenderablePro("value", "value"),
            new Property.Str("name", "name")

            // new Property.Str("groupField", "groupField"),

    };
    String name;
    Renderable value;

    @Override
    public void render(RenderContext rc) {
        if (value instanceof Binding) {
            Binding b = (Binding) value;
            rc.setVar(name, b.getValue(rc));
        } else {
            rc.setVar(name,
                    value == null ? null : this.value.getRenderValue(rc));
        }
    }

    @Override
    public void parseXml(Element ele, LoadContext lc) {
        super.parseProperties(properties, ele, lc);
    }

    @Override
    public void toXml(XmlContext xc) {
        // TODO Auto-generated method stub

    }

}
