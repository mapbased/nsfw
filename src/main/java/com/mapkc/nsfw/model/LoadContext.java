package com.mapkc.nsfw.model;

import com.mapkc.nsfw.FKNames;
import com.mapkc.nsfw.binding.Binding;
import com.mapkc.nsfw.component.AVRender;
import com.mapkc.nsfw.component.Component;
import com.mapkc.nsfw.component.Components;
import com.mapkc.nsfw.component.StrRender;
import com.mapkc.nsfw.query.QueryLoader;
import com.mapkc.nsfw.query.QuerysLoader;
import org.jsoup.nodes.*;

import java.util.ArrayList;
import java.util.List;

public class LoadContext {
    public static final String JWCID = "jid";
    public final Site site;
    final Fragment loadingFragment;
    QuerysLoader querysLoader = new QuerysLoader();
    // public AtomicInteger varIndex = new AtomicInteger();
    private final StringBuilder last = new StringBuilder(1024);
    private final List<Renderable> renderables = new ArrayList<Renderable>();
    // Map<String, Component> components = new java.util.HashMap<String,
    // Component>();
    private List<ActionHandler> headables = new java.util.ArrayList<ActionHandler>();
    private LoadContext parent;


    public LoadContext(Site site, Fragment loadingFragment) {
        this.site = site;
        this.loadingFragment = loadingFragment;
    }

    private LoadContext() {
        this.site = null;
        this.loadingFragment = null;
    }

    public static Renderable parseDom(String domStr, LoadContext clc) {
        Document doc = org.jsoup.parser.Parser.parseBodyFragment(domStr, "");

        clc.parseElementChildren(doc.body());
        return clc.getRenderable();
    }

    private static String parseCmd(String s) {
        int st = s.indexOf("{");
        int et = s.indexOf("}", st);
        if (st < 0 || et <= st) {

            return null;
            // throw new java.lang.RuntimeException("Error parse giving Cmd:" +
            // s);
        }
        String ret = s.substring(st + 1, et);
        if (ret.indexOf('{') > 0) {
            et = s.indexOf('}', et + 1);
            ret = s.substring(st + 1, et);
        }
        return ret;


    }

    private static String removeBindPart(String s) {
        int i = s.indexOf("}");
        if (i <= 0) {
            throw new java.lang.RuntimeException("Cannot find Cmd end tag:" + s);
        }

        return s.substring(i + 1);
    }

    public static Renderable getRenderable(String s) {
        LoadContext clc = new LoadContext();
        clc.parseBinding(s);
        return clc.getRenderable();
    }

    static public Binding getBinding(String s) {
        LoadContext clc = new LoadContext();// this.createChild();
        clc.parseBinding(s);
        return clc.getRenderGroup().getFirstBinding();
    }

    /**
     * ../../../st
     *
     * @param s
     * @return
     */
    public String getPath(String s) {
        if (s.startsWith("/") || this.loadingFragment == null) {
            return s;
        }

        String pid = this.loadingFragment.getParentId();
        return RenderContext.relativePath(pid, s);
    }

    public boolean isWebDesign() {
        return this.loadingFragment != null && this.loadingFragment.designDoc != null;
    }

    ActionHandler[] getHeadables() {
        return headables.toArray(new ActionHandler[headables.size()]);
    }

    public QueryLoader getQueryLoader(String id) {

        return this.querysLoader.getQueryLoader(id);
    }

    public Site getSite() {
        LoadContext lc = this;
        while (lc.site == null && lc.parent != null) {
            lc = lc.parent;
        }
        return lc.site;
    }

    public LoadContext createChild() {
        LoadContext lc = new LoadContext(this.site, this.loadingFragment);
        // lc.components = this.components;
        lc.headables = this.headables;

        lc.querysLoader = this.querysLoader;
        // lc.loadingFragment = this.loadingFragment;
        // lc.varIndex = this.varIndex;
        lc.parent = this;

        return lc;
    }

    public int fetchIntAttribute(Element e, String... names) {
        for (String name : names) {
            int i = this.fetchIntAttribute(e, name, 0);
            if (i > 0) {
                return i;
            }
        }
        return 0;
    }

    public int fetchIntAttribute(Element e, String name, int defaultValue) {
        String av = this.fetchAttribute(e, name);
        if (av == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(av);
        } catch (NumberFormatException ee) {
            return defaultValue;
        }

    }

