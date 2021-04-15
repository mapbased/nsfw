//package com.mapkc.nsfw.query;
//
//import com.mapkc.nsfw.model.RenderContext;
//import com.mapkc.nsfw.model.Schema;
//import com.mapkc.nsfw.model.SchemaField;
//import com.mapkc.nsfw.model.Site;
//import com.mapkc.nsfw.util.ReflectExt;
//import com.mapkc.nsfw.util.Strings;
//import com.mapkc.nsfw.util.TagSplitter;
//import com.mapkc.nsfw.util.logging.ESLogger;
//import com.mapkc.nsfw.util.logging.Loggers;
////import org.apache.hadoop.hbase.security.User;
//
//import javax.tools.ToolProvider;
//import java.lang.reflect.Field;
//import java.util.*;
//
///**
// * Created by chy on 15/1/31.
// */
//public class QueryInfo<T> {
//    final static ESLogger log = Loggers.getLogger(QueryInfo.class);
//    Class<T> tClass;
//    Map<String, List<String>> schemaFields = new HashMap<>();
//    Map<String, List<String>> schemaIDFields = new HashMap<>();
//    Map<String/*schema*/, QueryFields> secondLevelSchemaIdFields = new TreeMap<>();
//
//
//    private static final String[] EMPTY_STRA = new String[]{};
//    // private final Site site;
//    private List<QueryTypeHelper> queryTypeHelpers = new ArrayList<>(2);
//
//    private Map<Class, List<FieldHelper>> fieldHelperMap = new HashMap<>();
//
//    private Map<Class, QueryInfo> subQueryInfos;
//
//    private void addSubQueryInfo(Class c) {
//        if (this.subQueryInfos == null) {
//            this.subQueryInfos = new HashMap<>();
//        }
//        this.subQueryInfos.put(c, new QueryInfo(c));
//    }
//
//
//    private static class FieldHelper {
//        final Field field;
//        final QueryField queryField;
//        String fieldName;
//
//        FieldHelper(Field field) {
//            this.field = field;
//            this.queryField = field.getAnnotation(QueryField.class);
//            this.field.setAccessible(true);
//            // this.queryField = queryField;
//            fieldName = field.getName();
//            if (queryField.field().length() > 0) {
//
//                fieldName = queryField.field();
//
//            }
//        }
//
//
//    }
//
//
//    private static class Context {
//        // ArrayBaseResultRowSet arrayBaseResultRowSet;
//        ResultRow currentRow;
//        Map<String, ResultMapSet> wrappedData;
//        Map<String, ResultMapSet> secondLevelWrappedData;
//
//        Schema schema;
//        RenderContext rc;
//
//
//        public java.lang.Object getTarget(String field) {
//            return currentRow.getField(field);
//        }
//
//        public java.lang.Object getMapped(String field) {
//            java.lang.Object o = currentRow.getField(field);
//            if (o == null) {
//                return null;
//            }
//            String os = o.toString().trim();
//            return mapValueCheckTag(os, schema.getField(field));
//
//
//        }
//
//        private String mapValueCheckTag(String os, SchemaField schemaField) {
//            if (os == null) {
//                return null;
//            }
//            if (os.length() == 0) {
//                return "";
//            }
//            if (os.indexOf(' ') > 0) {
//                StringBuilder stringBuilder = new StringBuilder();
//                //  SchemaField schemaField = schema.getField(field);
//
////                TagSplitter.split(os).forEach(x -> {
////                    stringBuilder.append(schemaField.mapValue(x, rc)).append(" ");
////                });
//
//                for(String x: TagSplitter.split(os)){
//                    stringBuilder.append(schemaField.mapValue(x, rc)).append(" ");
//                }
//                if (stringBuilder.length() > 0) {
//                    stringBuilder.setLength(stringBuilder.length() - 1);
//                }
//                return stringBuilder.toString();
//            } else
//                return schemaField.mapValue(os, rc);
//        }
//
//
//        public java.lang.Object getTarget(String field, String schema, String schemaField) {
//            java.lang.Object o = this.getTarget(field);
//            if (o == null) {
//                return null;
//            }
//
//            return this.getByValue(o.toString(), schema, schemaField);
//        }
//
//        public Object getByValue(String fieldValue, String schema, String schemaField) {
//            ResultMapSet resultMapSet = this.wrappedData.getTarget(schema);
//            if (resultMapSet == null) {
//                return null;
//            }
//            ResultRow resultRow = resultMapSet.getRowById(fieldValue);
//            if (resultRow != null) {
//                return resultRow.getField(schemaField);
//            }
//            return null;
//
//        }
//
//        public Object getMappedByValue(String fieldValue, String schema, String schemaField) {
//            java.lang.Object o = this.getByValue(fieldValue, schema, schemaField);
//            if (o == null) {
//                return null;
//            }
//
//            Schema sch2 = rc.getSite().getSchema(schema);
//            //  return sch2.getField(schemaField).mapValue(o.toString(), rc);
//
//            return mapValueCheckTag(o.toString(), sch2.getField(schemaField));
//
//        }
//
//        public java.lang.Object getMapped(String field, String schema, String schemaField) {
//            java.lang.Object o = this.getTarget(field, schema, schemaField);
//            if (o == null) {
//                return null;
//            }
//
//            Schema sch2 = rc.getSite().getSchema(schema);
//            // return sch2.getField(schemaField).mapValue(o.toString(), rc);
//
//            return mapValueCheckTag(o.toString(), sch2.getField(schemaField));
//
//
//        }
//
//        public java.lang.Object getTarget(String field, String schema, String schemaField, String schema2, String schemaField2) {
//
//            java.lang.Object o = this.getTarget(field, schema, schemaField);
//            if (o == null) {
//                return null;
//            }
//            return this.getByValueSecondLevel(o.toString(), schema2, schemaField2);
//
//        }
//
//        public java.lang.Object getByValueSecondLevel(String fieldValue, String schema2, String schemaField2) {
//            ResultMapSet resultMapSet = secondLevelWrappedData.getTarget(schema2);
//            if (resultMapSet == null) {
//                return null;
//            }
//            ResultRow resultRow = resultMapSet.getRowById(fieldValue);
//            if (resultRow != null) {
//                return resultRow.getField(schemaField2);
//            }
//            return null;
//        }
//
//
//        public java.lang.Object getMapped(String field, String schema, String schemaField, String schema2, String schemaField2) {
//            java.lang.Object o = this.getTarget(field, schema, schemaField, schema2, schemaField2);
//            if (o == null) {
//                return null;
//            }
//            Schema sch2 = rc.getSite().getSchema(schema2);
//            //return sch2.getField(schemaField2).mapValue(o.toString(), rc);
//
//            return mapValueCheckTag(o.toString(), sch2.getField(schemaField2));
//
//
//        }
//
//        public java.lang.Object getMappedByValueSecondLevel(String fieldValue, String schema2, String schemaField2) {
//            java.lang.Object o = this.getByValueSecondLevel(fieldValue, schema2, schemaField2);
//            if (o == null) {
//                return null;
//            }
//            Schema sch2 = rc.getSite().getSchema(schema2);
//            //  return sch2.getField(schemaField2).mapValue(o.toString(), rc);
//            return mapValueCheckTag(o.toString(), sch2.getField(schemaField2));
//
//        }
//
//
//    }
//
//
//    private static class QueryTypeHelper {
//        // Map<String,Schema> valueMap=new HashMap<>(5);
//        final QueryType queryType;
//        final String valueFieldName;
//        final String typeFieldName;
//        Field field;
//
//        public QueryTypeHelper(QueryType queryType, String valueFieldName, String typeFieldName, Field field) {
//            this.queryType = queryType;
//            this.valueFieldName = valueFieldName;
//            this.typeFieldName = typeFieldName;
//            this.field = field;
//            field.setAccessible(true);
//
//        }
//
//    }
//
//    public QueryInfo(Class<T> tClass) {
//
//        // tClass= T.class;
//        this.tClass = tClass;
//        //  this.site = site;
//
//        this.init(tClass, "", tClass);
//    }
//
//
//    public List<T> query(Schema schema, Map<String, java.lang.Object> conditions,
//                         String whereappend, List<Object> appendConditions,
//                         RenderContext rc) {
//        return this.query(schema, conditions, whereappend, appendConditions.toArray(), rc);
//
//    }
//
//    public List<T> query(Schema schema, Map<String, java.lang.Object> conditions,
//
//                         RenderContext rc) {
//
//        return this.query(schema, conditions, null, Collections.EMPTY_LIST, rc);
//    }
//
//    public List<T> query(Schema schema, Map<String, java.lang.Object> conditions,
//                         String whereappend, Object[] appendConditions,
//                         RenderContext rc) {
//
//        StringBuilder stringBuilder = new StringBuilder();
//        List<java.lang.Object> args = new ArrayList<>(conditions.size());
//        if (conditions != null && conditions.size() > 0) {
////            conditions.forEach((k, v) -> {
////                stringBuilder.append(k).append("=? and ");
////                args.add(v);
////
////            });
////
//            for(Map.Entry<String, java.lang.Object> e:conditions.entrySet()){
//                stringBuilder.append(e.getKey()).append("=? and ");
//                args.add(e.getValue());
//            }
//        }
////        String s = stringBuilder.toString();
////        if (s.length()>0) {
////
////            s = s.substring(0, s.length() - 4);
////        }
//        if (whereappend != null && whereappend.length() > 0) {
//
//            String lowappend = whereappend.toLowerCase().trim();
//            if (lowappend.startsWith("order ") || lowappend.startsWith("limit ")) {
//                if (stringBuilder.length() > 0) {
//                    stringBuilder.setLength(stringBuilder.length() - 4);
//                }
//
//            }
//
//            stringBuilder.append(whereappend);
//            for (java.lang.Object o : appendConditions) {
//                args.add(o);
//            }
//        } else {
//            if (stringBuilder.length() > 0) {
//                stringBuilder.setLength(stringBuilder.length() - 4);
//            }
//        }
//
//        return query(schema, stringBuilder.toString(), args.toArray(), rc);
//
//
//    }
//
//
//    public T getTarget(Schema schema, Object id, RenderContext rc) {
//        return getTarget(schema, id, null, null, rc);
//
//    }
//
//    public T getTarget(Schema schema, Object id, String where, Object[] conditions, RenderContext rc) {
//        StringBuilder stringBuilder = new StringBuilder();
//        stringBuilder.append(schema.getKeyFieldName()).append("=?");
//        if (where != null && where.length() > 0) {
//            stringBuilder.append(" and ").append(where);
//        }
//        Object[] ps;
//        if (conditions == null || conditions.length == 0) {
//            ps = new Object[]{id};
//        } else {
//            List list = new ArrayList<>(3);
//            list.add(id);
//            for (Object o : conditions) {
//                list.add(o);
//            }
//            ps = list.toArray();
//        }
//        List<T> ret = this.query(schema, stringBuilder.toString(), ps, rc);
//
//        if (ret != null && ret.size() > 0) {
//            if (ret.size() > 1) {
//                log.warn("Get from schema:{},id:{},result count >1, is{}", schema.getId(), id, ret.size());
//            }
//            return ret.getTarget(0);
//        }
//        return null;
//
//    }
//
//    public List<T> query(Schema schema, String where, Object[] conditions, RenderContext rc) {
//        Site site = rc.getSite();
//        String[] flz = schemaFields.getTarget("").toArray(EMPTY_STRA);
//
//        //获取直接字段的值
//        List<Object[]> values = schema.listObjectBySql(flz, Object[].class, where, conditions);
//
//        ArrayBaseResultRowSet arrayBaseResultRowSet = new ArrayBaseResultRowSet(flz);
//      //  values.forEach(row -> arrayBaseResultRowSet.addRow(row));
//        for(Object [] row:values){
//            arrayBaseResultRowSet.addRow(row);
//        }
//
//        //获取间接字段schema对应的id
//        Map<String, Map<String, ResultRow>> schemaIdValues = new HashMap<>();
//
//
//        arrayBaseResultRowSet.getRows().forEach(r -> {
//            //根据特别type的值到不同的schema中去获取值，典型使用场景是动态
//
//            this.queryTypeHelpers.forEach(qh -> {
//                String typeValue = String.valueOf(r.getField(qh.typeFieldName));
//                String[] vs = qh.queryType.values();
//                for (int i = 0; i < vs.length; i++) {
//                    if (vs[i].equals(typeValue)) {
//
//                        String sch = qh.queryType.schemas()[i];
//                        java.lang.Object o = r.getField(qh.valueFieldName);
//                        if (o != null) {
//                            String os = o.toString();
//
//                            if (!os.equals("")) {
//                                if (os.indexOf(' ') > 0) {
//
//                                    for (String id : TagSplitter.split(os)) {
//                                        addMapValue(sch, id, schemaIdValues);
//                                    }
//
//                                } else {
//                                    addMapValue(sch, os, schemaIdValues);
//                                }
//                            }
//
//                        }
//                        break;
//
//
//                    }
//                }
//
//
//            });
//
//            this.schemaIDFields.forEach((k, v) -> {
//                v.forEach(fv -> {
//
//                    java.lang.Object o = r.getField(fv);
//                    if (o != null) {
//                        String os = o.toString();
//                        if (!os.equals("")) {
//                            if (os.indexOf(' ') > 0) {
//
//                                for (String id : TagSplitter.split(os)) {
//                                    addMapValue(k, id, schemaIdValues);
//                                }
//
//                            } else {
//                                addMapValue(k, os, schemaIdValues);
//                            }
//                        }
//
//
//                    }
//
//                });
//
//
//            });
//
//        });
//
//        //获取间接字段的值
//        Map<String, ResultMapSet> wrappedData = new HashMap<>();
//        schemaIdValues.forEach((k, v) -> {
//            if (v.size() > 0) {
//                Schema sch = site.getSchema(k);
//                if (sch == null) {
//                    throw new RuntimeException("Cannot find schema:" + k);
//                }
//
//                List<String> stringList = schemaFields.getTarget(k);
//                if (stringList != null) {
//
//                    ResultMapSet resultMapSet = sch.load(stringList.toArray(EMPTY_STRA), v);
//                    wrappedData.put(k, resultMapSet);
//                }
//            }
//
//        });
//        //获取二级间接字段
//        Map<String, ResultMapSet> secondLevelWrappedData = new HashMap<>();
//        if (secondLevelSchemaIdFields.size() > 0) {
//
//            secondLevelSchemaIdFields.forEach((k, v) -> {
//                Schema sch = site.getSchema(k);
//
//                Map<String, ResultRow> ids = new java.util.HashMap<String, ResultRow>();
//
//                v.schemaFields.forEach((schema1, fldlist) -> {
//
//                    ResultMapSet rms = wrappedData.getTarget(schema1);
//                    if (rms != null) {
//                        rms.getRows().forEach(row -> {
//                            if (row != null) {
//                                fldlist.forEach(afld -> {
//                                    java.lang.Object object = row.getField(afld);
//                                    if (object != null) {
//
//                                        String os = object.toString();
//
//
//                                        if (os.indexOf(' ') > 0) {
//
//                                            for (String id : TagSplitter.split(os)) {
//                                                ids.put(id, null);
//
//                                            }
//
//                                        } else {
//                                            ids.put(os, null);
//                                            // addMapValue(k, os, schemaIdValues);
//                                        }
//
//
//                                    }
//                                });
//                            }
//                        });
//                    }
//
//                });
//
//                secondLevelWrappedData.put(k,
//                        sch.load(schemaFields.getTarget(k).toArray(EMPTY_STRA), ids));
//
//
//            });
//
//
//        }
//
//
//        //设置直接字段和间接字段的值，并且准备三级字段
//
//
//        List<T> ret = new ArrayList<>(values.size());
//        Context context = new Context();
//        context.wrappedData = wrappedData;
//        context.schema = schema;
//        context.secondLevelWrappedData = secondLevelWrappedData;
//        context.rc = rc;
//
//        arrayBaseResultRowSet.getRows().forEach(r -> {
//
//            context.currentRow = r;
//
//
//            try {
//                T t = tClass.newInstance();
//                ret.add(t);
//
//                for (QueryTypeHelper qt : queryTypeHelpers) {
//                    Object typevalue = context.getTarget(qt.typeFieldName);
//
//                    if (typevalue == null) {
//                        continue;
//                    }
//                    Object fieldvalue = context.getTarget(qt.valueFieldName);
//                    if (fieldvalue == null) {
//                        continue;
//                    }
//
//                    String sv = typevalue.toString();
//
//                    String[] vs = qt.queryType.values();
//                    for (int i = 0; i < vs.length; i++) {
//                        if (sv.equals(vs[i])) {
//
//                            Object v = this.createInnerObj(qt.queryType.types()[i], context, qt.queryType.schemas()[i], fieldvalue.toString());
//                            qt.field.set(t, v);
//
//
//                            break;
//                        }
//
//
//                    }
//                }
//
//
//                fieldHelperMap.getTarget(tClass).forEach(fh -> {
//                    java.lang.Object value = null;
//
//                    QueryField queryField = fh.queryField;
//                    // if(fh.field.getTarget)
//                    if (queryField.schemaField2().length() > 0) {
//                        if (queryField.mapped()) {
//                            value = context.getMapped(fh.fieldName, queryField.schema(), queryField.schemaField(), queryField.schema2(), queryField.schemaField2());
//
//                        } else {
//                            value = context.getTarget(fh.fieldName, queryField.schema(), queryField.schemaField(), queryField.schema2(), queryField.schemaField2());
//                        }
//                    } else if (queryField.schema2().length() > 0) {
//
//                        try {
//                            Class fldtype = fh.field.getType();
//                            Object fldValue = context.getTarget(fh.fieldName, queryField.schema(), queryField.schemaField());
//                            String StrFldVal = String.valueOf(fldValue);
//                            if (fldValue == null || StrFldVal.length() == 0) {
//                                value = null;
//                            } else if (fldtype == List.class) {
//
//
//                                Class genType = ReflectExt.firstGenericClass(fh.field);
//                                //  Object val = genType.newInstance();
//                                if (queryField.sqlwhere().length() > 0) {
//                                    String sql = queryField.sqlwhere();
//                                    int cntc = Strings.countChar('?', sql);
//                                    Object[] os = new Object[cntc];
//                                    for (int i = 0; i < cntc; i++) {
//                                        os[i] = fldValue;
//                                    }
//
//                                    Schema sch = rc.getSite().getSchema(queryField.schema2());
//
//
//                                    value = subQueryInfos.getTarget(genType).query(sch, sql, os, rc);
//
//
//                                } else {
//                                    ArrayList vallist = new ArrayList();
//                                    value = vallist;
//                                    for (String str : TagSplitter.split(StrFldVal)) {
//                                        Object genval = createInnerObjSecondLevel(genType, context, queryField.schema2(), str);
//                                        vallist.add(genval);
//                                    }
//
//                                }
//
//
//                            } else {
//                                value = createInnerObjSecondLevel(fldtype, context, queryField.schema2(), StrFldVal);
////
//                            }
//
//
//                        } catch (Exception e) {
//                            throw new RuntimeException(e);
//                        }
//
//                    } else if (queryField.schemaField().length() > 0) {
//                        if (queryField.mapped()) {
//                            value = context.getMapped(fh.fieldName, queryField.schema(), queryField.schemaField());
//                        } else {
//                            value = context.getTarget(fh.fieldName, queryField.schema(), queryField.schemaField());
//                        }
//                    } else if (queryField.schema().length() > 0) {
//                        try {
//                            Class fldtype = fh.field.getType();
//                            Object fldValue = context.getTarget(fh.fieldName);
//                            String StrFldVal = String.valueOf(fldValue);
//                            if (fldValue == null || StrFldVal.length() == 0) {
//                                value = null;
//                            } else if (fldtype == List.class) {
//
//
//                                Class genType = ReflectExt.firstGenericClass(fh.field);
//                                //  Object val = genType.newInstance();
//                                if (queryField.sqlwhere().length() > 0) {
//                                    String sql = queryField.sqlwhere();
//                                    int cntc = Strings.countChar('?', sql);
//                                    Object[] os = new Object[cntc];
//                                    for (int i = 0; i < cntc; i++) {
//                                        os[i] = fldValue;
//                                    }
//
//                                    Schema sch = rc.getSite().getSchema(queryField.schema());
//
//
//                                    value = subQueryInfos.getTarget(genType).query(sch, sql, os, rc);
//
//
//                                } else {
//                                    ArrayList vallist = new ArrayList();
//                                    value = vallist;
//                                    for (String str : TagSplitter.split(StrFldVal)) {
//                                        Object genval = createInnerObj(genType, context, queryField.schema(), str);
//                                        vallist.add(genval);
//                                    }
//
//                                }
//
//
//                            } else {
//                                value = createInnerObj(fldtype, context, queryField.schema(), StrFldVal);
////
//                            }
//
//
//                        } catch (Exception e) {
//                            throw new RuntimeException(e);
//                        }
//
//                    } else {
//                        if (queryField.mapped()) {
//                            value = context.getMapped(fh.fieldName);
//                        } else {
//                            value = context.getTarget(fh.fieldName);
//                        }
//                    }
//                    if (value == null && fh.field.getType().isPrimitive()) {
//                        //do nothing
//                    } else {
//                        try {
//                            fh.field.set(t, value);
//                        } catch (IllegalAccessException e) {
//                            throw new RuntimeException(e);
//                        }
//                    }
//
//                    // fh.getValue(context);
//                });
//
//
////                firstLevelFields.forEach(f -> {
////
////                });
//
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//        });
//
//
//        return ret;
//
//    }
//
//    private Object createInnerObj(Class fldtype, Context context, String queryFieldSchema, String fldvalue) throws IllegalAccessException, InstantiationException {
//        Object value = fldtype.newInstance();
//
//        List<FieldHelper> fieldHelperList = fieldHelperMap.getTarget(fldtype);
//
//        if (fieldHelperList != null) {
//            for (FieldHelper fh2 : fieldHelperList) {
//                java.lang.Object value2 = null;
//                QueryField queryField2 = fh2.queryField;
//                if (queryField2.schemaField().length() > 0) {
//                    value2 = queryField2.mapped() ?
//                            context.getMappedByValueSecondLevel(fldvalue, queryField2.schema(), queryField2.schemaField())
//                            : context.getByValueSecondLevel(fldvalue, queryField2.schema(), queryField2.schemaField());
//
//                } else {
//                    value2 = queryField2.mapped() ?
//                            context.getMappedByValue(fldvalue, queryFieldSchema, fh2.fieldName)
//                            : context.getByValue(fldvalue, queryFieldSchema, fh2.fieldName);
//
//                }
//                if (fh2.field.getType().isPrimitive() && value2 == null) {
//                    continue;
//                }
//                fh2.field.set(value, value2);
//
//            }
//
//        }
//        return value;
//    }
//
//    private Object createInnerObjSecondLevel(Class fldtype, Context context, String queryFieldSchema, String fldvalue) throws IllegalAccessException, InstantiationException {
//        Object value = fldtype.newInstance();
//
//        List<FieldHelper> fieldHelperList = fieldHelperMap.getTarget(fldtype);
//
//        if (fieldHelperList != null) {
//            for (FieldHelper fh2 : fieldHelperList) {
//                java.lang.Object value2 = null;
//                QueryField queryField2 = fh2.queryField;
//
//                value2 = queryField2.mapped() ?
//                        context.getMappedByValueSecondLevel(fldvalue, queryFieldSchema, fh2.fieldName)
//                        : context.getByValueSecondLevel(fldvalue, queryFieldSchema, fh2.fieldName);
//
//
//                if (fh2.field.getType().isPrimitive() && value2 == null) {
//                    continue;
//                }
//                fh2.field.set(value, value2);
//
//            }
//
//        }
//        return value;
//    }
//
//
//    // private void assign(Class c,Schema schema,)
//
//    private void addSchemaField(String schema, String field) {
//        addMapField(schema, field, this.schemaFields);
//
//
//    }
//
//    private void addIdField(String schema, String field) {
//        addMapField(schema, field, this.schemaIDFields);
//
//
//    }
//
//    private static void addMapField(String schema, String field, Map<String, List<String>> map) {
//        List<String> flz = map.getTarget(schema);
//        if (flz == null) {
//            flz = new ArrayList<>();
//            map.put(schema, flz);
//
//        }
//        if (!flz.contains(field)) {
//            flz.add(field);
//        }
//
//
//    }
//
//    private static void addMapValue(String schema, String value, Map<String, Map<String, ResultRow>> map) {
//        Map<String, ResultRow> values = map.getTarget(schema);
//        if (values == null) {
//            values = new HashMap<>();
//
//            map.put(schema, values);
//
//        }
//        values.put(value, null);
//    }
//
//    private void addFieldHelper(Class classTree, FieldHelper fieldHelper) {
//        if (classTree == null) {
//            return;
//        }
//        List<FieldHelper> fieldHelpers = this.fieldHelperMap.getTarget(classTree);
//        if (fieldHelpers == null) {
//            fieldHelpers = new ArrayList<>();
//            fieldHelperMap.put(classTree, fieldHelpers);
//        }
//        fieldHelpers.add(fieldHelper);
//
//
//    }
//
//    /**
//     * 1.获取哪个schema，对应哪些字段
//     * 2.schema的id，从哪些一级字段中获取
//     *
//     * @param cClass
//     * @param schema
//     */
//
//    private void init(Class cClass, String schema, Class classTree) {
//
//
//        if (cClass == null) {
//            return;
//        }
//        Field[] flz = cClass.getDeclaredFields();
//
//
//        for (Field field : flz) {
//            QueryField qf = field.getAnnotation(QueryField.class);
//            if (qf == null) {
//                continue;
//            }
//            String fn = field.getName();
//            if (qf.field().length() > 0) {
//                fn = qf.field();
//
//
//            }
//            //  if (schema.equals("")) {
//            //   this.firstLevelFields.add(  FieldHelper.create(field));
//            //}
//
//            this.addSchemaField(schema, fn);
//            QueryType queryType = field.getAnnotation(QueryType.class);
//            if (queryType != null) {
//
//                // String qtfld = queryType.field();
//
//
//                String[] schemas = queryType.schemas();
//                Class[] types = queryType.types();
//                if (types.length != schemas.length || schemas.length != queryType.values().length) {
//                    throw new RuntimeException("Length mismatch");
//                }
//
//                for (int i = 0; i < schemas.length; i++) {
//
//                    init(types[i], schemas[i], fieldHelperMap.containsKey(types[i]) ? null : types[i]);
//                }
//                String ftn = field.getName();
//                if (queryType.field().length() > 0) {
//                    ftn = queryType.field();
//                }
//                // field.setAccessible(true);
//                this.queryTypeHelpers.add(new QueryTypeHelper(queryType, fn, ftn, field));
//
//
//                continue;
//            }
//            this.addFieldHelper(classTree, new FieldHelper(field));
//
//            if (qf.schema().length() > 0) {
//                if (schema.equals("")) { //从个列获取值作为另外一个schema的key，只有最外层对象（和其父类）才做。
//                    this.addIdField(qf.schema(), fn);
//                } else {
//                    this.addSecondLevelSchema(schema, fn, qf.schema());
//                }
//                if (qf.schemaField().length() > 0) {
//                    this.addSchemaField(qf.schema(), qf.schemaField());
//
//                } else {
//                    Class c = field.getType();
//                    if (c.equals(List.class)) {
//
//                        Class genc = ReflectExt.firstGenericClass(field);
//                        if (qf.sqlwhere().length() > 0) {
//                            addSubQueryInfo(genc);
//                        } else {
//                            init(genc, qf.schema(), fieldHelperMap.containsKey(genc) ? null : genc);
//                        }
//
//
//                    } else {
//                        init(c, qf.schema(), fieldHelperMap.containsKey(c) ? null : c);
//                    }
//                }
//            }
//
//            if (qf.schema2().length() > 0) {
//
//                this.addSecondLevelSchema(qf.schema(), qf.schemaField(), qf.schema2());
//
//
//                if (qf.schemaField2().length() > 0) {
//                    this.addSchemaField(qf.schema2(), qf.schemaField2());
//
//                } else {
//
//                    //Todo 不严谨，需要检查
//                    Class c = field.getType();
//                    if (c.equals(List.class)) {
//
//                        Class genc = ReflectExt.firstGenericClass(field);
//                        if (qf.sqlwhere().length() > 0) {
//                            addSubQueryInfo(genc);
//                        } else {
//                            init(genc, qf.schema2(), fieldHelperMap.containsKey(genc) ? null : genc);
//                        }
//
//
//                    } else {
//                        init(c, qf.schema2(), fieldHelperMap.containsKey(c) ? null : c);
//                    }
//
//                }
//            }
//
//
//        }
//        init(cClass.getSuperclass(), schema, classTree);
//
//
//    }
//
////    private void initLevel2(Class cClass,String schema,String schemaField,String schema2, Class classTree){
////
////        if (cClass == null) {
////            return;
////        }
////        Field[] flz = cClass.getDeclaredFields();
////
////
////        for (Field field : flz) {
////            QueryField qf = field.getAnnotation(QueryField.class);
////            if (qf == null) {
////                continue;
////            }
////            String fn = field.getName();
////            if (qf.field().length() > 0) {
////                fn = qf.field();
////
////
////            }
////            //  if (schema.equals("")) {
////            //   this.firstLevelFields.add(  FieldHelper.create(field));
////            //}
////
////            this.addSchemaField(schema2, fn);
////            QueryType queryType = field.getAnnotation(QueryType.class);
////            if (queryType != null) {
////
////
////
////
////                String[] schemas = queryType.schemas();
////                Class[] types = queryType.types();
////                if (types.length != schemas.length || schemas.length != queryType.values().length) {
////                    throw new RuntimeException("Length mismatch");
////                }
////
////                for (int i = 0; i < schemas.length; i++) {
////
////                    init(types[i], schemas[i], fieldHelperMap.containsKey(types[i]) ? null : types[i]);
////                }
////                String ftn = field.getName();
////                if (queryType.field().length() > 0) {
////                    ftn = queryType.field();
////                }
////
////                this.queryTypeHelpers.add(new QueryTypeHelper(queryType, fn, ftn, field));
////
////
////                continue;
////            }
////            this.addFieldHelper(classTree, new FieldHelper(field));
////
////            if (qf.schema().length() > 0) {
////                if (schema.equals("")) { //从个列获取值作为另外一个schema的key，只有最外层对象（和其父类）才做。
////                    this.addIdField(qf.schema(), fn);
////                } else {
////                    this.addSecondLevelSchema(schema, fn, qf.schema());
////                }
////                if (qf.schemaField().length() > 0) {
////                    this.addSchemaField(qf.schema(), qf.schemaField());
////
////                } else {
////                    Class c = field.getType();
////                    if (c.equals(List.class)) {
////
////                        Class genc = ReflectExt.firstGenericClass(field);
////                        if (qf.sqlwhere().length() > 0) {
////                            addSubQueryInfo(genc);
////                        } else {
////                            init(genc, qf.schema(), fieldHelperMap.containsKey(genc) ? null : genc);
////                        }
////
////
////                    } else {
////                        init(c, qf.schema(), fieldHelperMap.containsKey(c) ? null : c);
////                    }
////                }
////            }
////
////
////        }
////        initLevel2(cClass.getSuperclass(), schema,schemaField,schema2, classTree);
////
////
////    }
//
//    private void addSecondLevelSchema(String schema, String schemaField, String schema2) {
//
//        QueryFields queryFields = this.secondLevelSchemaIdFields.getTarget(schema2);
//        if (queryFields == null) {
//            queryFields = new QueryFields();
//            secondLevelSchemaIdFields.put(schema2, queryFields);
//        }
//        queryFields.addSchemaField(schema, schemaField);
//
//
//    }
//
//
//    public  static class User{};
//    public static class TestClass {
//
//        @QueryField()
//        private String field1;
//
//        @QueryField()
//        private String field2;
//
//        @QueryField(field = "f1", schema = "schema1", schemaField = "xms", schema2 = "schema2", schemaField2 = "scfield2")
//        private String field5;
//
//        @QueryField
//        private int type;
//        @QueryField(field = "id", schema = "/ds/wena/sss")
//
//        User user1;
//
//        @QueryField
//        List<User> users;
//
//        @QueryField(field = "userId", schema = "/ds/wenda/user_info")
//        User user;
//        @QueryField(field = "objid")
//
//        @QueryType(field = "type",
//                values = {"1", "3", "3"},
//                schemas = {"/ds/schema/1", "/ds/schema2"},
//                types = {User.class, ToolProvider.class})
//
//
//        Object value;
//
//
//    }
//}
