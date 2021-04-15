package com.mapkc.nsfw.query;

import com.mapkc.nsfw.binding.Binding;
import com.mapkc.nsfw.model.*;
import com.mapkc.nsfw.util.FacetDefine;
import com.mapkc.nsfw.util.Sort;
import com.mapkc.nsfw.util.logging.ESLogger;
import com.mapkc.nsfw.util.logging.Loggers;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 查询配置信息
 *
 * @author chy
 */
public class ListQueryConfig extends QueryConfig {
    final static ESLogger log = Loggers.getLogger(ListQueryConfig.class);

    static public class SEInfo {
        // protected String[] fields;
        //  protected Highlight[] highlights;
        protected FacetDefine[] treeFacetDefines;
        protected FacetDefine[] propertyFacetDefines;
        //  protected Group group;
        protected boolean needTreeParentNodeFilter;

    }

    static public class SQLInfo {
        public Renderable sqlBase;// select fieldlist from table
        /**
         * 在sqlbase或者其他地方存在where的时候，使用countsql来计算分页的条数
         */
        public Renderable countSql;


    }

    private int pageSize = 30;
    private int pageLabelCount = 15;
    protected SEInfo seinfo = new SEInfo();
    protected SQLInfo sqlinfo = new SQLInfo();
    protected com.mapkc.nsfw.util.Sort[] sorts;
    protected String[] sortLables;
    protected Sort[] fixedSorts;
    private Binding splitFieldValue;
    String componentId;

    protected List<QuerysDefine> querysDefines;// =new

    //page
    String p = "p";
    //faceted query
    String fq = "fq";
    //defined query
    String q = "q";

    String sortName = "s";

    /**
     * 关键字查询 title like '%@sql{_p.a}%'
     */

    private Renderable keywordQuery;

    // String qi;
    public ListQueryConfig() {
        super();
    }

    public ListQueryConfig(String schemaPath, Class rowClass) {
        super(schemaPath, rowClass);
    }

    public static final Sort[] parseSorts(String sorts) {
        if (sorts != null) {

            java.util.ArrayList<Sort> a = new java.util.ArrayList<Sort>();
            java.util.Iterator<String> i = com.google.common.base.Splitter
                    .on(',').omitEmptyStrings().trimResults().split(sorts)
                    .iterator();
            while (i.hasNext()) {
                String s = i.next();
                String name = s;
                boolean desc = true;
                int idx = s.indexOf(':');
                if (idx > 0) {
                    name = s.substring(0, idx);
                    desc = !"asc".equalsIgnoreCase(s.substring(idx + 1));
                }
                a.add(new Sort(name, desc));
            }
            return a.toArray(new Sort[a.size()]);

        }
        return null;
    }

    private static Sort[] fetchSorts(Element ele, LoadContext lc, String sname) {
        String sorts = lc.fetchAttribute(ele, sname);
        return parseSorts(sorts);
    }

    public String getSplitFieldValue(RenderContext renderContext) {
        if (this.splitFieldValue != null) {
            return splitFieldValue.getRenderValue(renderContext);
        }
        return renderContext.p("__split_field_value");
    }

    @Override
    public void init(Element ele, LoadContext lc) {

        String sqlb = lc.fetchAttribute(ele, "sqlbase");
        if (sqlb == null) {
            sqlb = lc.fetchAttribute(ele, "basesql");
        }
        if (sqlb != null) {
            this.sqlinfo.sqlBase = lc.getRenderableStr(sqlb);
        }
        String countSql = lc.fetchAttribute(ele, "countsql");
        if (countSql != null) {
            this.sqlinfo.countSql = lc.getRenderableStr(countSql);
        }

        this.pageLabelCount = lc.fetchIntAttribute(ele, "pagelabelcount", 9);
        String splitvalue = lc.fetchAttribute(ele, "splitvalue");
        if (splitvalue != null) {
            this.splitFieldValue = LoadContext.getBinding(splitvalue);
        }


        String keywordquery = lc.fetchAttribute(ele, "keywordquery");
        if (keywordquery == null) {
            keywordquery = lc.fetchAttribute(ele, "keyword-query");
        }
        if (keywordquery != null) {
            this.keywordQuery = LoadContext.getRenderable(keywordquery);
        }

        int ps = lc.fetchIntAttribute(ele, "pagesize", "page-size");
        if (ps > 0 && ps < 10000) {
            this.pageSize = ps;
        }
        this.sorts = fetchSorts(ele, lc, "sort");
        if (this.sorts == null) {
            this.sorts = fetchSorts(ele, lc, "sorts");
        }
        this.sortLables = lc.fetchStringsAttribute(ele, "sortlabels", "sort-labels");
        this.fixedSorts = fetchSorts(ele, lc, "fixedsorts");

        // ///////////
        String facet = lc.fetchAttribute(ele, "facet");
        if (facet != null) {
            String[] ff = facet.split(",");
            FacetDefine[] propertyFacetDefines = new FacetDefine[ff.length];
            for (int i = 0; i < ff.length; i++) {
                String flddesc = ff[i].trim();
                String[] ss = flddesc.split(":");


                propertyFacetDefines[i] = new FacetDefine(ss[0]);
                if (ss.length > 1) {
                    propertyFacetDefines[i].selectedValue = "";
                }
                //Using emptyy string tell SEQueryRReult keep when selected;
            }
            if (this.seinfo == null) {
                this.seinfo = new SEInfo();

            }
            this.seinfo.propertyFacetDefines = propertyFacetDefines;
        }


        Element qde = ele.getElementById(this.componentId + ".qd");
        if (qde != null) {


            try {
                String txt = qde.data();
                if (txt.length() < 1) {
                    txt = qde.text();
                }
                //this.querysDefines =;
                this.add(QuerysDefine.fromJson(txt));

                qde.remove();

            } catch (Exception e) {
                log.error("Query {} cannot create QuerysDefines", e, this.componentId);

            }
        }


        super.init(ele, lc);
    }


