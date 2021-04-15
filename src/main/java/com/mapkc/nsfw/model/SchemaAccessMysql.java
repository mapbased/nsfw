package com.mapkc.nsfw.model;

import com.mapkc.nsfw.input.FormFieldModel;
import com.mapkc.nsfw.query.ArrayBasedResultMapSet;
import com.mapkc.nsfw.query.ResultMapSet;
import com.mapkc.nsfw.query.ResultRow;
import com.mapkc.nsfw.util.EasyMap;
import com.mapkc.nsfw.util.Strings;
import com.mapkc.nsfw.util.VolatileBag;

import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by chy on 16/12/17.
 */
public class SchemaAccessMysql extends SchemaAccess {

    protected char sqlNameChar() {
        return '`';
    }

    public void setStatementConvereted(PreparedStatement preparedStatement, Object[] args) throws SQLException {
        if (args == null) {
            return;
        }
        for (int i = 0; i < args.length; i++) {

            preparedStatement.setObject(i + 1, args[i]);
        }
    }

    final public void setStatementConvereted(PreparedStatement preparedStatement, List args) throws SQLException {
        if (args == null) {
            return;
        }
        setStatementConvereted(preparedStatement, args.toArray());
    }


    @Override
    public Map<String, Object> getFieldsByQuery(Schema schema,
                                                final Map<String, Object> fields,
                                                final Map<String, ? extends Object> conditions) {
        Object[] cs = new Object[conditions.size()];
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (Map.Entry<String, ? extends Object> e : conditions.entrySet()) {
            sb.append(e.getKey()).append("=? AND ");
            cs[i++] = e.getValue();
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 4);
        }
        return this.getFieldsBySql(schema, fields, sb.toString(), cs);

    }

    private <T> T toObject(EasyMap fields, Class<T> t, ResultSet rs)
            throws InstantiationException, IllegalAccessException,
            SQLException {

        T obj = t.newInstance();
        int i = 1;
        for (Map.Entry<String, Object> e : fields.entrySet()) {
            Object o = rs.getObject(i++);
            Field f = (Field) e.getValue();
            if (o == null && f.getType().isPrimitive()) {
                continue;
            }
            f.set(obj, o);
        }

        return obj;

    }

    @Override
    public <T> List<T> listObjectByFullSql(Schema schema, final Class<T> t,
                                           String fullsql, Object[] conditions) {

        final List<T> ret = new ArrayList<T>();

        DataSource.ResultSetGetter maprsg = new DataSource.ResultSetGetter() {

            @Override
            public Object process(ResultSet rs) throws SQLException {

                ResultSetMetaData rmeta = rs.getMetaData();
                while (rs.next()) {
                    Map<String, Object> m = new TreeMap<String, Object>();
                    for (int i = 1; i <= rmeta.getColumnCount(); i++) {
                        m.put(rmeta.getColumnLabel(i), rs.getObject(i));

                    }
                    ret.add((T) m);
                }

                return null;
            }
        };

        schema.getDataSource().execute(fullsql, conditions,
                t.equals(Map.class) ? maprsg : new DataSource.ResultSetGetter() {

                    @Override
                    public Object process(ResultSet rs) throws SQLException {

                        ResultSetMetaData rmeta = rs.getMetaData();
                        Field[] flz = new Field[rmeta.getColumnCount()];

                        if (t.equals(Object[].class)) {

                        } else {

                            Field[] dcflz = t.getDeclaredFields();

                            for (int i = 0; i < flz.length; i++) {
                                String label = rmeta.getColumnLabel(i + 1);
                                for (Field f : dcflz) {
                                    if (f.getName().equals(label)) {
                                        flz[i] = f;
                                        if (!f.isAccessible()) {
                                            f.setAccessible(true);
                                        }
                                        break;
                                    }
                                }
                            }
                        }

                        try {

                            while (rs.next()) {
                                T obj = null;
                                if (t.equals(Object[].class)) {
                                    Object[] aa = new Object[flz.length];
                                    obj = (T) aa;
                                    for (int i = 0; i < flz.length; i++) {
                                        aa[i] = rs.getObject(i + 1);

                                    }
                                } else {

                                    obj = t.newInstance();
                                    for (int i = 0; i < flz.length; i++) {
                                        Field f = flz[i];
                                        if (f != null) {
                                            Object o = rs.getObject(i + 1);
                                            if (o == null && f.getType().isPrimitive()) {

                                            } else {
                                                f.set(obj, o);
                                            }
                                        }
                                    }
                                }
                                ret.add(obj);

                            }
                        } catch (Exception e) {
                            throw new java.lang.RuntimeException(e);
                        }

                        return null;
                    }

                }
        );
        return ret;

    }

    @Override
    public <T> List<T> listSingleFieldByFullSql(Schema schema,
                                                final Class<T> t, String fullsql, Object[] conditions) {

        final List<T> ret = new ArrayList<T>();
        schema.getDataSource().execute(fullsql, conditions,
                new DataSource.ResultSetGetter() {

                    @Override
                    public Object process(ResultSet rs) throws SQLException {

                        try {

                            while (rs.next()) {

                                ret.add((T) rs.getObject(1));

                            }
                        } catch (Exception e) {
                            throw new java.lang.RuntimeException(e);
                        }

                        return null;
                    }

                }
        );
        return ret;

    }

    @Override
    public <T> List<T> listObjectBySql(Schema schema,
                                       final String[] fields, final Class<T> t, String wheresql,
                                       Object[] conditions) {
        return listObjectBySql(schema, fields, t, wheresql, conditions, null);
    }


    public <T> List<T> listObjectBySql(Schema schema,
                                       final String[] fields, final Class<T> t, String wheresql,
                                       Object[] conditions, String splitFieldValue) {


        final EasyMap m = calFields(schema, fields, t);

        if (m.size() == 0) {
            throw new java.lang.RuntimeException(
                    "listObjectBySql:Cannot find fields");
        }
        StringBuilder sb = new StringBuilder();
        sb.append("select ");
        for (String s : m.keySet()) {

            sb.append(s).append(",");

        }
        sb.setLength(sb.length() - 1);

        sb.append(" from ").append(schema.getSplitedTableName(splitFieldValue));
        if (wheresql != null && wheresql.length() > 0) {
            sb.append(" where ").append(wheresql);
        }
        final List<T> ret = new java.util.ArrayList<T>();

        schema.getDataSource().execute(sb.toString(), conditions,
                new DataSource.ResultSetGetter() {

                    @Override
                    public Object process(ResultSet rs) throws SQLException {
                        if (t.equals(Object[].class)) {
                            int cnt = fields.length;

                            int edx = 0;
                            for (Map.Entry<String, Object> e : m.entrySet()) {
                                e.setValue(edx++);
                            }

                            while (rs.next()) {
                                Object[] r = new Object[cnt];
                                for (int i = 0; i < cnt; i++) {
                                    int idx = (Integer) m.get(fields[i]);
                                    r[i] = rs.getObject(idx + 1);
                                }
                                ret.add((T) r);
                            }

                        } else if (t.equals(Map.class)) {
                            while (rs.next()) {
                                Map r = new TreeMap();
                                for (String s : m.keySet()) {
                                    if (s.indexOf(' ') > 0) {
                                        s = s.substring(s.lastIndexOf(' ') + 1);
                                    }
                                    r.put(s, rs.getObject(s));
                                }
                                ret.add((T) r);

                            }
                        } else {
                            while (rs.next()) {
                                try {
                                    ret.add(toObject(m, t, rs));
                                } catch (Exception e) {
                                    throw new java.lang.RuntimeException(e);
                                }

                            }
                        }
                        return null;
                    }

                }
        );

        return ret;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> getFieldsBySql(Schema schema,
                                              final Map<String, Object> fields, String wheresql,
                                              Object[] conditions) {

        StringBuilder sb = new StringBuilder();
        sb.append("select ");
        for (String s : fields.keySet()) {
            SchemaField sf = schema.getField(s);
            if (sf != null) {
                sb.append(sf.convertFieldName(s));
            } else {
                sb.append(s);
            }
            sb.append(",");
            /*
             * if (schema.hasField(s)) { sb.append(s).append(","); } else {
             * log.warn( "QueryForList : cannod find field:{} in schema:{}",
             * s, schema.getId()); }
             */

        }
        sb.setLength(sb.length() - 1);

        sb.append(" from ").append(schema.getTableName());
        if (wheresql != null && wheresql.length() > 0) {
            sb.append(" where ").append(wheresql);
        }

        return (Map<String, Object>) schema.getDataSource().execute(
                sb.toString(), conditions, new DataSource.ResultSetGetter() {

                    @Override
                    public Object process(ResultSet rs) throws SQLException {

                        while (rs.next()) {
                            int i = 1;
                            for (Map.Entry<String, Object> e : fields
                                    .entrySet()) {
                                e.setValue(rs.getObject(i++));
                            }
                            return fields;

                        }
                        return null;
                    }

                }
        );

    }

    @Override
    public Map<String, Object> getFields(Schema schema,
                                         final Map<String, Object> fields, String idValue) {
        if (idValue == null || idValue.equals("")) {
            return null;
        }
        return this.getFieldsBySql(schema, fields, schema.getKeyFieldName()
                + "=?", new Object[]{idValue});

    }

    // public <T extends Object> T queryForObject(Schema schema,
    // String[] fieldNames,
    // final Map<String, Object> conditions,
    // Class<T> rettype) {
    // StringBuilder sb=new StringBuilder();
    // sb.append("select ");
    // for(String s:fieldNames){
    // if(schema.hasField(s)){
    // sb.append(s).append(",");
    // }
    // else{
    // log.warn("QueryForList : cannod find field:{} in schema:{}",
    // s,schema.getId());
    // }
    //
    // }
    // sb.setLength(sb.length()-1);
    // sb.append(" from ").append(schema.getTableName());//.append(w)
    // Object[] ps = new Object[conditions.size()];
    // // List<T> ret = new ArrayList<T>();
    // if(conditions.size()>0){
    // sb.append(" where ");
    // int i=0;
    // for(Map.Entry<String, Object > e:conditions.entrySet()){
    // sb.append(e.getKey()).append("=?,");
    // ps[i++]=e.getValue();
    // }
    // sb.setLength(sb.length()-1);
    // }
    // schema.getDataSource().execute(sb.toString(), new ResultSetGetter(){
    //
    // @Override
    // public Object process(ResultSet rs) throws SQLException {
    // // TODO Auto-generated method stub
    // Class<String[]> ss;
    // while (rs.next()) {
    //
    //
    // }
    // return null;
    // }
    //
    // }, ps);
    //
    // return null;
    //
    //
    // }
    @Override
    public boolean exist(Schema schema, String id) {

        String sql = new StringBuilder().append("select count(*) from ")
                .append(schema.getTableName()).append(" where ")
                .append(schema.getKeyFieldName()).append("=?").toString();
        return (Boolean) schema.getDataSource().execute(sql,
                new Object[]{id}, new DataSource.ResultSetGetter() {

                    @Override
                    public Object process(ResultSet rs) throws SQLException {
                        rs.next();
                        return rs.getInt(1) > 0;

                    }
                }
        );

    }

    @Override
    public void updateUsingGivenId(String id, Schema schema,
                                   Map<String, String> values, String splitvalue)
            throws IOException {

        StringBuilder sb = new StringBuilder();
        sb.append("update ").append(schema.getSplitedTableName(splitvalue)).append(" set ");
        final List<String> vs = new java.util.ArrayList<String>(
                values.size() + 1);

        for (Map.Entry<String, String> e : values.entrySet()) {
            SchemaField sf = schema.getField(e.getKey());
            if (/* e.getValue() == null || */sf == null) {
                continue;
            }
            sb.append(e.getKey()).append("=").append(sf.updateFunction())
                    .append(",");
            vs.add(e.getValue());

        }
        sb.setLength(sb.length() - 1);
        sb.append(" where ").append(schema.getKeyFieldName()).append("=?");
        vs.add(id);

        schema.getDataSource().execute(sb.toString(),
                new DataSource.PreparedStatmentHandler() {

                    @Override
                    public void handle(PreparedStatement stat)
                            throws SQLException {

                        setStatementConvereted(stat, vs);
//                        for (int i = 0; i < vs.size(); i++) {
//                            stat.setString(i + 1, vs.getTarget(i));
//                        }
                        int update = stat.executeUpdate();

                    }

                    @Override
                    public String toString() {
                        return Strings.toString(vs);
                    }
                }
        );

    }


    public void insertIngoreBatch(Schema schema, final List<Map<String, String>> valueList) {
        insertBatch(schema, valueList, 1);
    }

    public void replaceBatch(final Schema schema, final List<Map<String, String>> valueList) {
        insertBatch(schema, valueList, 0);
    }

    public void insertBatch(final Schema schema, final List<Map<String, String>> valueList) {
        insertBatch(schema, valueList, 2);
    }


    /**
     * Generated Id don't return
     *
     * @param schema
     * @param valueList
     */
    public void insertBatch(final Schema schema, final List<Map<String, String>> valueList, int type) {


        StringBuilder vs = new StringBuilder("(");
        final StringBuilder sb = new StringBuilder();
        String k = type == 0 ? "replace into " : type == 1 ? "insert ignore into " : "insert into ";
        sb.append(k).append(schema.getTableName(valueList.get(0)))
                .append("(");
        // String keyfield = schema.keyFieldName;
        final List<String> fieldIndex = new ArrayList<>(schema.items.size());

        for (Map.Entry<String, VolatileBag<XEnum>> sfs : schema.items.entrySet()) {
            com.mapkc.nsfw.model.XEnum xEnum = sfs.getValue().getValue();
            if (!(xEnum instanceof SchemaField)) {
                continue;
            }
//                if (sfs.getKey().equals(keyfield)) {
//                    continue;
//                }
            SchemaField schemaField = (SchemaField) xEnum;
            sb.append(sqlNameChar()).append(sfs.getKey()).append(sqlNameChar()).append(",");
            fieldIndex.add(sfs.getKey());
            vs.append(schemaField.updateFunction()).append(",");


        }
        sb.setLength(sb.length() - 1);
        vs.setLength(vs.length() - 1);
        vs.append(")");

        sb.append(")values").append(vs.toString());

        try {
            schema.getDataSource().transaction(new DataSource.Transaction() {
                @Override
                public void doInTransaction() throws Exception {
                    schema.getDataSource().execute(sb.toString(),
                            new DataSource.PreparedStatmentHandler() {
                                @Override
                                public String toString() {
                                    return Strings.toString(valueList);

                                }

                                @Override
                                public void handle(PreparedStatement stat)
                                        throws SQLException {

                                    for (Map<String, String> value : valueList) {
                                        // Map<String,String> value=null;
                                        for (int i = 0; i < fieldIndex.size(); i++) {
                                            stat.setString(i + 1, value.get(fieldIndex.get(i)));
                                        }
                                        stat.addBatch();
                                    }


                                    stat.executeBatch();


                                }
                            }, false
                    );
                }
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


    }

    @Override
    public String addUsingGeneratedId(Schema schema,
                                      Map<String, String> values) {
        return addUsingGeneratedId(schema, values, 2);
    }


    public String addUsingGeneratedId(Schema schema,
                                      Map<String, String> values, int type) {

        String k = type == 0 ? "replace into " : type == 1 ? "insert ignore into " : "insert into ";
        final StringBuilder sb = new StringBuilder();
        sb.append(k).append(schema.getTableName(values))
                .append("(");
        final List<String> vs = new java.util.ArrayList<String>(
                values.size() + 1);

        values.remove(schema.getKeyFieldName());
        StringBuilder fsb = new StringBuilder(values.size() << 1);
        for (Map.Entry<String, String> e : values.entrySet()) {
            SchemaField sf = schema.getField(e.getKey());
            if (e.getValue() == null || sf == null) {
                continue;
            }
            sb.append(sqlNameChar()).append(e.getKey()).append(sqlNameChar()).append(",");
            fsb.append(sf.updateFunction()).append(",");
            vs.add(e.getValue());

        }
        sb.setLength(sb.length() - 1);

        sb.append(")values(");

        sb.append(fsb);
        sb.setLength(sb.length() - 1);
        sb.append(")");

        final AtomicReference<String> ar = new AtomicReference<String>();

        schema.getDataSource().execute(sb.toString(),
                new DataSource.PreparedStatmentHandler() {
                    @Override
                    public String toString() {
                        return Strings.toString(vs);

                    }

                    @Override
                    public void handle(PreparedStatement stat)
                            throws SQLException {

                        setStatementConvereted(stat, vs);
//                        for (int i = 0; i < vs.size(); i++) {
//                            stat.setString(i + 1, vs.getTarget(i));
//                        }

                        stat.executeUpdate();

                        ResultSet rs = stat.getGeneratedKeys();
                        if (rs.next()) {

                            ar.set(rs.getString(1));
                        }

                    }
                }, true
        );
        values.put(schema.getKeyFieldName(), ar.get());

        return ar.get();
    }

    @Override
    public void addUsingGivenId(String id, Schema schema,
                                Map<String, String> values) {
        this.addUsingGivenId(id, schema, values, 2);
    }


    public void replaceUsingGivenId(String id, Schema schema,
                                    Map<String, String> values) throws IOException {
        this.addUsingGivenId(id, schema, values, 0);
    }

    public void insertIgnoreUsingGivenId(String id, Schema schema,
                                         Map<String, String> values) throws IOException {
        this.addUsingGivenId(id, schema, values, 1);
    }

    public void addUsingGivenId(String id, Schema schema,
                                Map<String, String> values, boolean replace) {
        addUsingGivenId(id, schema, values, replace ? 0 : 2);
    }

    /**
     * @param id
     * @param schema
     * @param values
     * @param type   0:replace 1: insert ignore 2:insert
     */

    public void addUsingGivenId(String id, Schema schema,
                                Map<String, String> values, int type) {
        StringBuilder sb = new StringBuilder();
        sb.append(type == 0 ? "replace into " : type == 1 ? "insert ignore into " : "insert into ")
                .append(schema.getTableName(values))
                .append("(");
        final List<String> vs = new java.util.ArrayList<String>(
                values.size() + 1);

        StringBuilder fsb = new StringBuilder(values.size() << 1);
        for (Map.Entry<String, String> e : values.entrySet()) {
            SchemaField sf = schema.getField(e.getKey());
            if (e.getValue() == null || sf == null) {
                continue;
            }
            sb.append(sqlNameChar()).append(e.getKey()).append(sqlNameChar()).append(",");
            fsb.append(sf.updateFunction()).append(",");
            vs.add(e.getValue());

        }
        if (values.containsKey(schema.getKeyFieldName())) {
            sb.setLength(sb.length() - 1);
        } else {
            sb.append(schema.getKeyFieldName());
            vs.add(id);
            fsb.append("?,");

        }
        sb.append(")values(");
        sb.append(fsb);

        sb.setLength(sb.length() - 1);
        sb.append(")");

        schema.getDataSource().execute(sb.toString(),
                new DataSource.PreparedStatmentHandler() {

                    @Override
                    public String toString() {
                        return Strings.toString(vs);
                    }

                    @Override
                    public void handle(PreparedStatement stat)
                            throws SQLException {
                        setStatementConvereted(stat, vs);
//                        for (int i = 0; i < vs.size(); i++) {
//                            stat.setString(i + 1, vs.getTarget(i));
//                        }
                        stat.executeUpdate();

                    }
                }
        );
    }

    /**
     * TODO ://SQL注入攻击
     */
    @Override
    public ResultMapSet load(final String[] fields,
                             final Map<String, ResultRow> ids, Schema schema
    ) {
        if (ids.size() == 0) {
            return new ArrayBasedResultMapSet(fields, ids);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("select ").append(schema.keyFieldName).append(",");
        for (String s : fields) {
            sb.append(s).append(",");
        }
        sb.setLength(sb.length() - 1);
        sb.append(" from ").append(schema.getTableName()).append(" where ")
                .append(schema.keyFieldName).append(" in(");
        for (String s : ids.keySet()) {
            sb.append("'").append(Strings.escapeSQL(s)).append("',");
        }
        sb.setCharAt(sb.length() - 1, ')');
        //TODO using in(?),then set string[] ,will be safer and simgpler

        String sbs = sb.toString();
        // log.debug("sql:{}", sbs);
        return (ResultMapSet) schema.getDataSource().execute(sbs,
                new DataSource.ResultSetGetter() {

                    @Override
                    public Object process(ResultSet rs) throws SQLException {

                        ArrayBasedResultMapSet abrr = new ArrayBasedResultMapSet(
                                fields, ids);

                        while (rs.next()) {
                            Object[] vs = new Object[fields.length + 1];
                            String id = rs.getString(1);
                            for (int i = 0; i < vs.length; i++) {
                                vs[i] = rs.getObject(i + 1);
                            }
                            abrr.addResult(id, vs);

                        }
                        return abrr;

                    }
                }
        );

    }

    /**
     * 从Schema获取给定的字段，传入的参数为多个值，以及自定义的where条件。返回时默认每个值对应的数据唯一。
     *
     * @param fields：额外加载的字段列表
     * @param values           ：id为key的值
     * @param fieldName        ：这个字段作为key
     * @param where            ：where条件
     * @param args             ：where参数
     * @param schema
     * @return
     */

    @Override
    public ResultMapSet load(final String[] fields,
                             final Map<String, ResultRow> values, String fieldName,
                             String where, Object[] args, Schema schema) {


        StringBuilder sb = new StringBuilder();
        sb.append("select ").append(fieldName).append(",");
        for (String s : fields) {
            sb.append(s).append(",");
        }
        sb.setLength(sb.length() - 1);
        sb.append(" from ").append(schema.getTableName()).append(" where ");
        boolean addwhere = where != null && where.length() > 0;
        if (addwhere) {

            sb.append(where);
        }
        if (values.size() > 0) {
            if (addwhere) {
                sb.append(" AND ");
            }

            sb.append(fieldName).append(" in(");
            for (String s : values.keySet()) {
                sb.append("'").append(Strings.escapeSQL(s)).append("',");
            }
            sb.setCharAt(sb.length() - 1, ')');
        }


        String sbs = sb.toString();
        // log.debug("sql:{}", sbs);
        return (ResultMapSet) schema.getDataSource().execute(sbs, args,
                new DataSource.ResultSetGetter() {

                    @Override
                    public Object process(ResultSet rs) throws SQLException {
                        // TODO Auto-generated method stub
                        ArrayBasedResultMapSet abrr = new ArrayBasedResultMapSet(
                                fields, values);

                        while (rs.next()) {
                            Object[] vs = new Object[fields.length + 1];
                            String id = rs.getString(1);
                            for (int i = 0; i < vs.length; i++) {
                                vs[i] = rs.getObject(i + 1);
                            }
                            abrr.addResult(id, vs);

                        }
                        return abrr;

                    }
                }
        );

    }

    @Override
    public void delete(String id, Schema schema) {
        this.deleteByQuery(schema, EasyMap.make(schema.keyFieldName, id));

    }

    @Override
    public int deleteBySQL(Schema schema, String wheresql, Object[] params) {

        StringBuilder sb = new StringBuilder("delete from ")
                .append(schema.getTableName()).append(" where ")
                .append(wheresql);

        return schema.getDataSource().execute(sb.toString(), params);

    }

    @Override
    public int countBySql(Schema schema, String wheresql, Object[] params) {
        StringBuilder sb = new StringBuilder("select count(*) from ")
                .append(schema.getTableName());
        if (wheresql != null && wheresql.length() > 0) {
            sb.append(" where ").append(wheresql);
        }

        return (Integer) schema.getDataSource().execute(sb.toString(),
                params, new DataSource.ResultSetGetter() {

                    @Override
                    public Object process(ResultSet rs) throws SQLException {
                        rs.next();
                        return rs.getInt(1);
                    }
                }
        );// cute(sb.toString(), params);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, String> load(FormModel model, String id,
                                    RenderContext rc) {
        final Map<String, String> ret = new java.util.HashMap<String, String>();
        final Schema sc = model.getSchema();
        model.travelFields(new FormModel.FormFieldVisitor() {

            @Override
            public void visit(FormFieldModel ffm) {
                if (/*ffm.isSchemaField() ||*/ sc.hasField(ffm.getName())) {
                    ret.put(ffm.getName(), ffm.getFieldName());
                }
            }
        }, rc, true);

        StringBuilder sb = new StringBuilder();
        sb.append("select ");
        for (Map.Entry<String, String> e : ret.entrySet()) {

            sb.append(e.getValue()).append(",");
            e.setValue(null);
        }

        sb.append(sc.getKeyFieldName()).append(" from ")
                .append(sc.getTableName()).append(" where ")
                .append(sc.getKeyFieldName()).append("=?");

        return (Map<String, String>) sc.getDataSource().execute(
                sb.toString(), new Object[]{id}, new DataSource.ResultSetGetter() {

                    @Override
                    public Object process(ResultSet rs) throws SQLException {
                        int i = 0;
                        if (rs.next()) {
                            for (Map.Entry<String, String> e : ret
                                    .entrySet()) {
                                e.setValue(rs.getString(++i));
                            }
                            return ret;
                        }
                        return null;
                    }
                }
        );

    }


    @Override
    public int changeFieldValue(Schema schema,
                                final Map<String, ? extends Object> values, String wheresql,
                                final Object[] conditions) {
        return this.changeFieldValue(schema, values, wheresql, conditions, null);
    }

    @Override
    public int changeFieldValue(Schema schema,
                                final Map<String, ? extends Object> values, String wheresql,
                                final Object[] conditions, String splitFieldValue) {
        if (values == null || values.size() == 0) {
            return 0;
        }
        List vs = new ArrayList();

        StringBuilder sb = new StringBuilder("update ").append(sqlNameChar()).append(
                schema.getSplitedTableName(splitFieldValue)).append(sqlNameChar()).append(" set ");
        for (Map.Entry<String, ? extends Object> e : values.entrySet()) {
            String k = e.getKey();
            SchemaField sf = schema.getField(k);
            if (sf == null) {
                throw new java.lang.RuntimeException("Cannot find field:"
                        + k + " in schema:" + schema.getId());
            }
            sb.append(sqlNameChar()).append(k).append(sqlNameChar()).append(" = ")
                    .append(sf.updateFunction()).append(",");
            vs.add(e.getValue());
        }
        sb.setLength(sb.length() - 1);

        if (wheresql != null) {
            sb.append(" where ");
            sb.append(wheresql);

        }

        final AtomicInteger ai = new AtomicInteger();
        schema.getDataSource().execute(sb.toString(),
                new DataSource.PreparedStatmentHandler() {

                    @Override
                    public String toString() {
                        return Strings.arrayToString(conditions) + values;
                    }

                    @Override
                    public void handle(PreparedStatement ps)
                            throws SQLException {
                        int parameterIndex = 1;


//
//                        for (Map.Entry<String, ? extends Object> e : values
//                                .entrySet()) {
//                            ps.setObject(parameterIndex++, e.getValue());
//
//                        }

                        for (int i = 0; i < conditions.length; i++) {
                            // ps.setObject(parameterIndex++, conditions[i]);
                            vs.add(conditions[i]);
                        }

                        setStatementConvereted(ps, vs);

                        int cnt = ps.executeUpdate();
                        ai.set(cnt);

                    }
                }
        );
        return ai.get();

    }

    @Override
    public int changeFieldValue(Schema schema,
                                final Map<String, ? extends Object> values,
                                final Map<String, ? extends Object> conditions) {

        for (String k : conditions.keySet()) {
            if (!schema.hasField(k)) {
                throw new java.lang.RuntimeException("Cannot find field:"
                        + k + " in schema:" + schema.getId());
            }

        }
        //Object[] os = new Object[conditions.size()];
        List<Object> os = new ArrayList<>(conditions.size());

        StringBuilder sb = new StringBuilder();


        for (Map.Entry<String, ? extends Object> e : conditions.entrySet()) {
            if (e.getValue() == null) {
                if (e.getKey().equals(schema.keyFieldName)) {
                    throw new RuntimeException("key field cannot be null");
                }
                sb.append(sqlNameChar()).append(e.getKey()).append(sqlNameChar()).append(" is null AND ");
            } else {
                sb.append(sqlNameChar()).append(e.getKey()).append(sqlNameChar()).append(" = ? AND ");
//					os[i++] = e.getValue();
                os.add(e.getValue());
            }
        }
        sb.setLength(sb.length() - 4);


        return this.changeFieldValue(schema, values, sb.toString(), os.toArray(new Object[os.size()]));

    }

    @Override
    final public int incFieldValue(Schema schema, final String idValue,
                                   final Map<String, ? extends Object> values) {
        if (values == null || values.size() == 0) {
            log.warn("incFieldValue ,but no fields found,schema:{},id:{}",
                    schema, idValue);
            return 0;
        }
        Object[] p = new Object[values.size() + 1];

        int idx = 0;
        StringBuilder sb = new StringBuilder("update ").append(sqlNameChar()).append(
                schema.getTableName()).append(sqlNameChar()).append(" set ");
        for (Map.Entry<String, ? extends Object> e : values.entrySet()) {
            sb.append(sqlNameChar()).append(e.getKey()).append(sqlNameChar()).append(" =").append(sqlNameChar())
                    .append(e.getKey()).append(sqlNameChar()).append(" + ? ,");
            p[idx++] = e.getValue();

        }
        sb.setLength(sb.length() - 1);
        sb.append(" where ").append(schema.getKeyFieldName()).append("=?");

        p[idx++] = idValue;

        return schema.getDataSource().execute(sb.toString(), p);

    }

    @Override
    final public int deleteByQuery(Schema schema,
                                   final Map<String, Object> conditions) {

        if (conditions == null || conditions.size() == 0) {
            return 0;
        }
        StringBuilder sb = new StringBuilder("delete from ").append(sqlNameChar()).append(
                schema.getTableName(conditions)).append(sqlNameChar()).append(" where ");
        Object[] vs = new Object[conditions.size()];
        int i = 0;
        for (Map.Entry<String, Object> e : conditions.entrySet()) {
            vs[i++] = e.getValue();
            sb.append(sqlNameChar()).append(e.getKey()).append(sqlNameChar()).append(" = ? AND ");
        }
        sb.setLength(sb.length() - 4);
        return schema.getDataSource().execute(sb.toString(), vs);

    }

    @Override
    final public int countByQuery(Schema schema,
                                  final Map<String, ? extends Object> conditions) {

        StringBuilder sb = new StringBuilder("select count(*) from ").append(sqlNameChar())
                .append(schema.getTableName(conditions)).append(sqlNameChar()).append(" where ");
        Object[] vs = new Object[conditions.size()];
        int i = 0;
        for (Map.Entry<String, ? extends Object> e : conditions.entrySet()) {
            vs[i++] = e.getValue();
            sb.append(sqlNameChar()).append(e.getKey()).append(sqlNameChar()).append(" = ? AND ");
        }
        sb.setLength(sb.length() - 4);
        return (Integer) schema.getDataSource().execute(sb.toString(), vs,
                new DataSource.ResultSetGetter() {

                    @Override
                    public Object process(ResultSet rs) throws SQLException {

                        rs.next();

                        return rs.getInt(1);
                    }
                }
        );

    }

}

