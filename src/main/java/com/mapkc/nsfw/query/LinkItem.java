package com.mapkc.nsfw.query;

/**
 * 用作查询页免得统计，分页等信息
 *
 * @author chy
 */
public interface LinkItem extends java.util.Enumeration<LinkItem> {

    String getLabel();

    String getLink();

    int getCount();

    boolean isSelected();

}
