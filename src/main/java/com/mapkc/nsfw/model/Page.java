package com.mapkc.nsfw.model;

import com.mapkc.nsfw.component.XmlContext;
import com.mapkc.nsfw.handler.AccessMode;
import com.mapkc.nsfw.input.FormField;
import com.mapkc.nsfw.util.Strings;
import com.mapkc.nsfw.util.logging.ESLogger;
import com.mapkc.nsfw.util.logging.Loggers;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Map;

public class Page extends Fragment implements ReqHandler {

    private final static ESLogger log = Loggers.getLogger(Page.class);
    final private Map<String, RenderGroup> renderGroups = new java.util.HashMap<String, RenderGroup>();
    @FormField(caption = "master path", input = "typeahead<path=/handler/rpcmisc?xtype=page>", msg = "如果不需要，填写null就不会逐级上调")
    private String masterPath;
    @FormField(caption = "排序", msg = "如果小于0，将不在导航中展示")
    private float sort;
    // 只对page有效，其他handler自己处理
    @FormField(caption = "访问模式")
    private AccessMode accessMode;
    @FormField(caption = "注释")
    private String comment;
    @FormField(caption = "权限")
    private String privilege;
    @FormField(caption = "测试参数", msg = "url请求参数，用来自动测试 比如:id=123&name=abc")
    private String testParam;

    public AccessMode getAccessMode() {
        return accessMode;
    }

    public String getComment() {
        return comment;
    }

    @Override
    public void handle(RenderContext rc) {

        rc.setPage(this);


        if (accessMode != null) {
            if (accessMode != AccessMode.Public) {
                int uid = rc.getUserIdAsInt();
                if (uid <= 0) {
                    rc.sendUnauthoried();
                    return;
                }
                if (accessMode == AccessMode.Admin) {
                    if (!rc.getSc().isAdmin(rc)) {
                        rc.sendRedirect("/errors/needadmin.html?from=" + Strings.encodeURIPath(rc.getUri()));
                        return;
                    }
                }
                if (!rc.can(this.privilege)) {
                    rc.sendError(HttpResponseStatus.FORBIDDEN);
                    return;
                }
            }
        }

        if (this.doActions(rc)) {
            return;
        }

        this.render(rc);
        rc.finish();

    }

    protected String defaultIcon() {
        return "fa  fa-file-text";
    }

    @Override
    public boolean doActions(RenderContext rc) {

        /*
         * TODO: 次序关系，到底master先调用还是本页先调用. master先调用的话，可以把权限校验等丢到master上，
         * 但一旦调用，master渲染需要的数据都要准备
         */
        Page master = this.getMasterPage(rc);
        if (master != null) {

            if (master.doActions(rc)) {
                return true;
            }
        }

        try {
            if (super.doActions(rc)) {
                return true;
            }

        } catch (Exception e) {

            log.error(this.getId(), e);
            rc.sendServerError(e);
            return true;

        }

        return false;

    }

    public String getPrivilege() {
        return privilege;
    }

    @Override
    public void parseXml(Element ele, LoadContext lc) {
        Element ec = ele.children().size() == 0 ? null : ele.child(0);
        String mp = this.attr("masterPath");

        if (mp != null && mp.trim().equals("")) {
            mp = null;
        }
        if ((mp == null) && ec != null) {
            mp = lc.fetchAttribute(ec, "master");
        }

        // this.masterPath = mp;
        if (mp == null) {
            masterPath = Site.getMasterPath(this.getPath());
            // this.masterPagebag=lc.site.getMasterFragmentBag(path);
        } else if (mp.equalsIgnoreCase("null") || mp.equalsIgnoreCase("none")
                || mp.equalsIgnoreCase("")

        ) {
            masterPath = null;
        } else if (!this.masterPath.startsWith("/")) {
            this.masterPath = lc.getPath(this.masterPath);
        }

        // this.caption = lc.fetchAttribute(ele, "caption");
        // this.order = lc.fetchIntAttribute(ele, "order");
        // ele.getElementsByAttributeValue("jid"," $content$");

        Elements es = ele.getElementsByAttributeValue("jid", " $content$");// ele.selectNodes("//*[@jid=\"$content$\"]");

        if (es == null || es.size() == 0) {
            lc.parseElementChildren(ele);
            String name = ec == null ? null : lc.fetchAttribute(ec, "name");
            if (name == null) {
                this.mainRenderGroup = lc.getRenderGroup();
            } else {
                this.renderGroups.put(name, lc.getRenderGroup());
            }

        } else {
            // TODO
            for (Element e : es) {

                String name = lc.fetchAttribute(e, "name");
                LoadContext clc = lc.createChild();
                clc.parseElementChildren(e);
                if (name == null) {
                    this.mainRenderGroup = clc.getRenderGroup();
                } else {
                    this.renderGroups.put(name, clc.getRenderGroup());
                }

            }
        }

        // this.components = lc.components;
        this.headables = lc.getHeadables();

    }

    public Page getMasterPage(RenderContext rc) {

        if (this.masterPath == null) {
            return null;
        }

        Fragment f = rc.getSite().getFragment(this.masterPath);
        if (f == null) {
            return null;
        }
        return f.getPage();
    }

    public RenderGroup getRenderGroup(String name) {
        if (name == null) {
            return this.mainRenderGroup;
        }
        return this.renderGroups.get(name);
    }

    @Override
    public void render(RenderContext rc) {

        this.masterRender(rc);
    }

    protected void masterRender(RenderContext rc) {

        Page master = this.getMasterPage(rc);
        if (master != null) {
            // / Page old=rc.currentMasterClient;
            rc.addMasterClient(this);
            master.masterRender(rc);
            // rc.currentMasterClient=old;
        } else {

            if (this.mainRenderGroup == null) {
                rc.write("Error,path:" + rc.getPath());
            } else {
                rc.setCurrentFragment(this);

                this.mainRenderGroup.render(rc);
            }
        }

    }

    @Override
    public void toXml(XmlContext xc) {
        xc.page = this;

        StringBuilder sb = xc.sb;
        // sb.append("<div caption=\"").append(this.caption).append("\" order=\"")
        // .append(this.order);
        if (this.masterPath != null) {
            sb.append("\" master=\"").append(this.masterPath);
        }
        sb.append("\" >\n");
        if (this.mainRenderGroup != null) {
            sb.append("<div jid=\"$content$\">\n");
            this.mainRenderGroup.toXml(xc);
            sb.append("</div>\n");
        }

        for (Map.Entry<String, RenderGroup> me : this.renderGroups.entrySet()) {

            sb.append("<div jid=\"$content$\" name=\"" + me.getKey() + "\">\n");
            me.getValue().toXml(xc);
            sb.append("</div>\n");
        }

        sb.append("</div>\n");

    }

    @Override
    public float getSort() {
        return sort;
    }

    @Override
    public void setSort(float floatValue) {
        sort = floatValue;
    }

    @Override
    public Page getPage() {
        return this;
    }

    public String getTestParam() {
        return testParam;
    }

    // @Override
    // public XEnumType getXEnumType() {
    // return XEnumType.Page;
    // }

}
