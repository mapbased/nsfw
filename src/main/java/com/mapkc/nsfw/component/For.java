package com.mapkc.nsfw.component;

import com.mapkc.nsfw.binding.Binding;
import com.mapkc.nsfw.model.LoadContext;
import com.mapkc.nsfw.model.RenderContext;
import com.mapkc.nsfw.model.RenderGroup;
import com.mapkc.nsfw.model.Renderable;
import com.mapkc.nsfw.util.DynamicLoop;
import com.mapkc.nsfw.vl.ValueList;
import org.jsoup.nodes.Element;
import org.mvel2.integration.VariableResolver;

public class For extends Component {

    public static final class ForBag implements VariableResolver {

        public ForBag() {

        }

        public ForBag(Object v) {
            this.value = v;
        }

        // public int index;
        public Object value;
        // public Object key;

        @Override
        public String toString() {

            return "ForBag:" + (value);
        }

        @Override
        public int getFlags() {

            return 0;
        }

        @Override
        public String getName() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Class getType() {

            // if (value != null) {
            // return value.getClass();
            // }
            return Object.class;
        }

        @Override
        public Object getValue() {
            // TODO Auto-generated method stub
            return value;
        }

        @Override
        public void setStaticType(Class arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void setValue(Object arg0) {
            this.value = arg0;

        }

    }

    static final Property[] properties = new Property[]{

            new Property.RenderablePro("from", "from"),
            new Property.RenderablePro("to", "to"),
            new Property.BindingPro("data", "数据源"),
            new Property.ValueListPro("vl", "数据源"),

            new Property.Str("var", "var"),
            new Property.Str("id", "id"),
            new Property.Str("delimiter", "delimiter"),

            new Property.Bool("include", "include")

    };
    Renderable from;
    Renderable to;
    Binding data;
    Binding _if;
    ValueList vl;
    String var;
    /**
     * 中间分割符，只有在元素中间才插入，对生成json很有用
     */
    String delimiter;
    boolean include = true; // include border or not
    /**
     * not by attribute
     */
    RenderGroup content;
    String id;// compoent id;
    String eleName;

    @Override
    public void render(final RenderContext rc) {
        int ifrom = Integer.MAX_VALUE;
        int ito = Integer.MAX_VALUE;

        if (from != null) {
            String s = from.getRenderValue(rc).trim();
            if (s.length() > 0) {
                ifrom = Integer.parseInt(s);
            }

        }
        if (to != null) {
            String s = to.getRenderValue(rc).trim();
            if (s.length() > 0) {
                ito = Integer.parseInt(s);
            }

        }
        Object vdata = null;

        // 支持嵌套的循环，但是嵌套的内容必须放到最后
        ForBag kfb = null;
        if (this.var != null) {
            Object keepedfb = rc.vars().get(this.var);

            if (keepedfb instanceof ForBag) {
                kfb = (ForBag) keepedfb;
            } else {
                kfb = new ForBag();
                rc.setVar(this.var, kfb);

            }
        }

        final ForBag fb = kfb;
        if (data != null) {
            vdata = data.getValue(rc);
        } else if (vl != null) {
            vdata = vl;

        } else {

            if (ifrom == Integer.MAX_VALUE) {
                ifrom = 0;
            }
            if (ito == Integer.MAX_VALUE) {
                ito = 20;
            }
            for (int i = ifrom; i < ito; i++) {
                if (fb != null) fb.value = i;
                this.doLoop(rc);
                if (this.delimiter != null && i < ito - 1) {
                    rc.write(this.delimiter);
                }

            }
            return;

        }

        if (vdata == null) {
            return;
        }
        DynamicLoop.DLVisitor dlv = this.delimiter == null ? new DynamicLoop.DLVisitor() {

            @Override
            public void visit(Object value, Object key) {
                if (fb != null) fb.value = value;
                // fb.key = key;
                // fb.index++;
                doLoop(rc);

            }
        } : new DynamicLoop.DLVisitor() {
            boolean hasIn = false;

            @Override
            public void visit(Object value, Object key) {
                if (fb != null) fb.value = value;

                if (hasIn) {
                    rc.write(delimiter);
                } else {
                    hasIn = true;
                }
                doLoop(rc);

            }
        };
        if (vdata instanceof ValueList) {
            DynamicLoop.loopIterator(((ValueList) vdata).iterator(rc), dlv);
            return;
        }
        DynamicLoop.loop(vdata, dlv);


    }

    private void doLoop(RenderContext rc) {
        if (this._if != null) {
            if (!this._if.getBooleanRenderValue(rc)) {
                return;
            }
        }
        this.content.render(rc);
    }

    @Override
    public void parseXml(Element ele, LoadContext lc) {
        this.parseProperties(properties, ele, lc);
        String ifs = lc.fetchAttribute(ele, "if");
        if (ifs != null) {
            this._if = lc.getBindingStr(ifs);
        }
        this.eleName = ele.nodeName();

        LoadContext clc = lc.createChild();
        // id = clc.fetchAttribute(ele, "id");
        if (this.var == null) {
            this.var = id;
        }
        /*if (this.var == null) {
            throw new java.lang.RuntimeException(
					"FOR component cannot find id or var");
		}*/
        if (this.include) {

            clc.parseElement(ele);
        } else {
            clc.parseElementChildren(ele);
        }

        this.content = clc.getRenderGroup();

    }

    @Override
    public void toXml(XmlContext xc) {

        StringBuilder sb = xc.sb;

        sb.append("<").append(this.eleName).append(" ");
        super.xmlProperties(properties, sb);
        sb.append(" ").append(LoadContext.JWCID).append("=\"for\" ");

        XmlContext child = xc.createChild();
        this.content.toXml(child);

        if (this.include) {
            String s = child.sb.toString().substring(this.eleName.length() + 1);
            sb.append(s);
        } else {
            sb.append(">").append(child.sb).append("</").append(this.eleName)
                    .append(">");
        }

    }

}
