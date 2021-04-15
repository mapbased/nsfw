/**
 *
 */
package com.mapkc.nsfw.query;

import com.mapkc.nsfw.binding.Binding;
import com.mapkc.nsfw.model.*;
import com.mapkc.nsfw.util.VolatileBag;
import org.jsoup.nodes.Element;

import java.lang.reflect.Field;

/**
 * @author chy
 */
public class QueryConfig {

    protected String[] fields;
    protected QueryLoader queryloader;
    protected Renderable fixedQuery;
    // protected String componentId;
    protected VolatileBag<Schema> schemaBag;
    /**
     * 通过指定一个rowclass，来控制row内部的逻辑，实现代码与模版分离
     */
    protected Class rowClass;
    boolean runOnSlave;
    private Renderable schemaRenderable;
    /**
     * 完整的schema路径
     */
    private String schemaPath;

    public QueryConfig() {

    }

    public QueryConfig(String schemaPath, Class rowClass) {
        this.schemaPath = schemaPath;
        this.queryloader = new QueryLoader();
        // this.schemaBag = schemaBag;
        if (rowClass != null) {
            this.rowClass = rowClass;
        }
        if (this.rowClass != null)
            this.initRowClass(rowClass);
    }


    public void setRowClass(Class rowClass) {
        this.rowClass = rowClass;
        if (this.queryloader != null)
            this.initRowClass(rowClass);
    }

    private void initRowClass(Class rowClass) {
        if (this.queryloader == null) {
            throw new RuntimeException("Please set QueryLoader first");
        }
        Field[] flz = rowClass.getDeclaredFields();
        for (Field field : flz) {
            QueryField queryField = field.getAnnotation(QueryField.class);


            if (queryField != null) {
                String fn = queryField.field();
                if (fn.length() == 0) {
                    fn = field.getName();
                }
                if (queryField.schema2().length() > 0) {
                    queryloader.add(fn, queryField.schema(), queryField.schemaField(), queryField.schema2(), queryField.schemaField2());
                } else if (queryField.schema().length() > 0) {
                    queryloader.add(fn, queryField.schema(), queryField.schemaField());
                } else {
                    queryloader.add(fn);
                }

            }

            // if(field.getTarget)
        }
    }

    public void setComponentId(String componentId) {

    }


    @Deprecated  //not safe for single instance usage
    public void setSchema(String schema, RenderContext rc) {
        if (schema != null) {
            this.schemaBag = (VolatileBag) rc.getSite().getXEnumBag(schema);
        }
    }

    protected void fetchQuery(Element ele, LoadContext lc) {
        String fixedQueryStr = lc.fetchAttribute(ele, "query");
        if (fixedQueryStr == null) {
            fixedQueryStr = lc.fetchAttribute(ele, "fixed-query");
        }
        if (fixedQueryStr == null) {
            fixedQueryStr = lc.fetchAttribute(ele, "fixedquery");
        }
        this.runOnSlave = lc.fetchBooleanAttribute(ele, "slave");

        if (fixedQueryStr != null) {
            LoadContext clc = lc.createChild();
            clc.parseBinding(fixedQueryStr);
            this.fixedQuery = clc.getRenderable();
        }
    }

    public void init(QueryConfigXEnum xEnum) {


    }

    public void init(Element ele, LoadContext lc) {
        String schema = lc.fetchAttribute(ele, "schema");

        if (schema != null) {
            if (schema.indexOf('@') >= 0) {
                this.schemaRenderable = LoadContext.getRenderable(schema);
            } else {
                this.schemaBag = (VolatileBag) lc.getSite().getXEnumBag(lc.getPath(schema));
            }

        }
        this.fetchQuery(ele, lc);

        String[] flz = lc.fetchStringsAttribute(ele, "fields");
        if (flz != null) {
            if (this.queryloader != null) {
                for (String s : flz) {
                    queryloader.add(s);
                }
            } else {
                this.fields = flz;
            }
        }

        // this.fields = lc.fetchStringsAttribute(ele, "fields");

    }

    public QueryLoader getQueryLoader() {
        return queryloader;
    }

    public void setQueryLoader(QueryLoader queryLoader) {
        this.queryloader = queryLoader;
        if (this.rowClass != null) {
            this.initRowClass(this.rowClass);
        }
    }

    public String[] getFields() {

        if (fields == null) {
            fields = this.queryloader.getFields();
        }
        return fields;
    }

    final public Schema getSchema(RenderContext rc) {

        if (this.schemaBag != null) {
            Schema sc = this.schemaBag.getValue();
            if (sc != null) {
                return sc;
            }
        }
        if (this.schemaRenderable != null) {
            if (this.schemaRenderable instanceof Binding) {
                return (Schema) ((Binding) this.schemaRenderable).getValue(rc);
            }
            return (Schema) rc.getSite().getXEnum(this.schemaRenderable.getRenderValue(rc));
        }
        if (this.schemaPath != null) {
            this.schemaBag = (VolatileBag) rc.getSite().getXEnumBag(this.schemaPath);
            if (this.schemaBag == null) {
                throw new RuntimeException("Canot find schema:" + schemaPath);
            }
            return this.schemaBag.getValue();
        }


        return null;
    }

    public SchemaType getSchemaType(RenderContext rc) {
        Schema s = this.getSchema(rc);
        if (s != null) {
            return s.getType();
        }
        return SchemaType.MySQL;

    }

    /**
     * 默认返回空，如果需要扩展，返回自己的的QueryResult
     *
     * @param rc
     * @return
     */
    public QueryResult getQueryResult(RenderContext rc) {
        return null;
    }


}
