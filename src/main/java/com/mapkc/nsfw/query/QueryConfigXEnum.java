package com.mapkc.nsfw.query;

import com.mapkc.nsfw.input.FormField;
import com.mapkc.nsfw.model.Site;
import com.mapkc.nsfw.model.XEnum;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;

/**
 * Created by chy on 14/12/25.
 */
public class QueryConfigXEnum extends XEnum {

    @Override
    protected String defaultIcon() {
        return "fa   fa-puzzle-piece";
    }

    @FormField(input = "checkbox", caption = "详细页面", defaultValue = "false")
    private boolean detail;
    @FormField(input = "text", caption = "主键表达式")
    private String idQuery;

    @FormField(caption = "Schema", input = "typeahead<path=/handler/rpcmisc?xtype=schema>")
    private String schema;
    @FormField(input = "text", caption = "Fixed Query")
    private String fixedQuery;

    @FormField(input = "text", caption = "SQL Base")
    private String sqlBase;
    @FormField(input = "text", caption = "Count SQL")

    private String countSql;
    @FormField(input = "text<type=number>", caption = "Page Label Count")
    private int pageLableCount;
    @FormField(input = "text", caption = "Keyword Query")

    private String keywordQuery;
    @FormField(input = "text<type=number>", caption = "Page Size")
    private int pageSize;
    private String sorts;
    private String fixedSorts;




    private QueryConfig createQueryConfig(QueryLoader queryLoader){
        QueryConfig config;
        if(this.detail){
            DetailQueryConfig queryConfig=new DetailQueryConfig();
            config=queryConfig;

        }else{
            ListQueryConfig queryConfig=new ListQueryConfig();
            config=queryConfig;


        }
        config.setQueryLoader(queryLoader);
        //config.setSchema();
        return config;
    }

   Cache<QueryLoader, QueryConfig> queryConfigs= CacheBuilder.newBuilder().weakKeys().build(new CacheLoader<QueryLoader, QueryConfig>() {
       @Override
       public QueryConfig load(QueryLoader queryLoader) throws Exception {
           return createQueryConfig(queryLoader);
       }
   });

    public QueryConfig getQueryConfig(QueryLoader queryLoader) {
        //return this.queryConfig;
        return this.queryConfigs.getIfPresent(queryLoader);
    }
}
