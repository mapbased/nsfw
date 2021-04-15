package com.mapkc.nsfw.model;

import com.mapkc.nsfw.query.ResultMapSet;
import com.mapkc.nsfw.query.ResultRow;

import java.util.Map;

public class SchemaAccessFlink extends SchemaAccess {
    /**
     * 返回的结果装到ids里面，数组下标和fields一致
     *
     * @param fields
     * @param ids
     * @param schema
     */
    @Override
    public ResultMapSet load(String[] fields, Map<String, ResultRow> ids, Schema schema) {
        return null;
    }
}
