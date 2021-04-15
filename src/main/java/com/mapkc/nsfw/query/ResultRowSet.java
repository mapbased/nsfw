package com.mapkc.nsfw.query;

import java.util.List;

public interface ResultRowSet {

    List<ResultRow> getRows();
    /**
     * // * 收集某一列的key做为新的请求参数 // * @param fieldName // * @param resultTobeFill
     * //
     */
    // public void collect(String fieldName,Map<String,ResultRow>
    // resultTobeFill);

}