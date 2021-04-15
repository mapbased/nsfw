/**
 *
 */
package com.mapkc.nsfw.query;

import com.mapkc.nsfw.model.LoadContext;
import com.mapkc.nsfw.model.RenderContext;
import com.mapkc.nsfw.model.Renderable;
import org.jsoup.nodes.Element;

/**
 * @author chy<br>
 *         lmd尚未实现
 */
public class DetailQueryConfig extends QueryConfig {

    // protected long loadTime = System.currentTimeMillis();

    private String componentId;
    // private  boolean hasId=false;
    private Renderable idQuery;


    public DetailQueryConfig() {
        super();
    }

    public DetailQueryConfig(String schemaPath, Class rowClass) {
        super(schemaPath, rowClass);
    }

    @Override
    public void setComponentId(String componentId) {
        this.componentId = componentId;

    }

    protected String lmdFieldName;

    protected void fetchQuery(Element ele, LoadContext lc) {
        String idQueryStr = lc.fetchAttribute(ele, "id-query");
        if (idQueryStr == null) {
            idQueryStr = lc.fetchAttribute(ele, "query");
        }

        if (idQueryStr != null) {
            LoadContext clc = lc.createChild();
            clc.parseBinding(idQueryStr);
            this.idQuery = clc.getRenderable();
        }

        String fixedQueryStr = lc.fetchAttribute(ele, "fixed-query");

        if (fixedQueryStr == null) {
            fixedQueryStr = lc.fetchAttribute(ele, "fixedquery");
        }

        if (fixedQueryStr != null) {
            LoadContext clc = lc.createChild();
            clc.parseBinding(fixedQueryStr);
            this.fixedQuery = clc.getRenderable();
        }
    }

    public String getId(RenderContext rc) {
        if (this.idQuery == null) {
            String kn = this.getSchema(rc).getKeyFieldName();
            String s = rc.param(componentId + "." + kn);
            if (s != null) {
                return s;
            }
            return rc.param(kn);
        }
        return this.idQuery.getRenderValue(rc);
    }

    public String getLmdFieldName() {
        return lmdFieldName;
    }

    @Override
    public QueryResult getQueryResult(RenderContext rc) {
        // switch (this.getSchemaType(rc)) {
        // case MySQL:
        // // return new MysqlQueryResult(this, rc);
        // case Search:
        // // return new SEQueryResult(this, rc);
        //
        // }

        return new DetailResult(this, rc);
    }

}
