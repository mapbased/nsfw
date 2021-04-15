package com.mapkc.nsfw.component;

import com.mapkc.nsfw.model.ActionHandler;
import com.mapkc.nsfw.model.LoadContext;
import com.mapkc.nsfw.model.RenderContext;
import com.mapkc.nsfw.model.Renderable;
import com.mapkc.nsfw.query.DetailQueryConfig;
import com.mapkc.nsfw.query.ListQueryConfig;
import com.mapkc.nsfw.query.QueryConfig;
import com.mapkc.nsfw.query.QueryResult;
import com.mapkc.nsfw.util.DynamicClassLoader;
import org.jsoup.nodes.Element;
import org.mvel2.MVEL;

/**
 * Query 控件不回嵌套。如果有嵌套的查询，本Query自身通过插件的机制处理
 *
 * @author chy
 */
public class Query extends Component implements ActionHandler {

      static final Property[] properties = new Property[]{

            new Property.Str("config", "配置"), new Property.Bool("include", "包括Tag"),
            new Property.RenderablePro("emptypage", "为空时跳转到")
    };
    Renderable content;

    QueryConfig queryConfig;

    String config;
    Renderable emptypage;

    boolean include = true;

    @Override
    public void render(RenderContext rc) {

        this.content.render(rc);

    }


    @Override
    public void parseXml(Element ele, LoadContext lc) {
        super.parseProperties(properties, ele, lc);


        try {
            if (this.config == null) {
                this.queryConfig = lc.fetchBooleanAttribute(ele, "isdetail") ? new DetailQueryConfig()
                        : new ListQueryConfig();

            } else {
                /**
                 * config属性是一个类名，或者ActionHandler上面的一个方法或者一个字段。
                 * （注意，是一个方法的意义并不大，返回的对象在Query里面做了引用。一旦初始化，方法不再调用）
                 */

                this.config = this.config.trim();
                if (this.config.indexOf('.') > 0) {
                    this.queryConfig = (QueryConfig) DynamicClassLoader.load(
                            this.config.trim(), lc.site);
                } else {

                    ActionHandler ac = lc.getLoadingFragment().getHandler();
                    if (ac != null) {
                        Object o = MVEL.eval(this.config, ac);
                        if (o instanceof QueryConfig) {
                            this.queryConfig = (QueryConfig) o;
                        }
                    }
                }
                // (QueryConfig) this.getClass()
                // .getClassLoader().loadClass(this.config.trim())
                // .newInstance();
            }
            this.queryConfig.setComponentId(componentId);
            queryConfig.setQueryLoader(lc.getQueryLoader(this.componentId));
            this.queryConfig.init(ele, lc);
            if (!this.include) {
                this.content = lc.createChild().parseElementChildren(ele)
                        .getRenderable();
            } else {
                this.content = lc.createChild().parseElement(ele).getRenderable();
            }

        } catch (Exception e) {
            throw new java.lang.RuntimeException("Cannot load class:" + config,
                    e);
        }

        // this.config.fromHtml(ele);


        // this.content=lc.createChild().p
    }

    @Override
    public void toXml(XmlContext xc) {
        // TODO Auto-generated method stub

    }

    public QueryConfig getQueryConfig() {
        return queryConfig;
    }

    @Override
    public boolean filterAction(RenderContext rc) {


        QueryResult qr = queryConfig.getQueryResult(rc);


        if (qr.isEmpty() && this.emptypage != null) {
            rc.sendNotFound(this.emptypage.getRenderValue(rc));
            // rc.sendRedirect(HttpResponseStatus.NOT_FOUND, this.emptypage);
            return true;
        }


        rc.setVar(this.componentId, qr);

        return false;
    }

    public QueryResult getQueryResult(RenderContext rc) {
        return (QueryResult) rc.v(this.componentId);
    }


}