    /**
     * @param e
     * @param name
     * @return null if not exist
     */
    public String fetchAttribute(Element e, String name) {

        String s = e.attr(name);

        e.removeAttr(name);

        if (s.equals("")) {
            return null;
        } else if (this.isWebDesign()) {
            e.attr(name + "-design", s);
        }
        return s;
    }

    public String[] fetchStringsAttribute(Element e, String... names) {
        for (String name : names) {
            String s = this.fetchAttribute(e, name);
            if (s != null) {
                return s.split(",");
            }
        }
        return null;

    }

    public boolean fetchBooleanAttribute(Element e, String name) {
        String s = this.fetchAttribute(e, name);
        if (s == null) {
            return false;
        }
        return "true".equalsIgnoreCase(s);

    }

    // public void parseNameSpace(Element ele) {
    //
    // Namespace nsp = ele.getNamespace();
    // if (nsp != null) {
    // if (ele.getParent() == null
    // ||
    // !nsp.getURI().equalsIgnoreCase(ele.getParent().getNamespace().getURI()))
    // {
    // String s = nsp.asXML();
    // if (s.length() > 10) // make sure not add empty
    // // nsp
    // {
    // last.append(" ").append(s);
    // }
    // }
    // }
    //
    // }

    // use getRenderable instead
    public RenderGroup getRenderGroup() {
        if (this.last.length() > 0) {
            renderables.add(new StrRender(last.toString()));
            last.setLength(0);
        }
        RenderGroup rg = new RenderGroup(
                this.renderables.toArray(new Renderable[this.renderables.size()]));
        return rg;
    }

    public Renderable getRenderable() {
        if (this.last.length() > 0) {
            renderables.add(new StrRender(last.toString()));
            last.setLength(0);
        }
        if (this.renderables.size() == 1) {
            return this.renderables.get(0);
        }
        RenderGroup rg = new RenderGroup(
                this.renderables.toArray(new Renderable[this.renderables.size()]));
        return rg;
    }

    public void addString(String s) {
        last.append(s);
    }

    public void addRender(Renderable r) {
        if (last.length() > 0) {
            renderables.add(new StrRender(last.toString()));
            last.setLength(0);
        }
        this.renderables.add(r);
    }

    public Renderable parseComponent(Element ele, String jid) {
        Renderable r = Components.INSTANCE.create(jid, this);

        if (r == null) {
            throw new RuntimeException("Cannot create component:" + jid);
        }

        if (r instanceof Component) {
            Component com = (Component) r;

            String id = this.fetchAttribute(ele, "com-id");
            // String id = ele.attr("com-id");

            if (id != null) {
                com.setComponentId(id);


            } else {
                id = ele.attr("id");

                if (id != null && !id.equalsIgnoreCase("")) {
                    com.setComponentId(id);
                    // this.components.put(id, com);

                }
            }
            // com.setContainer(this.loadingFragment);
        }
        if (r instanceof Xmlable) {
            Xmlable x = (Xmlable) r;
            x.parseXml(ele, this);

        }
        if (r instanceof ActionHandler) {
            this.headables.add((ActionHandler) r);

        }
        return r;

    }

    public Renderable parseDom(String domStr) {
        // LoadContext clc = this.createChild();
        return parseDom(domStr, this);
    }

    public LoadContext parseElement(Element ele) {
        if (ele instanceof Document) {
            return this.parseElementChildren(ele);
        }
        if (this.isWebDesign()) {
            ele.attr("eleid-design", this.loadingFragment.designElements.size() + "");
            ele.attr("elepath-design", this.loadingFragment.getId() + "");

            this.loadingFragment.designElements.add(ele);
        }
        String a = this.fetchAttribute(ele, JWCID);// ele.attributeValue(JWCID);
        if (a != null) {
            if (a.equalsIgnoreCase("remove")) {
                return this;
            }
            Renderable r = this.parseComponent(ele, a);
            if (r != null) {
                this.addRender(r);
            }
            return this;

        }

        this.last.append("<").append(ele.tagName());
        this.parseAttributes(ele.attributes().asList());
        // this.parseNameSpace(ele);
        if (ele.tag().isSelfClosing()) {
            last.append(" />");
            return this;
        }
        //

        last.append(">");

        // ////////////////////////////////////////////////////////////////////////
        parseElementChildren(ele);
        last.append("</").append(ele.tagName()).append(">");

        return this;

    }

