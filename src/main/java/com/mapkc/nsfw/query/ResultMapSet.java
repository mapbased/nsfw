package com.mapkc.nsfw.query;

import java.util.Collection;

/**
 * 根据id查出来的数据列表，可以根据id快速检索，
 *
 * @author chy
 */
public interface ResultMapSet {

    ResultRow getRowById(String id);

    Collection<ResultRow> getRows();

}