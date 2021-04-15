package com.mapkc.nsfw.model;

import com.mapkc.nsfw.input.FormField;
import com.mapkc.nsfw.query.ResultMapSet;
import com.mapkc.nsfw.query.ResultRow;
import com.mapkc.nsfw.util.EasyMap;
import com.mapkc.nsfw.util.SchemaAccessHelper;
import com.mapkc.nsfw.util.Strings;
import com.mapkc.nsfw.util.VolatileBag;
import com.mapkc.nsfw.util.logging.ESLogger;
import com.mapkc.nsfw.util.logging.Loggers;
import com.mapkc.nsfw.valid.Validator;
import org.mvel2.MVEL;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 定义了基础的存储单元。 底层存储可能是数据库，搜索引擎，hbase或者应用程序。
 * <p/>
 * schema定义了基础存储中有哪些字段，每个字段的值有哪些约束。根据schema可以合理的生成表单和列表页（比如枚举值自动翻译）。
 *
 * @author Howard Chang
 */
public class Schema extends XEnum {

    public static final int REPLACE = 0;
    public static final int INSERT_INGORE = 1;
    public static final int INSERT = 2;

    final static ESLogger log = Loggers.getLogger(Schema.class);

    // @Override
    // public XEnumType getXEnumType() {
    // return XEnumtype.getAccess().Schema;
    // }
    @FormField(caption = "Schema类型", input = "radio")
    protected SchemaType type;
    @FormField(caption = "keyFieldName")
    protected String keyFieldName = "_id";
    @FormField(caption = "Comment")
    String comment;
    @FormField(caption = "删除", input = "checkbox")
    boolean removed = false;
    VolatileBag<XEnum> dataSource;
    @FormField(caption = "表名")
    String tableName;

    @FormField(caption = "分表字段")
    String splitField;
    @FormField(caption = "分表表名表达式")
    String splitTableNameExp;
    Site site;
    /**
     * Mvel 表达式
     */
    private Serializable mvelSplitTableNameExp;

