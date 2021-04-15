package com.mapkc.nsfw.query;

import java.util.Map;

public class ArrayBasedResultRowImpl implements ResultRow {
    /**
     *
     */
    private final Map<String, Integer> fieldMap;
    final Object[] values;

    /**
     * @param fieldMap
     * @param values
     */
    ArrayBasedResultRowImpl(Map<String, Integer> fieldMap, Object[] values) {
        this.fieldMap = fieldMap;
        this.values = values;

    }

    @Override
    public Object getField(String fieldName) {
        Integer i = this.fieldMap.get(fieldName);
        if (i != null) {
            return values[i];
        }
        return null;
    }

    @Override
    public String getHighlight(String fieldname) {
        // TODO Auto-generated method stub
        Object o = this.getField(fieldname);
        if (o == null) {
            return null;
        }
        return o.toString();
    }

    @Override
    public String getId() {

        return String.valueOf(this.values[0]);
    }

}