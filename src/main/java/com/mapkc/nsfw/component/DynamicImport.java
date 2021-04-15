package com.mapkc.nsfw.component;

import com.mapkc.nsfw.model.*;
import org.jsoup.nodes.Element;

/**
 * 根据type不同，动态import不同路径下fraggment<br/>
 * Import包含了该类所有的功能。 同 Import的不同之处在于该类不执行相应actions
 *
 * @author chy
 */
public class DynamicImport implements Renderable, Xmlable
// , ActionHandler 只渲染，无法doActions，因为事先类型未知
{

    private Renderable path;

    public DynamicImport() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public void parseXml(Element ele, LoadContext lc) {
        String p = lc.fetchAttribute(ele, "page");
        this.path = lc.getRenderableStr(p);


    }

    @Override
    public void toXml(XmlContext xc) {
        // TODO Auto-generated method stub

    }

    @Override
    public void clean() {
        // TODO Auto-generated method stub

    }

    @Override
    public void render(RenderContext rc) {

        Fragment f = get(rc);// rc.getSite().getFragment(this.fragmentPath);
        if (f != null) {
            f.doActions(rc);

            f.render(rc);
        } else {
            rc.append("<b>").append("Cannot find page:")
                    .append(this.path.getRenderValue(rc)).append("</b>");
        }
    }

    @Override
    public void designRender(RenderContext rc) {
        this.render(rc);

    }

    @Override
    public String getRenderValue(RenderContext rc) {
        throw new java.lang.UnsupportedOperationException();

    }

    private Fragment get(RenderContext rc) {
        if(path==null){
            return  null;
        }
        String s = this.path.getRenderValue(rc);
        return rc.getSite().getFragment(s);// tXEnum(s);
    }


}
