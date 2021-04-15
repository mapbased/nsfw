package com.mapkc.nsfw.model;

import com.mapkc.nsfw.query.ResultMapSet;
import com.mapkc.nsfw.query.ResultRow;
import com.mapkc.nsfw.util.EasyMap;
import com.mapkc.nsfw.util.logging.ESLogger;
import com.mapkc.nsfw.util.logging.Loggers;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by chy on 16/12/17.
 */
public abstract class SchemaAccess {


    final static ESLogger log = Loggers.getLogger(SchemaType.class);

    static final EasyMap calFields(Schema schema, final String[] fields, Class t) {
        EasyMap em = new EasyMap();


        if (fields == null || fields.length == 0) {
            if (t.equals(Object[].class) || t.equals(Map.class)) {

                for (XEnum x : schema.getChildren("SchemaField")) {
                    SchemaField sf = (SchemaField) x;
                    em.put(sf.name, null);
                }
                return em;

            }
            Field[] flz = t.getDeclaredFields();
            for (Field f : flz) {
                Column c = f.getAnnotation(Column.class);
                String n = f.getName();
                if (c != null) {
                    if (c.schema().length() > 0) {
                        //需要到别的schema加载数据，直接越过
                        continue;
                    }
                    String fldn = c.field();
                    if (fldn != null && !fldn.equals("")) {
                        n = fldn;
                    }

                } else {
                    if (Modifier.isStatic(f.getModifiers())) {
                        continue;
                    }
                }
                if (schema.hasField(f.getName()) || c != null) {
                    if (!f.isAccessible()) {
                        f.setAccessible(true);
                    }
                    em.put(n, f);
                }
            }

        } else {

            Field[] flz = null;
            if (t.equals(Object[].class) || t.equals(Map.class)) {

            } else {

                flz = t.getDeclaredFields();
            }
            for (String s : fields) {
                // String s = fields[i];
                // can be fielda+fieldb as fieldc
                /*
                 * if (!schema.hasField(s)) {
                 * log.error("Cannot find field {} in schema {}", s,
                 * schema.getId()); continue; }
                 */
                if (flz == null) {
                    em.put(s, null);
                    continue;
                }
                boolean found = false;
                for (Field f : flz) {

                    String fn = f.getName();
                    Column column = f.getAnnotation(Column.class);
                    if (column != null && column.field().length() > 0) {
                        fn = column.field();
                    }
                    if (fn.equals(s) || s.endsWith(" " + fn)) {
                        em.put(s, f);
                        if (!f.isAccessible()) {
                            f.setAccessible(true);
                        }
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    log.error("Cannot find field {} in class {}", s,
                            t.getName());
                }

            }
        }

        return em;

    }

    public String generateKey(Schema schema) {
        return null;
    }

    // public abstract ResultRow getIfModified(DetailQueryConfig qc,
    // RenderContext rc);

    /**
     * @param model
     * @param id
     * @return
     */
    public Map<String, String> load(FormModel model, String id, RenderContext rc) {
        return null;
    }

    /**
     * 返回的结果装到ids里面，数组下标和fields一致
     *
     * @param fields
     * @param ids
     * @param schema
     */
    public abstract ResultMapSet load(String[] fields,
                                      final Map<String, ResultRow> ids, Schema schema);

    public boolean exist(Schema schema, String id) {
        return false;
    }

    protected void check(String id, Map<String, String> values) {

    }

    final public void update(FormModel model, String id, RenderContext rc,
                             Map<String, String> values) throws IOException {
        this.check(id, values);

        Schema schema = model.getSchema();
        switch (model.getActionType(rc, id)) {
            case AddUsingGivenId:
                this.addUsingGivenId(id, schema, values);
                break;
            case AddUsingGeneratedId:
                String s = this.addUsingGeneratedId(schema, values);
                ArrayList<String> al = new ArrayList<String>(1);
                al.add(s);
                rc.setParam(schema.getKeyFieldName(), al);

                break;
            case UpdateUsingGivenId:
                this.updateUsingGivenId(id, schema, values);
                break;
            case AddOrUpdateUsingGivenId:
                this.addOrUpdateUsingGivenId(id, schema, values);
                break;

            default:
                throw new java.lang.RuntimeException("Unknown type:"
                        + model.getActionType(rc, id));
        }

    }

    public String addUsingGeneratedId(Schema schema,
                                      Map<String, String> values) throws IOException {

        String id = this.generateKey(schema);
        this.addUsingGivenId(id, schema, values);

        return id;
    }

    public String addUsingGeneratedId(Schema schema,
                                      Map<String, String> values, int type) throws IOException {
        throw new UnsupportedOperationException();
    }

    public void addUsingGivenId(String id, Schema schema,
                                Map<String, String> values) throws IOException {

    }

    public void addUsingGivenId(String id, Schema schema,
                                Map<String, String> values, int type) {
        throw new UnsupportedOperationException();
    }

    public void insertIgnoreUsingGivenId(String id, Schema schema,
                                         Map<String, String> values) throws IOException {
        this.addOrUpdateUsingGivenId(id, schema, values);
    }

    public void replaceUsingGivenId(String id, Schema schema,
                                    Map<String, String> values) throws IOException {
        this.addOrUpdateUsingGivenId(id, schema, values);
    }

    public void updateUsingGivenId(String id, Schema schema,
                                   Map<String, String> values) throws IOException {
        updateUsingGivenId(id, schema, values, null);

    }

    public void updateUsingGivenId(String id, Schema schema,
                                   Map<String, String> values, String splitvalue) throws IOException {

    }

    public void addOrUpdateUsingGivenId(String id, Schema schema,
                                        Map<String, String> values) throws IOException {
        if (this.exist(schema, id)) {
            this.updateUsingGivenId(id, schema, values);
        } else {
            this.addUsingGivenId(id, schema, values);
        }

    }

    public void delete(String id, Schema schema) {

    }

    public int deleteByQuery(Schema schema, Map<String, Object> conditions) {
        return 0;

    }

    /**
     * @param schema
     * @param wheresql ：仅包含 where
     * @param params
     * @return
     */

    public int deleteBySQL(Schema schema, String wheresql, Object[] params) {
        return 0;

    }

    public int changeFieldValue(Schema schema,
                                final Map<String, ? extends Object> values,
                                final Map<String, ? extends Object> conditions) {
        return 0;

    }

    public int changeFieldValue(Schema schema,
                                final Map<String, ? extends Object> values, String wheresql,
                                final Object[] conditions, String splitFieldValue) {
        return 0;
    }

    public int changeFieldValue(Schema schema,
                                final Map<String, ? extends Object> values, String wheresql,
                                final Object[] conditions) {
        return 0;
    }

    public int changeFieldValue(String id, Schema schema,
                                final Map<String, ? extends Object> values) {
        return this.changeFieldValue(schema, values,
                EasyMap.make(schema.keyFieldName, id));
    }

    public int incFieldValue(Schema schema, String idValue,
                             Map<String, ? extends Object> values) {
        return 0;
    }

    final public int incFieldValue(Schema schema, String idValue,
                                   String fieldName, int step) {
        return incFieldValue(schema, idValue, new EasyMap(fieldName, step));
    }

    final public Object getField(Schema schema, String fieldName, String idValue) {

        Map<String, Object> m = this.getFields(schema, new EasyMap(fieldName,
                null), idValue);
        if (m != null) {
            return m.get(fieldName);
        }
        return null;
    }

    public Map<String, Object> getFieldsByQuery(Schema schema,
                                                final Map<String, Object> fields,
                                                final Map<String, ? extends Object> conditions) {
        return null;
    }

    public Map<String, Object> getFieldsBySql(Schema schema,
                                              final Map<String, Object> fields, String wheresql,
                                              Object[] conditions) {

        return null;
    }

    /**
     * 根据条件查询对象
     *
     * @param schema
     * @param fields     ：指定要加载的字段，可以使用别名的方式书写，别名必须对应对象中的方法名
     * @param t          :对应的字段(declarefields)如果在schema中存在，则自动生成sql并从底层加载。如果含有column标记，
     *                   也会根据标记信息加载.可以是Map.class 或Object[].class ,这样创建对象Map或者对象数组
     * @param wheresql
     * @param conditions
     * @return
     */
    public <T> List<T> listObjectBySql(Schema schema, final String[] fields,
                                       Class<T> t, String wheresql, Object[] conditions) {


        return null;
    }

    public <T> List<T> listObjectBySql(Schema schema,
                                       final String[] fields, final Class<T> t, String wheresql,
                                       Object[] conditions, String splitFieldValue) {
        return null;
    }

    public <T> T getObjectById(Schema schema, String[] fields, Class<T> t, Object id) {

        return getObjectById(schema, fields, t, id, null);
    }

    public <T> T getObjectById(Schema schema, String[] fields, Class<T> t, Object id, String splitFieldValue) {
        List<T> list = this.listObjectBySql(schema, fields, t, schema.getKeyFieldName() + "=?", new Object[]{id}, splitFieldValue);
        if (list == null || list.size() == 0) {
            return null;
        }
        return list.get(0);
    }

    public <T> List<T> listObjectByFullSql(Schema schema, Class<T> t,
                                           String fullsql, Object[] conditions) {

        return null;
    }

    public <T> List<T> listSingleFieldByFullSql(Schema schema,
                                                final Class<T> t, String fullsql, Object[] conditions) {
        return null;
    }

    public <T> List<T> listObjectBySql(Schema schema, Class<T> t,
                                       String wheresql, Object[] conditions) {

        return this.listObjectBySql(schema, null, t, wheresql, conditions);
    }

    public Map<String, Object> getFields(Schema schema,
                                         final Map<String, Object> fields, String idValue) {
        return null;
    }

    public int countByQuery(Schema schema,
                            final Map<String, ? extends Object> conditions) {
        return 0;
    }

    /**
     * 对应
     *
     * @param schema
     * @param wheresql
     * @param params
     * @return
     */
    public int countBySql(Schema schema, String wheresql, Object[] params) {
        return 0;
    }

    public void replaceBatch(Schema schema, final List<Map<String, String>> valueList) {
        throw new UnsupportedOperationException();
    }

    public void insertIngoreBatch(Schema schema, final List<Map<String, String>> valueList) {
        throw new UnsupportedOperationException();
    }

    public void insertBatch(final Schema schema, final List<Map<String, String>> valueList) {
        throw new UnsupportedOperationException();
    }

    public void insertBatch(final Schema schema, final List<Map<String, String>> valueList, int type) {
        throw new UnsupportedOperationException();
    }

    public ResultMapSet load(final String[] fields,
                             final Map<String, ResultRow> values, String fieldName,
                             String where, Object[] args, Schema schema) {
        return null;
    }
}