    @Override
    public void setComponentId(String componentId) {
        this.p = componentId + ".p";
        this.fq = componentId + ".fq";
        this.q = componentId + ".q";
        this.sortName = componentId + ".s";
        this.componentId = componentId;

    }

    public void setSqlBase(String sqlbase) {
        this.sqlinfo.sqlBase = LoadContext.getRenderable(sqlbase);
    }

    public void setFixedQuery(String fixedQuery) {
        this.fixedQuery = LoadContext.getRenderable(fixedQuery);
    }

    public void setKeywordQuery(String keywordQuery) {
        this.keywordQuery = LoadContext.getRenderable(keywordQuery);
    }


    public List<QuerysDefine> getQuerysDefines() {
        return querysDefines;
    }

    public void add(QuerysDefine qd) {
        if (this.querysDefines == null) {
            this.querysDefines = new ArrayList<QuerysDefine>(3);
        }
        this.querysDefines.add(qd);
    }

    public void add(List<QuerysDefine> qds) {
        if (qds != null) {
            for (QuerysDefine qd : qds) {
                this.add(qd);
            }
        }
    }

    public DataSource getDataSource(RenderContext rc) {
        Schema s = this.getSchema(rc);
        if (s != null) {
            return s.getDataSource();
        }
        return null;

    }


    public String getSqlBase(RenderContext rc) {
        if (this.sqlinfo == null || sqlinfo.sqlBase == null) {
            return null;
        }
        return this.sqlinfo.sqlBase.getRenderValue(rc);
    }

    public String getFixedQueryStr(RenderContext rc) {
        if (fixedQuery == null) {
            return null;
        }
        return fixedQuery.getRenderValue(rc);
    }

    /**
     * 只供搜索引擎使用
     *
     * @param
     * @return
     */
//    public Query getFixedQuery(RenderContext rc) {
//        String s = this.getFixedQueryStr(rc);
//        if (s != null) {
//            return new ExpressionQuery(s);
//        }
//        return null;
//    }
    public void setQuerysDefines(List<QuerysDefine> querysDefines) {
        this.querysDefines = querysDefines;
    }


    /**
     * 每页有多少条记录
     *
     * @return
     */
    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int ps) {
        this.pageSize = ps;
    }

    /**
     * 树状Facet导航定义，目前仅支持一棵树
     *
     * @return
     */
    public FacetDefine[] getTreeFacetDefines() {
        return seinfo.treeFacetDefines;
    }

    /**
     * 属性导航定义
     *
     * @return
     */
    public FacetDefine[] getPropertyFacetDefines(RenderContext rc) {
        return seinfo.propertyFacetDefines;
    }

    /**
     * 需要查询取出的字段
     *
     * @return
     */
    public Map<String, VQConfig> getFieldDefinedQueries() {
        if (this.queryloader != null)
            return this.queryloader.getFieldDefinedQueries();
        return null;
    }

//    public Highlight[] getHighlights() {
//        if (seinfo.highlights == null) {
//
//            Object[] o = this.queryloader.highlights.keySet().toArray();
//            Highlight[] ls = new Highlight[o.length];
//            for (int i = 0; i < ls.length; i++) {
//                ls[i] = new Highlight(o[i].toString());
//                ls[i].fragment_count = 1;
//                ls[i].fragment_size = 350;
//            }
//            seinfo.highlights = ls;
//        }
//        return seinfo.highlights;
//    }
//
//    public void setHighlights(Highlight[] hls) {
//        seinfo.highlights = hls;
//    }

    public Sort[] getSorts() {
        return sorts;
    }

    public void setSorts(Sort[] sorts) {
        this.sorts = sorts;
    }

    public int getPageLabelCount() {
        return pageLabelCount;
    }

    public void setPageLabelCount(int pageLabelCount) {
        this.pageLabelCount = pageLabelCount;
    }

//    public Group getGroup() {
//        return seinfo.group;
//    }

    public boolean needTreeParentNodeFilter() {
        return seinfo.needTreeParentNodeFilter;
    }

    public boolean setNeedTreeParentNodeFilter(boolean need) {
        return seinfo.needTreeParentNodeFilter = need;
    }

    @Override
    public QueryResult getQueryResult(RenderContext rc) {
        switch (this.getSchemaType(rc)) {
            case MySQL:
                return new MysqlQueryResult(this, rc);
            case Search:
                return null;// new SEQueryResult(this, rc);
            case PgCock:
                return new PgCockQueryResult(this, rc);

        }
        return null;
    }

    public Renderable getKeywordQuery() {
        return keywordQuery;
    }


}
