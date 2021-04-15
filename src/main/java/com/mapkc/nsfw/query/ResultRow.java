package com.mapkc.nsfw.query;

/**
 * 一行数据
 *
 * @author chy
 */
public interface ResultRow {

    Object getField(String fieldName);

    String getHighlight(String fieldname);

    // public void setField(String fieldName,Object obj);
    String getId();
}