    public static String concatForIn(Collection ins, boolean isString) {
        StringBuilder sb = new StringBuilder();

        for (Object o : ins) {
            if (isString) {
                sb.append("\"").append(Strings.escapeSQL(o.toString())).append("\"").append(",");
            } else
                sb.append(o.toString()).append(",");
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    public String getComment() {
        return comment;
    }

    public String getKeyFieldName() {
        return keyFieldName;
    }

    /**
     * 每次启动应用后，从数据库加载的schema和在XEnum存储的schemamerge。<br/>
     * 因为程序并不改变底层 schema，只是为其做一些扩展
     *
     * @param storedSchema
     */
    public Schema merge(Schema storedSchema) {

        if (storedSchema == null) {
            return this;
        }
        if (storedSchema.removed) {
            return null;

        }
        storedSchema.dataSource = this.dataSource;
        storedSchema.keyFieldName = this.keyFieldName;


        return storedSchema;
    }

    protected String defaultIcon() {
        return "fa   fa-table";
    }

    /**
     * 获取schema对应的表的名字，如果是搜索引擎，则为对应indexname,数据库应包含数据库名+表名
     *
     * @return
     */
    public String getTableName() {
        if (this.tableName != null && this.tableName.length() > 0) {
            return this.tableName;
        }
        return this.name;
    }

    public String getTableName(Map<String, ? extends Object> values) {

        if (this.splitField == null || this.splitField.length() == 0) {
            return this.getTableName();
        }
        Object o = values.get(splitField);

        return getSplitedTableName(o == null ? null : String.valueOf(o));

    }

    public String getSplitedTableName(String splitFieldValue) {
        if (this.splitField == null || this.splitField.length() == 0 || this.mvelSplitTableNameExp == null || splitFieldValue == null) {
            return this.getTableName();
        }

        Object o = MVEL.executeExpression(this.mvelSplitTableNameExp, splitFieldValue);
        String s = o == null ? null : o.toString();
        if (s == null) {
            return getTableName();
        }
        String sps = s.trim();
        if (sps.length() == 0) {
            return getTableName();
        }
        return new StringBuffer(getTableName()).append("__").append(sps).toString();

    }

    public DataSource getDataSource() {
        Object o = this.dataSource.getValue();
        if (o instanceof DataSource) {
            return (DataSource) o;
        }
        return null;
    }

    public SchemaField getField(String name) {
        VolatileBag<XEnum> x = this.items.get(name);
        if (x != null && x.getValue() instanceof SchemaField) {
            return (SchemaField) (x.getValue());
        }
        return null;
    }

    public boolean hasField(String name) {
        return this.getField(name) != null;
    }

    public boolean isFieldLoadable(String name) {
        SchemaField sf = this.getField(name);
        if (sf == null) {
            return false;
        }
        return sf.stored;
    }

    /*
     * 生成主键
     */
    public Object generateKey(RenderContext rc) {
        return this.type.getAccess().generateKey(this);
    }

    void addField(String name, SchemaField sf) {
        this.makeSureItems();
        this.items.put(name, new VolatileBag<XEnum>(sf));
        // this.fields.put(name, sf);
    }

    public void update(FormModel model, String id, RenderContext rc,
                       Map<String, String> values) throws IOException {
        //try {
        this.type.getAccess().update(model, id, rc, values);
        this.logEdit(rc, id, values, System.currentTimeMillis());
//		} catch (IntegrityConstraintViolationException e) {
//			log.debug(this.getId(), e);
//			rc.addError("", "提供的数据违反数据库约束");
//		}
    }

    public String getIdValue(RenderContext rc) {
        return rc.param(this.keyFieldName);
    }

    /**
     * 更新给定字段的值为给定的value
     *
     * @param id
     * @param values
     * @param rc
     * @throws IOException
     */
    public void update(String id, Map<String, String> values, RenderContext rc)
            throws IOException {
        this.validate(values, rc);
        if (rc.hasError()) {
            throw new RuntimeException(rc.getError());
        }

        this.type.getAccess().updateUsingGivenId(id, this, values);
        this.logEdit(rc, id, values, System.currentTimeMillis());

    }

    public int executeSql(final String sql, final Object[] values, RenderContext rc) {
        final AtomicReference<Integer> ar = new AtomicReference<Integer>();
        getDataSource().execute(sql,
                new DataSource.PreparedStatmentHandler() {

                    @Override
                    public String toString() {
                        return sql + Strings.toString(values);
                    }

                    @Override
                    public void handle(PreparedStatement stat)
                            throws SQLException {
                        if (type == SchemaType.MySQL) {
                            for (int i = 0; i < values.length; i++) {
                                stat.setObject(i + 1, values[i]);
                            }
                        } else if (type == SchemaType.PgCock) {
                            SchemaAccessPgCock.setPreparedStatementConvereted(stat, values);
                        }
//
                        ar.set(stat.executeUpdate());


                    }
                }, false
        );
        //TODO logedit
        return ar.get();

    }

    public void incUpdate(String id, Map<String, Object> values) throws IOException {
        this.type.getAccess().incFieldValue(this, id, values);
    }

    public Map<String, String> load(FormModel model, String id, RenderContext rc) {
        return this.type.getAccess().load(model, id, rc);
    }

    public SchemaType getType() {
        return type;
    }

    @Override
    protected void init(Site site) {
        // TODO Auto-generated method stub
        super.init(site);
        this.site = site;

        this.dataSource = site.getXEnumBag(this.getParentId());

        this.mvelSplitTableNameExp =
                (this.splitTableNameExp != null && this.splitTableNameExp.trim().length() > 0)
                        ? MVEL.compileExpression(this.splitTableNameExp) : null;

    }

    public ResultMapSet load(String[] fields, final Map<String, ResultRow> ids,
                             RenderContext rc) {
        return type.getAccess().load(fields, ids, this);

    }

    public ResultMapSet load(String[] fields, final Map<String, ResultRow> ids) {
        return type.getAccess().load(fields, ids, this);

    }

    public boolean exist(String id, RenderContext rc) {
        return type.getAccess().exist(this, id);
    }

    public String addUsingGeneratedId(Map<String, String> values) throws IOException {
        return type.getAccess().addUsingGeneratedId(this, values);
    }

    public String addUsingGeneratedId(Map<String, String> values, int t) throws IOException {
        return type.getAccess().addUsingGeneratedId(this, values, t);
    }

    public String addUsingGeneratedId(Map<String, String> values,
                                      RenderContext rc) throws IOException {

        this.validate(values, rc);
        if (rc.hasError()) {
            throw new RuntimeException(rc.getError());
        }
        String id = type.getAccess().addUsingGeneratedId(this, values);
        this.logEdit(rc, id, values, System.currentTimeMillis());
        return id;
    }

    public String addObjectGeneratedId(Object value,
                                       RenderContext rc) throws IOException {
        return addUsingGeneratedId(rc.fromObj(value), rc);

    }

    public void addUsingGivenId(String id, Map<String, String> values) throws IOException {


        this.type.getAccess().addUsingGivenId(id, this, values);

    }

    public void addUsingSql(final String sql, final Object[] values, RenderContext rc) {

        this.addUsingSql(sql, values, false, rc);
    }

    public String addUsingSql(final String sql, final Object[] values, final boolean generateId, RenderContext rc) {
        final AtomicReference<String> ar = new AtomicReference<String>();
        getDataSource().execute(sql,
                new DataSource.PreparedStatmentHandler() {

                    @Override
                    public String toString() {
                        return sql + Strings.toString(values);
                    }

                    @Override
                    public void handle(PreparedStatement stat)
                            throws SQLException {
                        SchemaAccessPgCock.setPreparedStatementConvereted(stat, values);
//                        for (int i = 0; i < values.length; i++) {
//                            stat.setObject(i + 1, values[i]);
//                        }
                        stat.executeUpdate();
                        if (generateId) {
                            ResultSet rs = stat.getGeneratedKeys();
                            if (rs.next()) {

                                ar.set(rs.getString(1));
                            }
                        }

                    }
                }, generateId
        );
        //TODO logedit
        return ar.get();
    }

    public void addUsingGivenId(String id, Map<String, String> values,
                                RenderContext rc) throws IOException {

        this.validate(values, rc);
        if (rc.hasError()) {
            throw new RuntimeException(rc.getError());
        }
        this.type.getAccess().addUsingGivenId(id, this, values);
        this.logEdit(rc, id, values, System.currentTimeMillis());

    }

    public void addObjectById(String id, Object value,
                              RenderContext rc) throws IOException {

        this.addUsingGivenId(id, rc.fromObj(value), rc);

    }

    public void updateUsingGivenId(String id, Map<String, String> values,
                                   RenderContext rc) throws IOException {
        this.validate(values, rc);
        if (rc.hasError()) {
            throw new RuntimeException(rc.getError());
        }
        this.type.getAccess().updateUsingGivenId(id, this, values);
        this.logEdit(rc, id, values, System.currentTimeMillis());

    }

    public void updateUsingGivenId(String id, Map<String, String> values,
                                   String splitField) throws IOException {

        this.type.getAccess().updateUsingGivenId(id, this, values, splitField);


    }

    public void updateObjectById(Object id, Object value,
                                 RenderContext rc) throws IOException {
        this.updateUsingGivenId(id.toString(), rc.fromObj(value), rc);

    }

    public void addOrUpdateUsingGivenId(String id, Map<String, String> values) throws IOException {

        this.type.getAccess().addOrUpdateUsingGivenId(id, this, values);
    }

    public void replaceUsingGivenId(String id, Map<String, String> values) throws IOException {

        this.type.getAccess().replaceUsingGivenId(id, this, values);
    }

    public void insertIgnoreUsingGivenId(String id, Map<String, String> values) throws IOException {

        this.type.getAccess().insertIgnoreUsingGivenId(id, this, values);
    }

    public void addOrUpdateUsingGivenId(String id, Map<String, String> values,
                                        RenderContext rc) throws IOException {
        this.validate(values, rc);
        if (rc.hasError()) {
            throw new RuntimeException(rc.getError());
        }
        this.type.getAccess().addOrUpdateUsingGivenId(id, this, values);
        this.logEdit(rc, id, values, System.currentTimeMillis());
    }

    public void addOrUpdateObjectById(String id, Object value,
                                      RenderContext rc) throws IOException {
        this.addOrUpdateUsingGivenId(id, rc.fromObj(value), rc);

    }

    /**
     * 根据ID删除数据
     *
     * @param id
     * @throws IOException
     */
    public void delete(String id) {
        type.getAccess().delete(id, this);

    }

    public void delete(String id, RenderContext rc) {
        type.getAccess().delete(id, this);
        //TODO this.logEdit();

    }

    public int deleteByQuery(Map<String, Object> conditions) {
        return type.getAccess().deleteByQuery(this, conditions);
        //TODO this.logEdit();

    }

    public int deleteBySQL(String wheresql, Object[] params) {
        return this.type.getAccess().deleteBySQL(this, wheresql, params);
        //TODO this.logEdit();
    }

    public int deleteBySQL(String wheresql, Object[] params, RenderContext rc) {
        //TODO logedit
        return this.type.getAccess().deleteBySQL(this, wheresql, params);

    }

    public int changeFieldValue(final Map<String, Object> values,
                                final Map<String, Object> conditions) {

        return type.getAccess().changeFieldValue(this, values, conditions);
        //TODO this.logEdit();

    }

    public int changeFieldValue(final Map<String, ? extends Object> values,
                                final Map<String, Object> conditions, RenderContext rc) {
        this.validate(values, rc);
        if (rc.hasError()) {
            throw new RuntimeException(rc.getError());
        }
        return type.getAccess().changeFieldValue(this, values, conditions);
        //TODO this.logEdit();

    }

    public void validate(final Map<String, ? extends Object> values,
                         RenderContext rc) {
//
//        String fkvalidVar = FKNames.FK_CUR_VALID;// "fk-curValid";
//        Object keepedfb = rc.vars().getTarget(fkvalidVar);
//        ForBag kfb;
//        if (keepedfb instanceof ForBag) {
//            kfb = (ForBag) keepedfb;
//        } else {
//            kfb = new ForBag();
//            rc.setVar(fkvalidVar, kfb);
//
//        }

        for (String fieldname : values.keySet()) {
            SchemaField sf = this.getField(fieldname);
            if (sf == null) {
                continue;
            }

            // For.ForBag fkvalid = new For.ForBag();

            for (VolatileBag<XEnum> v : sf.items.values()) {
                XEnum x = v.getValue();
                if (x instanceof Validator) {
                    Validator vd = (Validator) x;
                    try {
                        // kfb.value = x;
                        vd.validate(rc, this, values, fieldname);
                    } catch (Exception e) {
                        rc.addError(fieldname, e.getLocalizedMessage());
                    }
                }
            }
        }

    }

    public int changeFieldValue(final Map<String, ? extends Object> values,
                                String wheresql, final Object[] conditions, RenderContext rc) {

        this.validate(values, rc);
        if (rc.hasError()) {
            throw new RuntimeException(rc.getError());
        }
        return type.getAccess().changeFieldValue(this, values, wheresql, conditions);
        //TODO this.logEdit(rc,);
    }

    public int changeFieldValue(final Map<String, ? extends Object> values,
                                String wheresql, final Object[] conditions, String splitField) {


        return type.getAccess().changeFieldValue(this, values, wheresql, conditions, splitField);

    }

    public int changeFieldValue(String id,
                                final Map<String, ? extends Object> values) {
        return type.getAccess().changeFieldValue(id, this, values);
    }

    public int changeFieldValue(String id,
                                final Map<String, ? extends Object> values, RenderContext rc) {
        this.validate(values, rc);
        if (rc.hasError()) {
            throw new RuntimeException(rc.getError());
        }
        int i = type.getAccess().changeFieldValue(id, this, values);
        if (i > 0) {
            this.logEdit(rc, id, values, System.currentTimeMillis());
        }
        return i;
    }

    public int changeFieldValue(String id, String fieldName, Object value) {

        EasyMap m = EasyMap.make(fieldName, value);
        return this.changeFieldValue(id, m);
    }

    public int changeFieldValue(String id, String fieldName, Object value, RenderContext rc) {

        EasyMap m = EasyMap.make(fieldName, value);
        this.validate(m, rc);
        if (rc.hasError()) {
            throw new RuntimeException(rc.getError());
        }
        int i = this.changeFieldValue(id, m);
        if (i > 0) {
            this.logEdit(rc, id, m, System.currentTimeMillis());
        }
        return i;
    }

    public int changeFieldValue(String id, String fieldName, Object newValue, Object oldValue) {

        return this.changeFieldValue(EasyMap.make(fieldName, newValue),
                EasyMap.make(fieldName, oldValue).add(keyFieldName, id));
    }

    public int changeFieldValue(String id, String fieldName, Object newValue, Object oldValue, RenderContext rc) {
        this.validate(EasyMap.make(fieldName, newValue), rc);
        if (rc.hasError()) {
            throw new RuntimeException(rc.getError());
        }

        EasyMap m = EasyMap.make(fieldName, newValue);
        int i = this.changeFieldValue(m,
                EasyMap.make(fieldName, oldValue).add(keyFieldName, id));

        if (i > 0) {
            this.logEdit(rc, id, m, System.currentTimeMillis());
        }
        return i;
    }

    public int incFieldValue(String idValue,
                             Map<String, ? extends Object> values) {
        return type.getAccess().incFieldValue(this, idValue, values);
    }

    final public int incFieldValue(String idValue, String fieldName, int step) {
        return type.getAccess().incFieldValue(this, idValue, fieldName, step);
    }

    final public Object getField(String fieldName, String idValue) {
        return type.getAccess().getField(this, fieldName, idValue);
    }

    final public String getFieldStr(String fieldName, String idValue) {
        Object o = type.getAccess().getField(this, fieldName, idValue);
        if (o == null) {
            return null;
        }
        return o.toString();
    }

    final public int getFieldInt(String fieldName, String idValue) {
        Number number = (Number) type.getAccess().getField(this, fieldName, idValue);

        if (number != null) {
            return number.intValue();
        }
        return 0;
    }

    public Object getFieldByQuery(String fieldname,
                                  final Map<String, ? extends Object> conditions) {
        EasyMap e = EasyMap.make(fieldname, null);
        Map<String, Object> m = this.getFieldsByQuery(e, conditions);
        if (m == null) {
            return null;
        }
        return m.get(fieldname);
    }

    public Map<String, Object> getFieldsByQuery(
            final Map<String, Object> fields,
            final Map<String, ? extends Object> conditions) {
        return type.getAccess().getFieldsByQuery(this, fields, conditions);
    }

    public Map<String, Object> getFieldsBySql(final Map<String, Object> fields,
                                              String wheresql, Object[] conditions) {
        return type.getAccess().getFieldsBySql(this, fields, wheresql, conditions);

    }

    public Map<String, Object> getFieldsByFullSql(String fullsql, Object[] conditions) {

        return (Map<String, Object>) this.getDataSource().execute(fullsql, conditions, new DataSource.ResultSetGetter() {
            @Override
            public Object process(ResultSet rs) throws SQLException {
                if (!rs.next()) {
                    return null;
                }
                Map<String, Object> m = new HashMap<String, Object>();
                ResultSetMetaData rsm = rs.getMetaData();
                for (int i = 0; i < rsm.getColumnCount(); i++) {
                    m.put(rsm.getColumnLabel(i + 1), rs.getObject(i + 1));
                }

                return m;
            }
        });

    }

    public Object getFieldByFullSql(String fullsql, Object[] conditions) {

        return this.getDataSource().execute(fullsql, conditions, new DataSource.ResultSetGetter() {
            @Override
            public Object process(ResultSet rs) throws SQLException {
                if (!rs.next()) {
                    return null;
                }

                return rs.getObject(1);
            }
        });

    }

    public Object getFieldBySql(String fieldName, String wheresql,
                                List conditions) {
        if (conditions == null) {
            return this.getFieldBySql(fieldName, wheresql, new Object[]{});

        }
        return this.getFieldBySql(fieldName, wheresql, conditions.toArray(new Object[conditions.size()]));

    }

    public Object getFieldBySql(String fieldName, String wheresql,
                                Object[] conditions) {

        Map<String, Object> ret = type.getAccess().getFieldsBySql(this,
                EasyMap.make(fieldName, null), wheresql, conditions);
        if (ret == null) {
            return null;
        }
        return ret.get(fieldName);

    }

    /**
     * @param fields
     * @param t          ：若t为 Object[].class 必须指定fields。<br>
     *                   fields可包含 as或者复杂计算列
     * @param wheresql   ：可包含group但通常不能包含join
     * @param conditions
     * @return
     */

    public <T> List<T> listObjectBySql(final String[] fields, Class<T> t,
                                       String wheresql, Object[] conditions) {
        return type.getAccess().listObjectBySql(this, fields, t, wheresql, conditions);
    }

    /**
     * Class 可以使用Column标记
     *
     * @param fields
     * @param t
     * @param id
     * @param <T>
     * @return
     */
    public <T> T getObjectById(final String[] fields, Class<T> t, Object id) {
        return type.getAccess().getObjectById(this, fields, t, id);

    }

    public <T> T getObjectById(final String[] fields, Class<T> t, Object id, String splitFieldValue) {
        return type.getAccess().getObjectById(this, fields, t, id, splitFieldValue);

    }

    /**
     * convenience method
     *
     * @param list
     * @param tClass
     * @param <T>
     */
    public <T> void fillObjectFields(List<T> list, Class<T> tClass) {


        SchemaAccessHelper.fill(list, tClass, this.site);
    }

    public <T> void fillObjectFields(Object o) {


        SchemaAccessHelper.fill(o, this.site);
    }

    /**
     * @param t          k
     * @param wheresql
     * @param conditions
     * @return
     */
    public <T> List<T> listObjectBySql(Class<T> t, String wheresql,
                                       Object[] conditions) {
        return type.getAccess().listObjectBySql(this, t, wheresql, conditions);
    }

    public <T> List<T> listObjectBySql(
            final String[] fields, final Class<T> t, String wheresql,
            Object[] conditions, String splitFieldValue) {
        return type.getAccess().listObjectBySql(this, fields, t, wheresql, conditions, splitFieldValue);
    }

    public <T> List<T> listObjectBySql(String[] fields, Class<T> t,
                                       Map<String, ? extends Object> mapconditions,
                                       String wheresql,
                                       List conditions) {
        if (mapconditions == null || mapconditions.size() == 0)
            return type.getAccess().listObjectBySql(this, fields, t, wheresql, conditions.toArray());
        StringBuilder stringBuilder = new StringBuilder();
        List list = new ArrayList<>(mapconditions.size() + 4);
//        mapconditions.forEach((k, v) -> {
//            stringBuilder.append(k).append("=? and ");
//            list.add(v);
//        });

        for (Map.Entry<String, ? extends Object> e : mapconditions.entrySet()) {
            stringBuilder.append(e.getKey()).append("=? and ");
            list.add(e.getValue());
        }
        if (wheresql != null && wheresql.length() > 0) {
            stringBuilder.append(wheresql);
        } else {
            if (stringBuilder.length() > 4) {
                stringBuilder.setLength(stringBuilder.length() - 4);
            }
        }
        if (conditions != null) {
            list.addAll(conditions);

        }
        return type.getAccess().listObjectBySql(this, fields, t, stringBuilder.toString(), list.toArray());


    }

    /**
     * 根据完整的SQL查询数据集合,
     *
     * @param t:可以是Map.class ,String[].class,或者是使用@column标记的对象class。
     * @param sql
     * @param conditions
     * @param <T>
     * @return
     */
    public <T> List<T> listObjectByFullSql(Class<T> t, String sql, Object[] conditions) {
        return type.getAccess().listObjectByFullSql(this, t, sql, conditions);
    }

    public void loopByFullSQL(final RowVisitor<Map<String, Object>> rowVisitor, String sql, Object[] conditions) {

        this.getDataSource().execute(sql, conditions, Integer.MIN_VALUE, new DataSource.ResultSetGetter() {
            @Override
            public Object process(ResultSet rs) throws SQLException {


                ResultSetMetaData rmeta = rs.getMetaData();
                while (rs.next()) {
                    Map<String, Object> m = new TreeMap<String, Object>();
                    for (int i = 1; i <= rmeta.getColumnCount(); i++) {
                        m.put(rmeta.getColumnLabel(i), rs.getObject(i));
                    }

                    rowVisitor.visit(m);

                }


                return null;
            }
        });
    }

    public <T> List<T> listSingleFieldBySql(
            final Class<T> t, String fieldName, String wheresql, Object[] conditions) {


        return this.type.getAccess().listSingleFieldByFullSql(this, t, new StringBuffer("select ")
                .append(fieldName).append(" from ").append(this.getTableName()).append(
                        (wheresql == null || wheresql.length() == 0) ? "" : " where " + wheresql

                ).toString(), conditions);
    }

    public <T> List<T> listSingleFieldByFullSql(
            final Class<T> t, String fullsql, Object[] conditions) {
        return this.type.getAccess().listSingleFieldByFullSql(this, t, fullsql, conditions);
    }

    public Map<String, Object> getFields(final Map<String, Object> fields,
                                         String idValue) {
        return type.getAccess().getFields(this, fields, idValue);
    }

    public <T> T getFields(T t, String idValue) {
        return this.getFields(t, null, idValue);
    }

    public <T> T getFields(T t, String[] fields, String idValue) {
        EasyMap map = SchemaAccess.calFields(this, fields, t.getClass());// t.getClass().getFields();

        Map<String, Object> m = this.getFields(EasyMap.copy(map), idValue);
        if (m == null) {
            return null;
        }
        try {
            for (Map.Entry<String, Object> e : map.entrySet()) {
                Field f = (Field) e.getValue();
                Object v = m.get(e.getKey());
                if (v != null) {

                    f.set(t, v);

                }
            }
        } catch (Exception e1) {
            throw new java.lang.RuntimeException(e1);

        }
        return t;
    }

    public int countByQuery(Map<String, ? extends Object> conditions) {
        return type.getAccess().countByQuery(this, conditions);
    }

    public int countByQuery(Map<String, ? extends Object> conditions, String whereappend, Object[] objectsArgs) {


        StringBuilder stringBuilder = new StringBuilder();
        List<java.lang.Object> args = new ArrayList<>(conditions.size());
        if (conditions != null && conditions.size() > 0) {
//            conditions.forEach((k, v) -> {
//                stringBuilder.append(k).append("=? and ");
//                args.add(v);
//
//            });
            for (Map.Entry<String, ? extends Object> e : conditions.entrySet()) {
                stringBuilder.append(e.getKey()).append("=? and ");
                args.add(e.getValue());
            }
        }
        if (whereappend != null && whereappend.length() > 0) {
            stringBuilder.append(whereappend);
            if (objectsArgs != null) {
                for (Object o : objectsArgs) {
                    args.add(o);
                }
            }
        } else {
            if (stringBuilder.length() > 0) {
                stringBuilder.setLength(stringBuilder.length() - 4);
            }
        }

        return this.countBySql(stringBuilder.toString(), args.toArray());

    }

    public void replaceBatch(List<Map<String, String>> valueList) {
        this.type.getAccess().replaceBatch(this, valueList);
    }

    public void insertBatch(List<Map<String, String>> valueList) {
        this.type.getAccess().insertBatch(this, valueList);
    }

    public void insertIngoreBatch(List<Map<String, String>> valueList) {
        this.type.getAccess().insertIngoreBatch(this, valueList);
    }

    public int countBySql(String wheresql, Object[] params) {
        return type.getAccess().countBySql(this, wheresql, params);
    }

    public ResultMapSet load(final String[] fields,
                             final Map<String, ResultRow> values, String fieldName,
                             String where, Object[] args) {
        return type.getAccess().load(fields, values, fieldName, where, args, this);
    }

    private void logEdit(RenderContext rc, String id, Map<String, ? extends Object> flz, long timeStamp) {

        for (Map.Entry<String, ? extends Object> e : flz.entrySet()) {
            this.logEdit(rc, id, e.getKey(), e.getValue(), timeStamp);
        }
    }

    private void logEdit(RenderContext rc, String id, String fieldName, Object fieldValue, long timeStamp) {

        // log.warn("UID:{}\tId:{}\tfield:{}\tValue:{}", rc.getUserIdAsInt(), id, fieldName, fieldValue);
    }

    public interface RowVisitor<T> {
        void visit(T row);
    }


//    public void valid(RenderContext rc, Map<String, ? extends Object> values) {
//        for (Map.Entry<String, ? extends Object> entry : values.entrySet()) {
//            String fieldName = entry.getKey();
//            SchemaField sf = this.getField(fieldName);
//            if (sf != null) {
//
//            }
//        }
//    }

}
