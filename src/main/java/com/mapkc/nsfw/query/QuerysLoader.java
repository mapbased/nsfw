package com.mapkc.nsfw.query;

import java.util.Map;

public class QuerysLoader {
    Map<String, QueryLoader> fields = new java.util.HashMap<String, QueryLoader>(
            4);

    public QueryLoader getQueryLoader(String query) {

        QueryLoader ql = this.fields.get(query);

        if (ql == null) {
            ql = new QueryLoader();
            this.fields.put(query, ql);
        }
        return ql;
    }

}