    public LoadContext parseElementChildren(Element ele) {
        // List segments=lc.segments;
        // if(ele.nodeName().equals("pre")){
        // ele.tag().preserveWhitespace()
        // System.out.println("");
        // }
        List<Node> nodes = ele.childNodes();
        for (Node n : nodes) {
            String nn = n.nodeName();

            if (nn.equals("#doctype")) {

                this.parseBinding(n.outerHtml());
                this.last.append("\n");

            } else if (nn.equals("#comment")) {

                this.parseBinding(n.outerHtml());

            } else if (nn.equals("#text")) {
                TextNode tn = (TextNode) n;
                this.parseBinding(tn.text(), true);

            } else if (nn.equals("#data")) {
                this.parseBinding(n.outerHtml());
            } else {

                parseElement((Element) n);
            }
        }
        return this;

    }

    public LoadContext parseAttributes(org.jsoup.nodes.Attributes attributes) {
        return this.parseAttributes(attributes.asList());
    }

    public LoadContext parseAttributes(List<Attribute> attributes) {

        for (int i = 0; i < attributes.size(); i++) {
            Attribute an = attributes.get(i);
            String aname = an.getKey();
            String avalue = an.getValue();


            last.append(" ");
            if (aname.endsWith("-design")) {
                last.append(aname);
                last.append("=\"");
                last.append(an.getValue());
                last.append('"');
                continue;

            } else if (aname.startsWith("@")) {
                if (an.getValue().length() > 0)
                    this.parseBinding(an.getKey() + "=" + an.getValue());
                else {
                    this.parseBinding(an.getKey());
                }
                continue;
            } else if (aname.endsWith("-")) {
                Binding b = this.getBindingStr(avalue);
                if (b == null) {
                    throw new java.lang.RuntimeException("Cannot parse:"
                            + an.toString());
                }
                this.addRender(new AVRender(aname.substring(0,
                        aname.length() - 1), b));

                continue;
            } else if (aname.equals(FKNames.FK_REMOVE)) {

                this.addRender(this.getRenderableStr(an.getValue()));
                continue;

            }
            // =(Attribute)n;

            last.append(aname);
            if (avalue != null) {
                last.append("=\"");

                if (avalue.startsWith("/static/") && avalue.indexOf('@') < 0) {
                    this.addRender(site.getAsset(avalue));


                } else {
                    parseBinding(avalue);
                }
                last.append('"');
            } else {
                //  avalue=null;
            }
        }
        return this;
    }

    public Renderable getRenderableStr(String s) {
        LoadContext clc = this.createChild();
        clc.parseBinding(s);
        return clc.getRenderable();
    }

    public Binding getBindingStr(String s) {
        LoadContext clc = this.createChild();
        clc.parseBinding(s);
        return clc.getRenderGroup().getFirstBinding();
    }

    public Binding getFirstBinding() {
        for (Renderable r : this.renderables) {
            if (r instanceof Binding) {
                return (Binding) r;
            }

        }
        return null;
    }

    public void parseBinding(String s) {
        this.parseBinding(s, false);
    }

    public void parseBinding(String s, boolean inParsedTextNode) {
        if (s == null) {
            return;
        }

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (inParsedTextNode) {

                if (c == '<') {
                    last.append("&lt;");
                    continue;
                }
                if (c == '>') {
                    last.append("&gt;");
                    continue;
                }
                if (c == '&') {
                    last.append("&amp;");
                    continue;
                }
            }
            if (c == '@') {
                if (i > 0 && s.charAt(i - 1) == '\\') {
                    last.setCharAt(last.length() - 1, c);
                    continue;
                }
                String bs = s.substring(i + 1);

                int bsidx = bs.indexOf('{');

                if (bsidx >= 0) {
                    String type = bs.substring(0, bsidx);
                    Binding.Converter cvt = Binding.getConverter(type);
                    if (cvt != null) {
                        //if (Binding.isConverter(type)) {
                        String cmd = parseCmd(bs);
                        if (cmd != null) {
                            Binding b = Binding.from(type, cmd, cvt);
                            b.mock(null, this);
                            this.addRender(b);
                            if (this.site != null && this.site.info != null) {
                                this.site.info.allBindings.put(b,
                                        this.loadingFragment.getPath());
                            }

                            parseBinding(removeBindPart(bs));
                            return;
                        }
                    }
                }

            }
            last.append(c);

        }

    }

    public Fragment getLoadingFragment() {
        return loadingFragment;
    }


}
