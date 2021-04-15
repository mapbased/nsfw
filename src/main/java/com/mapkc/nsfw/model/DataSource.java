/**
 *
 */
package com.mapkc.nsfw.model;

//import com.mapbased.search.client.SearchClient;
//import com.mapbased.search.req.Req;
//import com.mapbased.search.req.Resp;
//import com.mapbased.search.req.misc.StringPair;
//import com.mapbased.search.req.socket.*;

import com.mapkc.nsfw.input.FormField;
import com.mapkc.nsfw.util.IntBag;
import com.mapkc.nsfw.util.StringPair;
import com.mapkc.nsfw.util.Strings;
import com.mapkc.nsfw.util.VolatileBag;
import com.mapkc.nsfw.util.logging.ESLogger;
import com.mapkc.nsfw.util.logging.Loggers;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.ConnectException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author chy
 */
public class DataSource extends XEnum {
    final static ESLogger log = Loggers.getLogger(DataSource.class);
    @FormField(input = "radio", caption = "类型")
    SchemaType schemaType;
    @FormField(caption = "URL", required = true)
    Renderable url;
    @FormField(caption = "User Name")
    Renderable userName;
    @FormField(caption = "Password", input = "text<type=password>")
    Renderable password;
    @FormField(caption = "包含的表", msg = "正则表达式，只加载这些包含的表，避免小项目加载一大堆表")
    Pattern schemainclude;
    private final ThreadLocal<Connection> threadlocalConnection = new ThreadLocal<Connection>();
    private final ThreadLocal<Connection> threadlocalConnectionSlave = new ThreadLocal<Connection>();
    private BasicDataSource jdbcDS;

    /**
     * 从注释中抽取screenName和真实的comment。 在数据库和搜索的注释中，采用
     * screenname+逗号+真实注释的规则，方面从底层schema直接获取screenname和注释
     *
     * @param cmt  :注释
     * @param name ：字段名
     */
    static StringPair screenNamefromComment(String cmt, String name) {

        StringPair sp = new StringPair();
        if (cmt == null || cmt.trim().equals("")) {
            cmt = name;
        }
        int i = cmt.indexOf(',');
        if (i < 0) {
            i = cmt.indexOf(":");
        }
        if (i > 0) {
            sp.name = cmt.substring(0, i);
            sp.value = cmt.substring(i + 1);
        } else {
            sp.name = cmt;
            sp.value = "";
        }
        return sp;
    }

    protected String defaultIcon() {
        return "fa   fa-database";
    }

