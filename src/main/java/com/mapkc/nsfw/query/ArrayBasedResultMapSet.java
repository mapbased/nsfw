package com.mapkc.nsfw.query;

import java.util.Collection;
import java.util.Map;

/**
 * Not thread safe
 *
 * @author chy
 */
public class ArrayBasedResultMapSet implements ResultMapSet {

    final Map<String, Integer> fieldMap = new java.util.HashMap<String, Integer>();

    final Map<String, ResultRow> ids;

    public ArrayBasedResultMapSet(String[] fields,
                                  final Map<String, ResultRow> ids) {
        this.ids = ids;
        for (int i = 0; i < fields.length; i++) {
            this.fieldMap.put(fields[i], i + 1);
        }
    }

    @Override
    public ResultRow getRowById(String id) {

        return ids.get(id);
    }

    public Collection<ResultRow> getRows() {
        return ids.values();
    }

    public void addResult(String id, Object[] values) {
        ArrayBasedResultRowImpl rr = new ArrayBasedResultRowImpl(fieldMap,
                values);

        this.ids.put(id, rr);
    }

}