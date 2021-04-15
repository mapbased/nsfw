package com.mapkc.nsfw.component;

import com.mapkc.nsfw.model.Fragment;
import com.mapkc.nsfw.model.LoadContext;
import com.mapkc.nsfw.model.RenderContext;
import com.mapkc.nsfw.model.XEnum;
import org.jsoup.nodes.Element;

/**
 * 把引用的页面直接包含进来，不主张使用，因为被引用的页面改变后，不能联动改变。页面如果太大，会浪费内存
 * 但在query可能有用因为导入进来跟包含进来还是不一样的。
 * 另外，inline也可以导入一个page，只是使用了dom结构，相应的handler都会忽视
 *
 * @author chy
 */
public class Inline extends Component {


    @Override
    public void render(RenderContext rc) {

    }

    @Override
    public void parseXml(Element ele, LoadContext lc) {
        String fragmentPath = lc.fetchAttribute(ele, "page");
        if (fragmentPath == null) {
            throw new java.lang.RuntimeException(
                    "Must provide page attribute for Import component");
        }
        Fragment f = lc.site.getFragment(fragmentPath);
        if (f == null) {
            throw new java.lang.RuntimeException("Cannot find ：" + fragmentPath);

        }
        String html = f.getAttribute(XEnum.KnownAttributes.content.name());
        lc.parseDom(html);

    }

    @Override
    public void toXml(XmlContext xc) {
        // TODO

    }

}
