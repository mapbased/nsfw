package com.mapkc.nsfw.component;

import com.mapkc.nsfw.binding.Binding;
import com.mapkc.nsfw.model.LoadContext;
import com.mapkc.nsfw.model.RenderContext;
import com.mapkc.nsfw.model.Renderable;
import com.mapkc.nsfw.model.TreeNode;
import org.jsoup.nodes.Element;

import java.util.List;

@Deprecated
public class Tree extends Component {

    public static class TreeVar {
        public TreeNode node;

    }

    public static final Property[] properties = new Property[]{

            new Property.BindingPro("root", "根节点"),

    };
    Binding root;
    Renderable content;
    Renderable rootAttributes;

    /**

     */

    @Override
    public void render(RenderContext rc) {
        TreeNode node = (TreeNode) root.getValue(rc);
        TreeVar tn = new TreeVar();
        tn.node = node;
        rc.setVar("treenode", tn);

        rc.write("<ul ");
        this.rootAttributes.render(rc);
        rc.write(" > <li>");
        this.content.render(rc);
        List<TreeNode> c = node.getChildren();
        if (c.size() > 0) {
            this.renderChildren(rc, tn);
        }
        rc.write("</li></ul>");

        this.content.render(rc);

    }

    private void renderChildren(RenderContext rc, TreeVar tn) {
        TreeNode backup = tn.node;
        for (TreeNode n : backup.getChildren()) {
            rc.write("<ul><li>");
            tn.node = n;
            this.content.render(rc);
            if (n.getChildren().size() > 0) {
                this.renderChildren(rc, tn);
            }
            rc.write("</li></ul>");
        }
        tn.node = backup;

    }

    @Override
    public void parseXml(Element ele, LoadContext lc) {
        super.parseProperties(properties, ele, lc);

        rootAttributes = lc.createChild().parseAttributes(ele.attributes())
                .getRenderable();
        // this.content=lc.createChild().parseElement(ele).getRenderable();
        Element e = ele.children().first();
        if (e.nodeName().equals("li")) {
            e = e.children().first();
        }
        this.content = lc.createChild().parseElement(e).getRenderable();

    }

    @Override
    public void toXml(XmlContext xc) {
        // TODO Auto-generated method stub

    }

}
