package com.mapkc.nsfw.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class QueryFields {
    /**
     * schema,Fields list, of self or parent
     */
    Map<String, List<String>> schemaFields = new TreeMap<String, List<String>>();

    public void addSchemaField(String schemaAndField) {
        String[] ss = schemaAndField.split("\\.");
        addSchemaField(ss[0], ss[1]);
    }

    public void addSchemaField(String schema, String field) {
        List<String> list = this.schemaFields.get(schema);
        if (list == null) {
            list = new ArrayList<String>(4);
            this.schemaFields.put(schema, list);
        }
        if (!list.contains(field)) {
            list.add(field);
        }
    }
}