package com.mapkc.nsfw.query;

import com.mapkc.nsfw.model.*;
import com.mapkc.nsfw.util.Sort;
import com.mapkc.nsfw.util.StringPair;
import com.mapkc.nsfw.util.Strings;
import com.mapkc.nsfw.util.logging.ESLogger;
import com.mapkc.nsfw.util.logging.Loggers;
import com.mapkc.nsfw.vl.Value;
import com.mapkc.nsfw.vl.ValueList;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MysqlQueryResult extends QueryResult {
    final static ESLogger log = Loggers.getLogger(MysqlQueryResult.class);
    long queryTime = System.currentTimeMillis();
    int total = -1;
    Schema schema;
    String countsql;
    String whereWithoutFieldDefinedQuery;

    public MysqlQueryResult(ListQueryConfig config, RenderContext rc) {
        this.config = config;
        this.rc = rc;
        this.schema = config.getSchema(rc);

        StringBuilder count = new StringBuilder("select count(*) from ");
        StringBuilder sb = new StringBuilder();
        String sqlbase = config.getSqlBase(rc);
        boolean haswhere = false;
        if (sqlbase == null) {
            if (this.schema == null) {
                throw new java.lang.RuntimeException(
                        "Cannot find schema or sqlbase:" + config.componentId);
            }

            String listFieldValue = config.getSplitFieldValue(rc);
            String tablename = schema.getSplitedTableName(listFieldValue);

            String[] fields = config.getFields();
            String keyid = schema.getKeyFieldName();
            if (fields.length == 0) {
                fields = new String[]{keyid};
            } else if (fields[0].trim().equals("")) {
                fields = new String[]{tablename + ".*"};
            }

            sb.append("select ");
            if (!keyid.equalsIgnoreCase(fields[0])) {
                sb.append(keyid).append(",");
            }

            for (String s : fields) {
                SchemaField sf = schema.getField(s);
                if (sf != null) {
                    sb.append(sf.convertFieldName(s)).append(",");
                } else {
                    sb.append(s).append(",");
                }
            }
            sb.setLength(sb.length() - 1);
            sb.append(" from ").append(tablename).append(" ");
            count.append(tablename);
        } else {
            haswhere = sqlbase.toLowerCase().indexOf("where") > 0;
            sb.append(sqlbase).append(" ");
            count.append(sqlbase.substring(sqlbase.toLowerCase().indexOf(
                    " from ") + 6));
            // TODO 这块不严密，但有错测试时应该可以测出，实现是应该尽量避免sql文字中含有from
        }

        StringBuilder where = new StringBuilder();

        List<QuerysDefine> qds = this.config.getQuerysDefines();
        boolean endwithand = false;
        if (qds != null) {

            for (int i = 0; i < qds.size(); i++) {
                int qdi = rc.paramInt(config.q + i, -1);
                QuerysDefine qd = qds.get(i);
                if (qdi >= 0) {
                    QuerysDefine.QueryDefine qda = qd.get(qdi);//querys[qdi];
                    this.querys.add(new StringPair(qd.screenName + ":"
                            + qda.getScreenName(), this.changeUrl(config.q + i,
                            null)));
                    where.append(String.valueOf(qda.getQuery(rc)))
                            .append(" and ");
                    endwithand = true;
                    // this.addQuery((Query) qda.getQuery(), queries);
                }

            }
        }


        String fixedq = this.config.getFixedQueryStr(rc);
        if (fixedq != null) {
            where.append(fixedq);
            endwithand = false;
        }
        String q = rc.param(config.q);
        if (q != null) {
            q = q.trim();
            if (q.length() > 0) {
                q = this.makeQuery(q);
                if (q != null) {
                    if (endwithand || where.length() == 0) {
                        where.append(q);
                        endwithand = false;
                    } else {
                        where.append(" and ").append(q);
                    }
                }
            }
        }
        if (endwithand) {
            where.setLength(where.length() - 4);
        }
        this.whereWithoutFieldDefinedQuery = where.toString();
        String fieldDefinedQuery = this.fieldDefinedQuerySql(null);
        if (fieldDefinedQuery != null) {
            if (where.length() == 0) {
                where.append(fieldDefinedQuery);
            } else {
                where.append(" and ").append(fieldDefinedQuery);
            }
        }


        // this.whereCondition=where.toString();
        if (where.length() > 0) {
            String join = haswhere ? " and " : " where ";

            sb.append(join).append(where).append(" ");
            count.append(join).append(where);
        }

		/*
         *
		 * TODO 规则定义清楚 Sort[] fxss=this.config.fixedSorts;
		 * if(fxss!=null&&fxss.length>0){
		 * 
		 * for(Sort s:fxss){
		 * 
		 * } }
		 */
        Sort[] ss = this.config.getSorts();
        Sort toUse = null;
        if (ss != null) {
            int si = rc.paramInt(config.sortName, -1);
            if (si >= 0 && si < ss.length) {
                toUse = ss[si];


            }
        } else if (config.fixedSorts != null && config.fixedSorts.length > 0) {
            toUse = config.fixedSorts[0];
        } else {
            ss = ListQueryConfig.parseSorts(rc.p(this.config.sortName + "a"));
            //TODO :more check for sql safe
            if (ss != null && ss.length > 0) {
                toUse = ss[0];
            }
        }
        if (toUse != null) {
            sb.append(" order by ").append(toUse.name)
                    .append(toUse.desc ? " desc " : " asc ");
        }


        int pageIndex = rc.paramInt(config.p, 0);
        int from = pageIndex * getPageSize();
        int size = getPageSize();

        if (size > 0) {
            sb.append(" limit ").append(size);
        }
        if (from > 0) {
            sb.append(" offset ").append(from);

        }
        this.countsql = count.toString();

        // //////////////////////
        final String sql = sb.toString();
        // log.debug("query sql:{}", sql);

        final DataSource dataSource = config.getDataSource(rc);

        dataSource.runOnSlave(config.runOnSlave, new DataSource.QueryTransaction() {
            @Override
            public Object doInTransaction() throws Exception {

                dataSource.execute(sql,
                        new DataSource.ResultSetGetter() {

                            @Override
                            public Object process(ResultSet rs) throws SQLException {
                                // TODO Auto-generated method stub

                                ResultSetMetaData md = rs.getMetaData();
                                int cc = md.getColumnCount();

                                String[] fields = new String[cc];
                                for (int i = 0; i < cc; i++) {
                                    fields[i] = md.getColumnLabel(i + 1);
                                }
                                ArrayBaseResultRowSet abrr = new ArrayBaseResultRowSet(
                                        fields);

                                while (rs.next()) {

                                    Object[] vs = new Object[cc];
                                    // String id = rs.getString(1);//first col must be
                                    // id
                                    for (int i = 0; i < vs.length; i++) {
                                        vs[i] = rs.getObject(i + 1);
                                    }
                                    abrr.addRow(vs);

                                }
                                rowset = abrr;
                                autoWrap();
                                MysqlQueryResult.this.it = rowset.getRows().iterator();
                                return null;
                            }

                        }
                );
                return null;

            }
        });

    }

    private String fieldDefinedQuerySql(String skipfield) {

        Map<String, VQConfig> fdq = this.config.getFieldDefinedQueries();
        if (fdq != null && schema != null) {
            StringBuilder where = new StringBuilder();
            String cid = this.config.componentId;


            for (String s : fdq.keySet()) {
                if (/*!schema.hasField(s) ||*/ s.equalsIgnoreCase(skipfield)) {
                    continue;
                }
                String pname = s;
                if (cid != null) {
                    pname = cid + "." + s;
                }
                String v = rc.param(pname, "");
                if (v.length() > 0) {
                    where.append(s).append("=\"").append(Strings.escapeSQL(v)).append("\"").append(" and ");
                }

            }
            if (where.length() > 0) {
                where.setLength(where.length() - 4);
                return where.toString();
            } else {
                return null;
            }


        }
        return null;
    }

    @Override
    public LinkItem getFieldDefinedQuery(String fieldName, String groupsql) {
        return this.getFieldDefinedQueryEx(fieldName, groupsql, 100,2);
    }

    /**
     * @param fieldName
     * @param groupsql:解析时用到，这边的代码没有用到
     * @param limit
     * @return
     */

    public LinkItem getFieldDefinedQueryEx(String fieldName, String groupsql, int limit,int order) {
        long now=System.currentTimeMillis();
        Schema sch = this.getSchema();
        if (sch == null) {
            return null;
        }
        final SchemaField sf = sch.getField(fieldName);
//        if (sf == null) {
//            return null;
//        }
        Map<String, VQConfig> fdq = this.config.getFieldDefinedQueries();
        VQConfig vqConfig = fdq == null ? null : fdq.get(fieldName);
        if (vqConfig == null) {
            return null;
        }
        boolean group = false;
        String parsedgroupsql = vqConfig.getGroupSql(rc);
        if (parsedgroupsql != null) {
            group = true;
        }


        final LinkItemContainer linkItemContainer = new LinkItemContainer();
       final ValueList vl = sf == null ? null : sf.valueList;

        /*if (vl == null) {
            group = true;
        } else {
            java.util.Iterator<Value> iterator = vl.iterator(rc);
            while (iterator.hasNext()) {
                linkItemContainer.add(iterator.next());


            }
        }*/
        if (true) {
            if (parsedgroupsql == null || parsedgroupsql.length() <= 2) {

                StringBuilder sb = new StringBuilder("select ").append(fieldName).append(" fld,count(*) fk_cnt from ");
                String sqlbase = config.getSqlBase(rc);
                boolean haswhere = false;
                if (sqlbase != null) {
                    haswhere = sqlbase.toLowerCase().indexOf("where") > 0;
                    sb.append(sqlbase.substring(sqlbase.toLowerCase().indexOf(
                            " from ") + 6));
                } else {

                    String listFieldValue = config.getSplitFieldValue(rc);
                    String tablename = schema.getSplitedTableName(listFieldValue);

                    sb.append(tablename).append(" ");
                }
                if (this.whereWithoutFieldDefinedQuery != null && this.whereWithoutFieldDefinedQuery.length() > 0) {
                    sb.append(haswhere ? " and " : " where ").append(this.whereWithoutFieldDefinedQuery);
                    haswhere = true;
                }
                String fdsql = this.fieldDefinedQuerySql(fieldName);
                if (fdsql != null) {
                    sb.append(haswhere ? " and " : " where ").append(fdsql);
                    haswhere=true;
                }
                String orders=" 2 desc";
                if(order==1){
                    orders=" 1 ";
                }
                else if(order==-1){
                    orders=" 1 desc";
                }
                sb.append(" group by 1 order by ").append(orders);
                if (limit > 0) {
                    sb.append(" limit ").append(limit);
                }
                parsedgroupsql = sb.toString();


            }


            /////////

            ////////
            final String pgroupSql = parsedgroupsql;

            schema.getDataSource().runOnSlave(config.runOnSlave, new DataSource.QueryTransaction() {
                @Override
                public Object doInTransaction() throws Exception {

                    List<Object[]> oss = schema.listObjectByFullSql(Object[].class, pgroupSql, new Object[]{});
                    ResultMapSet rms = null;
                    if (sf != null && sf.hasJoinField()) {
                        Map<String, ResultRow> joinresult = new HashMap<String, ResultRow>();
                        for (Object[] os : oss) {

                            joinresult.put(String.valueOf(os[0]), null);
                        }


                        rms = sf.joinSchema.getValue().load(new String[]{sf.joinField}, joinresult);
                    }

                    for (Object[] os : oss) {

                        if(os[0]==null){
                            continue;
                        }
                        String id = String.valueOf(os[0]);

                        String screenname = id;
                        int cnt = ((Number) os[1]).intValue();
                        if (rms != null) {
                            ResultRow row = rms.getRowById(id);
                            if (row != null) {
                                Object o = row.getField(sf.joinField);
                                if (o != null) {
                                    screenname = o.toString();
                                }
                            }
                        }else if(vl!=null){
                            screenname=vl.getScreenNameByValue(id,rc);
                            if(screenname==null){
                                screenname=id;
                            }

                        }

                        linkItemContainer.add(screenname, id, cnt);
                    }


                    return null;

                }
            });


        }

        //log.info("统计耗时：{} -{}",fieldName,System.currentTimeMillis()-now);
        return linkItemContainer.linkItem(this, fieldName);

    }

    protected String makeQuery(String q) {
        rc.setVar("q", q);
        Renderable r = this.config.getKeywordQuery();
        if (r != null) {
            return r.getRenderValue(rc);
        }

        log.error("you need provide a keywordquery or overwrite makequery method");
        // TODO not a sql;
        return null;
        // return q;
    }

    /**
     * 数据库访问select stat有三处重复了，如果还有更多，可考虑抽象
     */
    @Override
    public int getTotal() {
        if (this.total < 0) {
            String cntsql = this.countsql;
            if (config.sqlinfo.countSql != null) {
                cntsql = config.sqlinfo.countSql.getRenderValue(rc);
            }
            // log.debug("count sql:{}", this.countsql);

            final String csql = cntsql;
            final DataSource dataSource = config.getDataSource(rc);
            dataSource.runOnSlave(config.runOnSlave, new DataSource.QueryTransaction() {
                @Override
                public Object doInTransaction() throws Exception {
                    dataSource.execute(csql,
                            new DataSource.ResultSetGetter() {

                                @Override
                                public Object process(ResultSet rs) throws SQLException {
                                    if (rs.next()) {

                                        total = rs.getInt(1);
                                        return total;

                                    }
                                    return 0;
                                }
                            });
                    return null;
                }
            });


        }
        return this.total;
    }

    @Override
    public int getTime() {
        // TODO Auto-generated method stub
        return (int) (System.currentTimeMillis() - this.queryTime);
    }

}
