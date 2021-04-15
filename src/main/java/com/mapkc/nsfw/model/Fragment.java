package com.mapkc.nsfw.model;

import com.mapkc.nsfw.component.Component;
import com.mapkc.nsfw.component.Import;
import com.mapkc.nsfw.component.Query;
import com.mapkc.nsfw.component.XmlContext;
import com.mapkc.nsfw.input.FormField;
import com.mapkc.nsfw.input.FormModelInfo;
import com.mapkc.nsfw.util.VolatileBag;
import com.mapkc.nsfw.util.logging.ESLogger;
import com.mapkc.nsfw.util.logging.Loggers;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@FormModelInfo(caption = "Fragment")
public class Fragment extends XEnum implements Xmlable {
    private final static ESLogger log = Loggers.getLogger(Fragment.class);
    public List<Element> designElements;
    protected RenderGroup mainRenderGroup;
    @FormField(caption = "Handler")
    protected ActionHandler handler;
    protected Document designDoc;
    // Map<String, Component> components;
    ActionHandler[] headables;
    @FormField(caption = "Content", input = "code", sort = 10000)
    String content;

    public Page getPage() {
        return null;
    }

    public Component getComponent(String id) {
        if (this.headables == null) {
            return null;
        }
        for (ActionHandler a : this.headables) {
            if (a instanceof Component) {
                Component q = (Component) a;
                if (id.equals(q.getComponentId())) {
                    return q;
                }
            }
        }

        return null;
    }

    protected String defaultIcon() {
        return "fa  fa-file";
    }


    public Query getQuery(String id) {
        if (this.headables == null) {
            return null;
        }
        for (ActionHandler a : this.headables) {
            if (a instanceof Query) {
                Query q = (Query) a;
                if (id.equals(q.getComponentId())) {
                    return q;
                }
            }
        }
        return null;

    }


    public String getPath() {
        return this.getId();
    }

    // @Override
    // public XEnumType getXEnumType() {
    // return XEnumType.Fragment;
    // }

    public boolean doActions(RenderContext rc) {
        ActionHandler pageh = this.getHandler();
        if (pageh != null && pageh.filterAction(rc)) {
            return true;
        }
        if (this.headables != null) {
            for (ActionHandler h : this.headables) {

                if (h.filterAction(rc)) {
                    return true;
                }
            }
        }
        return false;
    }

    public ActionHandler getHandler() {
        Object obj = objAttributes.get(GroovyOBJ);
        if (obj instanceof ActionHandler) {
            return (ActionHandler) obj;
        }

        VolatileBag<XEnum> xEnumVolatileBag = this.items.get(this.name + ".groovy");
        if (xEnumVolatileBag != null) {
            Object v = xEnumVolatileBag.value;
            if (v instanceof GroovyCode) {
                Object o = ((GroovyCode) v).getGroovyObj();
                if (o instanceof ActionHandler) {
                    return (ActionHandler) o;
                }
            }
            log.warn("Has groovy config but not used:{}", this.getId());
        }
        return handler;
    }


    public void render(RenderContext rc) {
        Fragment old = rc.getCurrentFragment();
        rc.setCurrentFragment(this);

        mainRenderGroup.render(rc);
        rc.setCurrentFragment(old);
    }

    final private void parseSrc(String src, LoadContext lc) {


        boolean designMode = lc.site.getConfig("design-mode", "false").equalsIgnoreCase("true");
        if (designMode) {
            this.designElements = new ArrayList<>();
        }
        Document doc;
        if (src == null) {
            src = "";
        }

        //TODO switch design mode
        if (src.startsWith("<!DOCTYPE") || src.startsWith("<html")) {
            doc = Parser.parse(src, "");
            if (designMode) {
                this.designDoc = doc;
            }
            this.parseXml(doc, lc);
        } else {
            doc = Parser.parseBodyFragment(src, "");

            if (designMode) {
                this.designDoc = doc;
            }
            this.parseXml(doc.body(), lc);
        }


    }

    @Override
    public void parseXml(Element ele, LoadContext lc) {

        Elements ns = ele.getElementsByAttributeValue("jid", "$content$");
        // Element n=ns.getTarget(0);
        // (Element) ele.selectSingleNode("//*[@jid=\"$content$\"]");
        // System.out.println(doc.getDocType().asXML());
        if (ns.size() > 0) {
            lc.parseElementChildren(ns.get(0));
        } else {

            lc.parseElementChildren(ele);
        }
        this.mainRenderGroup = lc.getRenderGroup();
        // @need synchronized map?
        // this.components = lc.components;
        this.headables = lc.getHeadables();

    }

    @Override
    public void toXml(XmlContext xc) {
        StringBuilder sb = xc.sb;
        sb.append("<div jid=\"$content$\" >");
        this.mainRenderGroup.toXml(xc);
        sb.append("</div>");

    }

    @Override
    public void clean() {
        //this.content = null;
        // TODO Auto-generated method stub

    }

    @Override
    protected void init(Site site) {

        // this.content = this.getAttribute("content");
        // String data = getAttribute(XEnum.KnownAttributes.content.name());//
        // site.getSiteStore().getContent(path);
        // if (data == null) {
        // return;
        // }


        if (this.handler == null) {
            this.handler = this.loadActionHandler(site);
        }
//        else {
//            this.handlerBag = (VolatileBag) site.getXEnumBag(this.getId() + "/" + this.getName() + ".groovy");
//        }
        LoadContext lc = new LoadContext(site, this);

        this.parseSrc(content, lc);
        this.clean();

    }

    public Set<Fragment> getUsage(Site site) {
        Set<Fragment> fl = new HashSet<>();
        for (VolatileBag<XEnum> xb : site.getEnums().values()) {
            XEnum x = xb.getValue();
            if (x instanceof Fragment) {
                Fragment f = (Fragment) x;
                if (f == this) {
                    continue;
                }
                if (f.headables == null) {
                    continue;
                }
                for (ActionHandler h : f.headables) {
                    if (h instanceof Import) {
                        Import i = (Import) h;
                        if (i.getFragmentPath().equals(this.getId())) {
                            fl.add(f);
                        }
                    }
                }


            }

        }
        return fl;
    }

    protected ActionHandler loadActionHandler(Site site) {


        String id = this.getId();
        Object o = site.getPathObject(id);
        if (o instanceof ActionHandler) {
            return (ActionHandler) o;
        }
        if (o != null) {
            log.warn("class is loaded,but not type of ActionHandler:{} for {}",
                    o.getClass().getName(), id);
        }


        return null;
    }


    // Fragment 没有必要响应请求，如果ajax需要动态内容，可以用rpc获取
    // @Override
    // public void handle(RenderContext rc) {
    // if (this.doActions(rc)) {
    // return;
    // }
    // this.render(rc);
    // rc.finish();
    //
    // }
    protected boolean hasHandler() {
        return true;
    }
}
