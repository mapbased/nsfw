package com.mapkc.nsfw.query;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mapkc.nsfw.model.LoadContext;
import com.mapkc.nsfw.model.RenderContext;
import com.mapkc.nsfw.model.Renderable;
import com.mapkc.nsfw.model.XEnum;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class QuerysDefine {


    public String screenName;
    public String name;
    public String allName;
    //全部的中文名
    @JsonDeserialize(as = DefaultQueryDefine[].class)
    protected QueryDefine[] querys;

    public QuerysDefine() {

    }

    public QuerysDefine(XEnum x) {

        this.name = x.getName();
        this.screenName = x.getScreenName();

    }

    public static List<QuerysDefine> fromJson(String s) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        QuerysDefine[] qd = mapper.readValue(s, QuerysDefine[].class);
        List<QuerysDefine> qds = new ArrayList<>(qd.length);
        for (QuerysDefine q : qd) {
            qds.add(q);
        }
        return qds;

    }

    public static void main(String[] ss) throws IOException {
//		QuerysDefine  qd = QuerysDefine.fromJson("[{\"screenName\":\"haha\",\"name\":\"aa\",\"querys\":" +
//				"[{\"query\":\"as\",\"screenName\":\"dd\"}]}]");

    }

    public void setQuerys(QueryDefine[] querys) {
        this.querys = querys;
    }

    public int size() {
        if (this.querys == null) {
            return 0;
        }
        return this.querys.length;
    }

    public QueryDefine get(int index) {
        if (this.querys == null || index >= this.querys.length) {
            return null;
        }
        return this.querys[index];
    }

    public String getAllName() {
        return this.allName;
    }

    public interface QueryDefine {
        /**
         * @param rc
         * @return SQL where or Search Query
         */
        Object getQuery(RenderContext rc);

        String getScreenName();

    }

    /**
     * 默认的，可以通过json解析配置生成
     */
    public static class DefaultQueryDefine implements QueryDefine {
        Renderable renderableQuery;
        private String screenName;
        private String query;

        @Override
        public Object getQuery(RenderContext rc) {
            if (this.renderableQuery != null)
                return renderableQuery.getRenderValue(rc);
            return query;
        }

        public void setQuery(String query) {
            if (query.indexOf('@') >= 0) {
                this.renderableQuery = LoadContext.getRenderable(query);
            }
            this.query = query;
        }

        @Override

        public String getScreenName() {
            return screenName;
        }

        public void setScreenName(String screenName) {
            this.screenName = screenName;
        }

    }

}
