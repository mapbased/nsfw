package com.mapkc.nsfw.query;

import java.util.List;
import java.util.Map;

public class ArrayBaseResultRowSet implements ResultRowSet {

    final Map<String, Integer> fieldMap = new java.util.HashMap<String, Integer>();

    final List<ResultRow> rows = new java.util.LinkedList<ResultRow>();

    public ArrayBaseResultRowSet(String[] fields) {

        for (int i = 0; i < fields.length; i++) {
            this.fieldMap.put(fields[i], i);
        }
    }

    @Override
    public List<ResultRow> getRows() {
        return rows;
    }

    public void addRow(Object[] values) {
        rows.add(new ArrayBasedResultRowImpl(this.fieldMap, values));
    }

}