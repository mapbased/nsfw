package com.mapkc.nsfw.query;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by chy on 14-7-22.
 */
public class MapBasedResultMapSet implements ResultMapSet {
    public static class MapbasedResultRow implements ResultRow {

        String id;
        Map<String, Object> values = new HashMap<>(10);

        @Override
        public Object getField(String fieldName) {
            return this.values.get(fieldName);
        }

        @Override
        public String getHighlight(String fieldname) {
            return null;
        }

        @Override
        public String getId() {
            return id;
        }

        public void addField(String name, Object value) {
            this.values.put(name, value);
        }
    }

    Map<String, ResultRow> ids = new HashMap<>();

    public void addField(String id, String fieldName, Object value) {
        MapbasedResultRow r = (MapbasedResultRow) this.ids.get(id);
        if (r == null) {
            r = new MapbasedResultRow();
            ids.put(id, r);
        }
        r.addField(fieldName, value);
    }

    @Override
    public ResultRow getRowById(String id) {
        return ids.get(id);
    }

    @Override
    public Collection<ResultRow> getRows() {
        return ids.values();
    }


}