    @Override
    public void destory(Site site) {
//        if (this.searchClient != null) {
//            this.searchClient.destory();
//        }
        if (this.jdbcDS != null) {
            try {
                this.jdbcDS.close();
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    /**
     * TODO :create search client
     *
     * @return
     */
//    private SearchClient getSearchClient() {
//        return this.searchClient;
//    }
    public void load(Site s) {
        this.lastModified = System.currentTimeMillis();
        if (this.schemaType == SchemaType.Hbase) {


//        } else if (this.schemaType == SchemaType.Search) {
//            this.loadSearch(s);
        } else if (this.schemaType == SchemaType.MySQL) {
            this.loadMysql(s);
        } else if (this.schemaType == SchemaType.PgCock) {
            this.loadPgSql(s);
        }
    }
    //MysqlDataSource rawDataSource;

    //SearchClient searchClient;

    private Schema loadPgCockSchema(final StringPair t, Site site) {

        final Schema schema = new Schema();
        // s.datasource = ds;
        schema.type = SchemaType.PgCock;
        StringPair names = screenNamefromComment(t.value, t.name);
        schema.screenName = names.name;
        schema.comment = names.value;
        schema.name = t.name;


        this.execute(
                "SELECT  COLUMN_NAME,ORDINAL_POSITION, IS_NULLABLE,DATA_TYPE,CHARACTER_MAXIMUM_LENGTH,'' as COLUMN_COMMENT "
                        + "FROM information_schema.COLUMNS  WHERE TABLE_SCHEMA=? AND TABLE_NAME=? order by ORDINAL_POSITION asc",
                new Object[]{this.getDatabaseName(site), t.name},
                new ResultSetGetter() {
                    @Override
                    public Object process(ResultSet rs) throws SQLException {
                        while (rs.next()) {
                            if (rs.getInt("ORDINAL_POSITION") == 1) {
                                schema.keyFieldName = rs
                                        .getString("COLUMN_NAME");
                                // TODO :这里默认了第一个字段为key
                            }
                            SchemaField sf = new SchemaField();
                            sf.name = rs.getString("COLUMN_NAME");
                            StringPair sp = screenNamefromComment(
                                    rs.getString("COLUMN_COMMENT"), sf.name);
                            sf.screenName = sp.name;
                            sf.comment = sp.value;
                            sf.maxLength = rs
                                    .getLong("CHARACTER_MAXIMUM_LENGTH");
                            sf.indexed = true;
                            sf.stored = true;
                            sf.tokenized = false;

                            sf.setSort(rs.getInt("ORDINAL_POSITION"));

                            String datatype = rs.getString("DATA_TYPE");
                            sf.type = SchemaFieldType.fromPgCockType(datatype);
                            if (sf.type == null) {
                                log.warn(
                                        "Cannot find schema type:{} ,table:{}",
                                        datatype, t.name);
                                sf.type = SchemaFieldType.Text;
                            }


                            schema.addField(sf.name, sf);
                        }

                        return null;
                    }
                }
        );
        return schema;
    }

    public String url(Site s) {
        RenderContext rc = new RenderContext(s);
        return this.url.getRenderValue(rc);

    }

    // @Override
    // public XEnumType getXEnumType() {
    // return XEnumType.DataSource;
    // }

//    public Resp sendSearchReq(Req req) {
//
//        return this.sendSearchReq(req, 10000);
//    }
//
//    public Resp sendSearchReq(Req req, long timeout) {
//
//        return getSearchClient().send(req, timeout);
//    }
//
//
//    private void loadSearchSchemas(Site site) {
//        Resp resp = sendSearchReq(new MetaReq());
//        if (resp instanceof MetaResp) {
//            MetaResp mr = (MetaResp) resp;
//
//            // if(this.schemainclude==null||this.schemainclude.matcher(in))
//
//            // String id = this.getId();
//            for (StringPair sp : mr.clusters) {
//                if (this.schemainclude != null) {
//                    if (!this.schemainclude.matcher(sp.name).matches()) {
//                        continue;
//                    }
//                }
//
//                Schema s = fromSearch(sp, site, this);
//
//                schemaBuilded(s, site);
//                // site.enums.put(s.name, new VolatileBag<XEnum>(s));
//            }
//
//        }
//    }

    public String password(Site s) {
        RenderContext rc = new RenderContext(s);
        return this.password.getRenderValue(rc);

    }


//    /**
//     * 注释规则：screenname,comment
//     *
//     * @param sp
//     * @param site
//     * @return
//     */
//    public static Schema fromSearch(StringPair sp, Site site, DataSource ds) {
//        Schema s = new Schema();
//        // s.datasource = ds;
//        s.type = SchemaType.Search;
//
//        s.name = sp.name;
//        StringPair names = screenNamefromComment(sp.value, sp.name);
//        s.screenName = names.name;
//        s.comment = names.value;
//
//        SchemaReq sr = new SchemaReq();
//        sr.schemaName = sp.name;
//
//        Resp resp = ds.sendSearchReq(sr);
//        if (resp instanceof SchemaResp) {
//            SchemaResp srsp = (SchemaResp) resp;
//            for (TypeInfo ti : srsp.types) {
//                if (ti.name.endsWith("*")) {
//
//                    // TODO support dynamic fields
//                    continue;
//                }
//                SchemaField sf = new SchemaField();
//                sf.fromTypeInfo(ti);
//                s.addField(sf.name, sf);
//            }
//            // System.out.println(srsp);
//        } else {
//            log.error("Error Resp for:{}", sr.schemaName);
//        }
//
//        return s;
//    }

    public String user(Site s) {
        RenderContext rc = new RenderContext(s);
        return this.userName.getRenderValue(rc);

    }

    private void initDs(Site s) throws ConnectException {
        RenderContext rc = new RenderContext(s);
        String url = this.url.getRenderValue(rc);

        if (this.schemaType == SchemaType.Hbase) {

//        } else if (this.schemaType == SchemaType.Search) {
//            this.searchClient = new SearchClient(url);
        } else if (this.schemaType == SchemaType.MySQL) {
            this.jdbcDS = new BasicDataSource();
            this.jdbcDS.setDriverClassName(com.mysql.jdbc.Driver.class
                    .getName());
            this.jdbcDS.setUrl(url);
            this.jdbcDS.setUsername(this.userName.getRenderValue(rc));
            this.jdbcDS.setPassword(this.password.getRenderValue(rc));
            // this.jdbcDS.setTestWhileIdle(true);
            this.jdbcDS.setValidationQuery("select 1");
            this.jdbcDS.setTestWhileIdle(true);

            this.jdbcDS.setMaxActive(30);
            this.jdbcDS.setMinIdle(5);
            this.jdbcDS.setInitialSize(4);


//			this.rawDataSource = new MysqlDataSource();
//			this.rawDataSource.setUser(this.userName.getRenderValue(rc));
//			this.rawDataSource.setURL(url);
//			//this.rawDataSource.setAutoReconnectForPools(true);
//			this.rawDataSource.setPassword(this.password.getRenderValue(rc));

        } else if (this.schemaType == SchemaType.PgCock) {
            this.jdbcDS = new BasicDataSource();
            this.jdbcDS.setDriverClassName(org.postgresql.Driver.class
                    .getName());
            this.jdbcDS.setUrl(url);
            this.jdbcDS.setUsername(this.userName.getRenderValue(rc));
            this.jdbcDS.setPassword(this.password.getRenderValue(rc));
            // this.jdbcDS.setTestWhileIdle(true);
            // this.jdbcDS.setValidationQuery("select 1");
            this.jdbcDS.setTestWhileIdle(true);

            this.jdbcDS.setMaxActive(30);
            this.jdbcDS.setMinIdle(5);
            this.jdbcDS.setInitialSize(4);

        }
    }

    @Override
    protected void loadChildren(Site site) throws IOException {

        super.loadChildren(site);
        /*
         * try { this.load(site); } catch (Exception e) {
         * log.error("Error while init datasource:{}", this.getId(), e); }
         */
    }

    private Schema getStoredSchema(String name) {
        VolatileBag<XEnum> vb = this.items.get(name);
        if (vb != null && vb.getValue() instanceof Schema) {
            return (Schema) vb.getValue();
        }
        return null;
    }

    // TODO:delete this
    private void schemaBuilded(Schema s, Site site) {

        Schema schema = this.getStoredSchema(s.name);
        s.dataSource = site.getXEnumBag(this.getId());
        // s.parentId = this.getId();
        s.setParentIdRecursive(this.getId());
        if (schema != null) {
            if (schema.removed) {
                return;
            } else {

                for (String fn : schema.items.keySet()) {
                    if (!s.items.containsKey(fn)) {
                        if (this.schemaType != SchemaType.Search || fn.indexOf('_') <= 0) {
                            log.info("Cannot find field:{} in schema:{}", fn, schema.getId());
                        }
                    }
                }
                for (String fn : s.items.keySet()) {
                    if (!schema.items.containsKey(fn)) {
                        if (this.schemaType != SchemaType.Search || fn.indexOf('*') <= 0) {
                            log.info("Unloaded field:{} in schema:{}", fn, schema.getId());
                        }
                    }
                }


            }
        } else {
            s.lastModified = System.currentTimeMillis();
            try {
                VolatileBag<XEnum> bag = site.put(s.getId(), s);
                this.addSingleChild(s.name, bag);
                s.store(site);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

        // Schema newschema = s.merge(schema);
        // VolatileBag bag = null;
        // if (newschema != null) {
        //
        // String gn = newschema.globalName;
        // if (gn == null || gn.equals("")) {
        // gn = s.name;
        // }
        // // bag = new VolatileBag<XEnum>(newschema);
        //
        // bag = site.put(gn, newschema);
        // }
        // if (schema == null && bag != null) {
        // this.addChild(s.name, bag);
        // try {
        // newschema.store(site);
        // } catch (IOException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        //
        // }

    }

    private void loadSearch(Site s) {

        //   this.loadSearchSchemas(s);

    }

    private Connection getConnection() throws SQLException {
        if (log.isDebugEnabled()) {
            long c = System.currentTimeMillis();
            //	Connection cn = this.jdbcDS.getConnection();
            Connection cn = this.jdbcDS.getConnection();
            long t = System.currentTimeMillis() - c;
            if (t > 20)
                log.info("Getconnection using:{}ms", t);
            return cn;
        }
        return this.jdbcDS.getConnection();
        //return this.jdbcDS.getConnection();
    }

    private Connection getSlaveConnection() throws SQLException {
        Slaves slaves = this.getChild(Slaves.class);
        if (slaves == null) {
            return getConnection();
        }
        Connection connection = null;
        try {
            connection = slaves.selectConnection();
        } catch (Exception e) {
            log.error("Cannot getTarget slave db connection:{},using master", e, getId());
        }

        if (connection == null) {
            return getConnection();
        }
        return connection;

    }

    public Schema schemaFromDataStore(Schema schema) {
        StringPair s = new StringPair(schema.getTableName(), schema.getScreenName() + "," + schema.comment);
        if (schema.type == SchemaType.MySQL) {
            return this.loadMysqlSchema(s, schema.site);
//        } else if (schema.type == SchemaType.Search) {
//            return fromSearch(s, schema.site, this);
        } else if (schema.type == SchemaType.PgCock) {
            return this.loadPgCockSchema(s, schema.site);
        }
        return null;
    }

    private Schema loadMysqlSchema(final StringPair t, Site site) {

        final Schema schema = new Schema();
        // s.datasource = ds;
        schema.type = SchemaType.MySQL;
        StringPair names = screenNamefromComment(t.value, t.name);
        schema.screenName = names.name;
        schema.comment = names.value;
        schema.name = t.name;


        this.execute(
                "SELECT  `COLUMN_NAME`,ORDINAL_POSITION, `IS_NULLABLE`,`DATA_TYPE`,`CHARACTER_MAXIMUM_LENGTH`,`COLUMN_COMMENT` "
                        + "FROM `information_schema`.`COLUMNS`  WHERE `TABLE_SCHEMA`=? AND `TABLE_NAME`=? order by ORDINAL_POSITION asc",
                new Object[]{this.getDatabaseName(site), t.name},
                new ResultSetGetter() {
                    @Override
                    public Object process(ResultSet rs) throws SQLException {
                        while (rs.next()) {
                            if (rs.getInt("ORDINAL_POSITION") == 1) {
                                schema.keyFieldName = rs
                                        .getString("COLUMN_NAME");
                                // TODO :这里默认了第一个字段为key
                            }
                            SchemaField sf = new SchemaField();
                            sf.name = rs.getString("COLUMN_NAME");
                            StringPair sp = screenNamefromComment(
                                    rs.getString("COLUMN_COMMENT"), sf.name);
                            sf.screenName = sp.name;
                            sf.comment = sp.value;
                            sf.maxLength = rs
                                    .getLong("CHARACTER_MAXIMUM_LENGTH");
                            sf.indexed = true;
                            sf.stored = true;
                            sf.tokenized = false;

                            sf.setSort(rs.getInt("ORDINAL_POSITION"));

                            String datatype = rs.getString("DATA_TYPE");
                            sf.type = SchemaFieldType.fromSqlType(datatype);
                            if (sf.type == null) {
                                log.warn(
                                        "Cannot find schema type:{} ,table:{}",
                                        datatype, t.name);
                                sf.type = SchemaFieldType.Text;
                            }


                            schema.addField(sf.name, sf);
                        }

                        return null;
                    }
                }
        );
        return schema;
    }

    private void loadMysql(final Site site) {
        // this.lastModified = System.currentTimeMillis();

        List<StringPair> tables = this.getTableNames(this.getDatabaseName(site));
        for (final StringPair t : tables) {
            Schema schema = this.loadMysqlSchema(t, site);
            schemaBuilded(schema, site);
        }

    }

    private void loadPgSql(final Site site) {

        List<StringPair> tables = this.getPgCockTableNames(this.getDatabaseName(site));
        for (final StringPair t : tables) {
            Schema schema = this.loadPgCockSchema(t, site);
            schemaBuilded(schema, site);
        }
    }

    private String getDatabaseName(Site site) {
        String url = this.url.getRenderValue(new RenderContext(site));
        int i = url.lastIndexOf("/");
        if (i <= 0) {
            return null;
        }
        int j = url.indexOf("?", i + 1);
        if (j > 0) {

            return url.substring(i + 1, j);
        }
        return url.substring(i + 1);

    }

    @SuppressWarnings("unchecked")
    List<StringPair> getTableNames(String dbname) {
        return (List<StringPair>) this
                .execute(
                        "select TABLE_NAME,TABLE_COMMENT from information_schema.tables where  TABLE_SCHEMA=?",
                        new Object[]{dbname},
                        new ResultSetGetter() {

                            @Override
                            public Object process(ResultSet rs)
                                    throws SQLException {
                                List<StringPair> l = new ArrayList<StringPair>();
                                while (rs.next()) {

                                    String table = rs.getString(1);
                                    if (schemainclude != null) {
                                        if (!schemainclude.matcher(table)
                                                .matches()) {
                                            continue;
                                        }
                                    }

                                    l.add(new StringPair(table, rs
                                            .getString(2)));
                                }
                                return l;
                            }
                        }
                );

    }

    List<StringPair> getPgCockTableNames(String dbname) {
        return (List<StringPair>) this
                .execute(
                        "select TABLE_NAME,'' from information_schema.tables where  TABLE_SCHEMA=?",
                        new Object[]{dbname},
                        new ResultSetGetter() {

                            @Override
                            public Object process(ResultSet rs)
                                    throws SQLException {
                                List<StringPair> l = new ArrayList<StringPair>();
                                while (rs.next()) {

                                    String table = rs.getString(1);
                                    if (schemainclude != null) {
                                        if (!schemainclude.matcher(table)
                                                .matches()) {
                                            continue;
                                        }
                                    }

                                    l.add(new StringPair(table, rs
                                            .getString(2)));
                                }
                                return l;
                            }
                        }
                );

    }

    public int execute(String sql, final Object[] params) {
        final IntBag ib = new IntBag();
        this.execute(sql, new PreparedStatmentHandler() {

            @Override
            public void handle(PreparedStatement stat) throws SQLException {

                setPreparedStatementConvereted(stat, params);


                ib.value = stat.executeUpdate();

            }

            public String toString() {
                return Strings.toString(params);
            }


        });
        return ib.value;
    }

    /**
     * 不能嵌套！！
     *
     * @throws SQLException
     */

    // public void startTransaction() throws SQLException {
    // Connection c = this.getConnection();
    // c.setAutoCommit(false);
    //
    // this.threadlocalConnection.set(c);
    // }
    //
    // public void commitTransaction() throws SQLException {
    // Connection c = this.threadlocalConnection.getTarget();
    // try {
    // c.commit();
    // }
    //
    // finally {
    // this.threadlocalConnection.set(null);
    // c.close();
    // }
    //
    // }
    //
    // public void rollbackTransaction() throws SQLException {
    // Connection c = this.threadlocalConnection.getTarget();
    // if (c == null) {
    // return;
    //
    // }
    // try {
    // c.rollback();
    // }
    //
    // finally {
    // this.threadlocalConnection.set(null);
    // c.close();
    // }
    // }
    public void transaction(Transaction t) throws SQLException {

        //处理递归调用的情况
        Connection connection = this.threadlocalConnection.get();
        if (connection != null) {
            try {
                t.doInTransaction();
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return;
        }


        Connection c = this.getConnection();
        boolean oldac = c.getAutoCommit();
        c.setAutoCommit(false);


        this.threadlocalConnection.set(c);

        try {
            t.doInTransaction();
            c.commit();
            // this.commitTransaction();

        } catch (RuntimeException r) {
            c.rollback();
            throw r;
        } catch (Exception e) {

            c.rollback();

            throw new java.lang.RuntimeException(e);
        } finally {
            this.threadlocalConnection.set(null);
            if (oldac) { /*
             * set back old value because a pooled connection do not
             * really close
             */
                c.setAutoCommit(oldac);
            }
            c.close();
        }


    }

    public Object runOnSlave(boolean runOnSlave, QueryTransaction t) {


        Connection connection = this.threadlocalConnectionSlave.get();
        if (connection != null || runOnSlave == false) {
            try {
                return t.doInTransaction();
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }


        Connection c = null;
        try {
            c = this.getSlaveConnection();
        } catch (SQLException e) {
            log.warn("Error while getTarget slave connection", e);

        }


        this.threadlocalConnectionSlave.set(c);

        try {
            return t.doInTransaction();


        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {


            throw new java.lang.RuntimeException(e);
        } finally {
            this.threadlocalConnectionSlave.set(null);

            if (c != null) {
                try {
                    c.close();
                } catch (SQLException e) {

                    log.warn("Error while getTarget slave connection", e);
                }
            }
        }

    }

    public void execute(String sql, PreparedStatmentHandler ph) {
        this.execute(sql, ph, false);
    }

    public void execute(String sql, PreparedStatmentHandler ph,
                        boolean generateKey) {
        if (log.isDebugEnabled()) {
            log.debug("{}, SQL:{} ,values:{} ", this.getId(), sql, ph);
        }

        java.sql.Connection conn = null;

        java.sql.PreparedStatement stat = null;
        boolean close = false;
        // ResultSet rs = null;
        try {

            conn = this.threadlocalConnection.get();
            if (conn == null) {
                conn = getConnection();
                close = true;
            }


            stat = conn.prepareStatement(sql,
                    generateKey ? PreparedStatement.RETURN_GENERATED_KEYS
                            : PreparedStatement.NO_GENERATED_KEYS
            );

            try {
                ph.handle(stat);
            } catch (com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException e) {
                this.checkCreateTable(e, conn);
                ph.handle(stat);
                // if(e.getMessage())
            }

        } catch (SQLIntegrityConstraintViolationException e) {
            log.warn("{}, SQL:{} ,values:{} ", this.getId(), sql, ph);
            throw new IntegrityConstraintViolationException(e);

        } catch (SQLException e) {
            log.warn("{}, SQL:{} ,values:{} ", this.getId(), sql, ph);
            throw new java.lang.RuntimeException(e.getMessage(), e);
        } finally {


            if (stat != null) {
                try {
                    stat.close();
                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if (conn != null && close) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

    }

    private void checkCreateTable(com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException e, Connection connection) throws SQLException {

        int code = e.getErrorCode();

        if (code == 1146) {
            String s = e.getMessage();
            String begin = "Table '";
            String end = "' doesn't exist";
            int idx = s.indexOf(end);
            if (s.startsWith(begin) && idx > 0) {
                String tablename = s.substring(begin.length(), idx);
                int __idx = tablename.indexOf("__");
                if (__idx > 0) {
                    String otable = tablename.substring(0, __idx);
                    Statement statement = connection.createStatement();
                    try {
                        statement.execute(new StringBuilder().append("Create table   IF NOT EXISTS ").append(tablename)
                                .append(" like ").append(otable).toString());
                        return;
                    } finally {


                        statement.close();
                    }


                }
            }
        }
        throw e;
    }

    public Object execute(String sql, Object[] args, final ResultSetGetter rsg) {
        return this.execute(sql, args, 0, rsg);
    }

    public void setPreparedStatementConvereted(PreparedStatement preparedStatement, Object[] args) throws SQLException {
        if (args == null) {
            return;
        }
        if (this.schemaType == SchemaType.PgCock) {

            SchemaAccessPgCock.setPreparedStatementConvereted(preparedStatement, args);

            return;
        }
        for (int i = 0; i < args.length; i++) {
            preparedStatement.setObject(i + 1, args[i]);
        }
    }

    public Object execute(String sql, Object[] args, int fetchSize,
                          final ResultSetGetter rsg) {


        java.sql.Connection conn = null;

        java.sql.PreparedStatement stat = null;
        ResultSet rs = null;
        boolean close = false;
        String statusInfo = "master";
        try {

            conn = this.threadlocalConnection.get();
            if (conn == null) {
                conn = this.threadlocalConnectionSlave.get();
                if (conn == null) {
                    conn = getConnection();
                    close = true;
                } else {
                    statusInfo = "on slave";
                }
            } else {
                statusInfo = "in transaction";
            }

            if (log.isDebugEnabled()) {
                log.debug("{},{}, SQL:{} args:{}", this.getId(), statusInfo, sql, Strings.arrayToString(args));

            }

            stat = conn.prepareStatement(sql);
            stat.setFetchSize(fetchSize);
            if (args != null) {

                setPreparedStatementConvereted(stat, args);
//
//                for (int i = 0; i < args.length; i++) {
//                    stat.setObject(i + 1, args[i]);
//
//                }
            }

            try {


                rs = stat.executeQuery();
                return rsg.process(rs);
            } catch (com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException e) {
                int code = e.getErrorCode();

                /**
                 * 分表查询表不存在时，default到默认表
                 */
                if (code == 1146) {
                    String s = e.getMessage();
                    String begin = "Table '";
                    String end = "' doesn't exist";
                    int idx = s.indexOf(end);
                    if (s.startsWith(begin) && idx > 0) {
                        String tablename = s.substring(begin.length(), idx);
                        int dotidx = tablename.indexOf('.');
                        if (dotidx > 0) {
                            tablename = tablename.substring(dotidx + 1);
                        }
                        int __idx = tablename.indexOf("__");
                        if (__idx > 0) {
                            String otable = tablename.substring(0, __idx);


                            sql = StringUtils.replace(sql, tablename, otable);


                            stat.close();

                            stat = conn.prepareStatement(sql);
                            stat.setFetchSize(fetchSize);
                            if (args != null) {
                                for (int i = 0; i < args.length; i++) {
                                    stat.setObject(i + 1, args[i]);
                                }
                            }


                            rs = stat.executeQuery();
                            return rsg.process(rs);

                        }
                    }
                }
                throw e;
            }


        } catch (SQLIntegrityConstraintViolationException e) {
            throw new IntegrityConstraintViolationException(e);

        } catch (SQLException e) {
            log.warn("{},{}, SQL:{} args:{}", this.getId(), statusInfo, sql, Strings.arrayToString(args));
            // log.error("Execute SQL:{}", sql);
            throw new java.lang.RuntimeException(e);
        } finally {

            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if (stat != null) {
                try {
                    stat.close();
                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if (conn != null && close) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

    }

    public Object execute(String sql, ResultSetGetter rsg) {
        return this.execute(sql, null, rsg);

    }

    @Override
    protected void init(Site site) {
        // TODO Auto-generated method stub
        super.init(site);
        try {
            this.initDs(site);
        } catch (Exception e) {
            log.error("Error while init datasource {}", this.getId(), e);
        }

    }

    @Override
    public String[] mightChildTypes() {

        return new String[]{Slaves.class.getSimpleName(), Schema.class.getSimpleName()};
    }

    public interface Transaction {
        void doInTransaction() throws Exception;
    }

    public interface QueryTransaction {
        Object doInTransaction() throws Exception;
    }

    public interface ResultSetGetter {
        Object process(ResultSet rs) throws SQLException;
    }

    public interface PreparedStatmentHandler {
        void handle(PreparedStatement stat) throws SQLException;
    }
}
