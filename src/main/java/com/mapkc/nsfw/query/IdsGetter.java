package com.mapkc.nsfw.query;

import com.mapkc.nsfw.model.Schema;

import java.util.*;

/**
 * Created by chy on 14-9-12.
 * 收集每行的id，便于一起到schema发请求.
 * 注意，多次调用返回的collection可以为同一个对象，里面的数据不同。
 */
public interface IdsGetter {
    Collection<String> collectIds(ResultRow resultRow);

    Map<String, ResultRow> collectMultiRow(List<ResultRow> resultRows);
}

class FieldNameIdsGetter implements IdsGetter {
    final String fieldName;

    public FieldNameIdsGetter(String fieldname) {
        this.fieldName = fieldname;
    }

    @Override
    public Collection<String> collectIds(ResultRow resultRow) {

        Object o = resultRow.getField(fieldName);
        if (o == null) {
            return null;
        }
        List<String> l = new ArrayList<>(1);
        l.add(o.toString());
        return l;
    }

    @Override
    public Map<String, ResultRow> collectMultiRow(List<ResultRow> resultRows) {
        Map<String, ResultRow> keys = new java.util.HashMap<String, ResultRow>();
        for (ResultRow rr : resultRows) {
            Collection<String> ids = collectIds(rr);
            if (ids == null) {
                continue;
            }
            for (String id : ids) {
                if (id != null)
                    keys.put(id, null);
            }


        }
        return keys;
    }

    /**
     * @// TODO: 16/10/24 中间关联表，多对多的情况。没有完成
     */
    class MidTableIdsGetter implements IdsGetter {

        String fieldName;
        Schema schema;
        String joinField;
        String refrenceField;
        String where;
        Object[] conditions;

        @Override
        public Collection<String> collectIds(ResultRow resultRow) {
            Object field = resultRow.getField(fieldName);
            if (field == null) {
                return Collections.EMPTY_LIST;
            }
            if (where == null) {
                where = joinField + "=?";
                conditions = new Object[]{field};
            } else {
                where = joinField + "=? and " + where;
                if (conditions == null) {
                    conditions = new Object[]{field};
                } else {
                    Object[] nconditions = new Object[conditions.length + 1];
                    System.arraycopy(conditions, 0, nconditions, 1, conditions.length);
                    conditions = nconditions;
                    conditions[0] = field;
                }
            }

            return schema.listSingleFieldBySql(String.class, refrenceField, where, conditions);
//            return null;
        }

        @Override
        public Map<String, ResultRow> collectMultiRow(List<ResultRow> resultRows) {

            Map<String, String> keys = new HashMap<>();


            return null;
        }
    }
}