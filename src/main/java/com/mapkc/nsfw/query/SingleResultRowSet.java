package com.mapkc.nsfw.query;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chy on 14-9-15.
 */
public class SingleResultRowSet implements ResultRowSet {
    List<ResultRow> rows = new ArrayList<>(1);

    public SingleResultRowSet(ResultRow row) {
        rows.add(row);
    }

    @Override
    public List<ResultRow> getRows() {
        return rows;
    }
}
