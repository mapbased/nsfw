package com.mapkc.nsfw.query;

import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;
import com.mapkc.nsfw.FKNames;
import com.mapkc.nsfw.model.RenderContext;
import com.mapkc.nsfw.model.Schema;
import com.mapkc.nsfw.model.SchemaField;
import com.mapkc.nsfw.util.AsConvert;
import com.mapkc.nsfw.util.Sort;
import com.mapkc.nsfw.util.StringPair;
import com.mapkc.nsfw.util.TagSplitter;
import com.mapkc.nsfw.util.logging.ESLogger;
import com.mapkc.nsfw.util.logging.Loggers;
import com.mapkc.nsfw.vl.ValueList;
import groovy.json.JsonSlurper;

import java.lang.reflect.Field;
import java.util.*;

public abstract class QueryResult implements java.util.Enumeration<QueryResult> {


    final static ESLogger log = Loggers.getLogger(QueryResult.class);
    protected ResultRowSet rowset;
    protected RenderContext rc;
    protected ListQueryConfig config;


    protected int treeQueryIndex = -1;


    /**
     * 当前的查询条件 key,为显示内容，value：remove url ;<br>
     * 先计算了，如果没调用，有点浪费
     */
    protected List<StringPair> querys = new LinkedList<StringPair>();
    protected java.util.Iterator<ResultRow> it;

    protected ResultRow currentRow;


    protected Object rowObj;

    /**
     * SchemaName,ResultMapSet
     */
    protected Map<String, ResultMapSet> wrappedData = new TreeMap<String, ResultMapSet>();
    protected Map<String, ResultMapSet> secondLevelWrappedData;


    public abstract int getTotal();

    public boolean isEmpty() {
        return this.rowset == null || this.rowset.getRows().size() == 0;
    }

    public abstract int getTime();

    public LinkItem getFacet(String fieldname) {
        return null;
    }

    public LinkItem getFacet(String fieldname, String schemaName,
                             final String schemaFieldName) {
        return null;
    }

    public boolean hasFacet(String fieldname) {
        return false;
    }


    @Override
    public boolean hasMoreElements() {
        if (it == null) {
            return false;
        }
        return it.hasNext();
        // return true;
    }

    @Override
    public QueryResult nextElement() {
        this.currentRow = it.next();
        this.updateRowObj(this.config);
        return this;
    }

    public void reset() {

        this.it = this.rowset.getRows().iterator();
    }

    public Object get(String fieldName) {
        if (this.currentRow == null) {
            return null;
        }
        return this.currentRow.getField(fieldName);
    }

    public Object getIfEmpty(String fieldName,
                             String schemaName, String schemaField, String defaultValue) {

        Object o = this.get(fieldName, schemaName, schemaField);
        if (o == null || o.toString().length() == 0) {
            return defaultValue;
        }
        return o;

    }

    public Object getIfEmpty(String fieldName,
                             String schemaName, String schemaField,
                             String schemaName2, String schemaField2,

                             String defaultValue) {

        Object o = this.get(fieldName, schemaName, schemaField, schemaName2, schemaField2);
        if (o == null || o.toString().length() == 0) {
            return defaultValue;
        }
        return o;

    }

    public Object getIfEmpty(String fieldName, String defaultValue) {
        Object o = this.get(fieldName);
        if (o == null || o.toString().length() == 0) {
            return defaultValue;
        }
        return o;

    }

    /**
     * 获取相应字段的schemaField mapped value
     *
     * @param fieldName
     * @return
     */
    public String getMapped(String fieldName) {
        Object o = this.get(fieldName);
        if (o == null) {
            return null;
        }
        Schema sc = this.getQueryConfig().getSchema(rc);
        if (sc == null) {
            return String.valueOf(o);
        }
        SchemaField sf = sc.getField(fieldName);
        if (sf == null) {
            return String.valueOf(o);
        }
        return sf.mapValue(o.toString(), rc);
    }

    public String getMapped(String fieldName, String sep) {
        Object o = this.get(fieldName);
        if (o == null) {
            return null;
        }
        Schema sc = this.getQueryConfig().getSchema(rc);
        if (sc == null) {
            return String.valueOf(o);
        }
        SchemaField sf = sc.getField(fieldName);
        if (sf == null) {
            return String.valueOf(o);
        }
        if (sf.valueList != null) {
            String[] ss = String.valueOf(o).split(sep);
            StringBuilder stringBuilder = new StringBuilder();
            for (String s : ss) {
                String ts = s.trim();
                if (ts.length() == 0) {
                    continue;
                }
                String v = sf.valueList.getScreenNameByValue(ts, rc);
                if (v == null) {
                    v = ts;
                }
                stringBuilder.append(v).append(sep);

            }
            return stringBuilder.toString().trim();
        }
        return String.valueOf(o);
        //return sf.mapValue(o.toString(), rc);
    }

    public String getMapped(String fieldName, String schemaName,
                            String schemaField) {
        Object o = this.get(fieldName, schemaName, schemaField);
        if (o == null) {
            return null;
        }
        Schema sc = rc.getSite().getSchema(schemaName);
        SchemaField sf = sc.getField(schemaField);
        if (sf != null) {
            return sf.mapValue(o.toString(), rc);
        }
        return null;


    }

    public String getHighlight(String fieldName) {
        if (this.currentRow == null) {
            return null;
        }
        return this.currentRow.getHighlight(fieldName);
    }

    public String getAsStr(String fieldName) {
        Object o = this.get(fieldName);
        if (o == null) {
            return "";
        }
        return String.valueOf(this.get(fieldName));
    }

    public int getAsInt(String fieldName) {
        Object o = this.get(fieldName);
        return AsConvert.asInt(o, 0);
    }

    public int getAsInt(String fieldName, String schemaName, String schemaField) {
        Object o = this.get(fieldName, schemaName, schemaField);
        return AsConvert.asInt(o, 0);
    }

    public long getAsLong(String fieldName) {
        Object o = this.get(fieldName);
        return AsConvert.asLong(o, 0);
    }

    public List getJsonArray(String fieldName) {
        Object o = this.get(fieldName);
        if (o == null) {
            return Collections.emptyList();
        }
        return (List) new JsonSlurper().parseText(o.toString());
        // return String.valueOf(this.get(fieldName));
    }

    public Map getJsonObj(String fieldName) {
        Object o = this.get(fieldName);
        if (o == null) {
            return Collections.emptyMap();
        }
        return (Map) new JsonSlurper().parseText(o.toString());
        // return String.valueOf(this.get(fieldName));
    }


    public boolean getAsBool(String fieldName) {
        return AsConvert.asBool(fieldName, false);
    }

    /**
     * fieldName 的值作为参数到schemaName对应的schema中获取schemaField字段的值
     * 典型用例，在列表页面，显示用户是否已经关注该问题
     * <p/>
     * getTarget("questionId","/ds/wenda/question_follow_by_user","id","uid=123"
     *
     * @param fieldName
     * @param schemaName
     * @param schemaField
     * @param where
     * @return
     */
    public Object get(String fieldName, String schemaName, String schemaField,
                      String where) {

        return null;

    }

    protected Schema getSchema() {
        return this.config.getSchema(rc);
    }

    protected String mapValue(String fieldName, String fieldValue) {
        // System.out.println("map value:" + fieldName + "---" + fieldValue);
        Schema sc = this.getSchema();
        if (sc == null) {
            return fieldValue;
        }

        SchemaField sf = sc.getField(fieldName);

        if (sf != null) {
            if (sf.hasJoinField()) {
                ResultMapSet rms = this.wrappedData.get(sf.joinSchema.getValue()
                        .getId());
                if (rms != null) {
                    ResultRow rr = rms.getRowById(fieldValue);
                    if (rr != null) {
                        Object o = rr.getField(sf.joinField);
                        if (o != null) {
                            return o.toString();
                        }
                    }
                }
            }
            return sf.mapValue(fieldValue, rc);
        }
        return fieldValue;
    }

    public Object getCollected(String fieldName, String schemaName,
                               String schemaField) {
        return this.get(fieldName, schemaName, schemaField);
    }

    /**
     * 收集按列存储的属性表的内容
     *
     * @param fieldName      ：使用主查询的的这个字段取值
     * @param schemaName     ：到这个schema获取，这个schema一般命名以 _attr 结尾
     * @param attrNameField  ：attrschema中标识名字的字段
     * @param AttrValueField ：attrschema中表示字段值的字段
     */
    public void collectAttributes(String fieldName, String schemaName, String attrNameField, String AttrValueField) {
        Schema sc = rc.getSite().getSchema(schemaName);

        if (sc == null) {
            throw new java.lang.NullPointerException("Cannot find schema:" + schemaName);

        }

        Map<String, ResultRow> keys = new java.util.HashMap<String, ResultRow>();
        for (ResultRow rr : this.rowset.getRows()) {
            Object o = rr.getField(fieldName);
            if (o != null) {

                keys.put(o.toString(), null);
            }


        }
        if (keys.size() < 1) {
            return;
        }
        String[] ks = keys.keySet().toArray(new String[keys.size()]);
        StringBuilder sb = new StringBuilder(sc.getKeyFieldName()).append(" in(");
        for (String s : ks) {
            sb.append("?,");
        }
        sb.setCharAt(sb.length() - 1, ')');
        List<Object[]> rr = sc.listObjectBySql(new String[]{sc.getKeyFieldName(), attrNameField, AttrValueField}, Object[].class,
                sb.toString(), ks);

        MapBasedResultMapSet mbrms = new MapBasedResultMapSet();
        for (Object[] ss : rr) {
            mbrms.addField(String.valueOf(ss[0]), String.valueOf(ss[1]), ss[2]);
        }
        if (null != this.wrappedData.put(schemaName, mbrms)) {
            throw new java.lang.RuntimeException(
                    "wrappedData can only store one schema:" + schemaName);

        }

    }


    /**
     * 拿fieldname的多个值到schemaName对应的schema中获取schemaFields对应的字段。同时使用where条件，
     * 和args参数
     */

    public void collect(String fieldName, String schemaName,
                        java.util.List<String> schemaFields, String keyField, String where,
                        Object[] args) {
        String[] ss = schemaFields == null ? null : schemaFields
                .toArray(new String[schemaFields.size()]);
        this.collect(fieldName, schemaName, ss, keyField, where, args);

    }

    public void collect(String fieldName, String schemaName,
                        String[] schemaFields, String keyField, String where, Object[] args) {
        this.collect(new FieldNameIdsGetter(fieldName), schemaName, schemaFields, keyField, where, args);
    }


    /**
     * 使用idg收集任意字段的值，并作处理，处理成id。拿这些id到schemaName对应的schema中根据keyField和where
     * 拿相应的数据.
     * 注意，这里由于指定了keyfield，如果还有其他autowrap的，并且keyfield不一样，会冲突。
     *
     * @param idg
     * @param schemaName
     * @param schemaFieldList
     * @param keyField
     * @param where
     * @param args
     */
    public void collect(IdsGetter idg, String schemaName,
                        List<String> schemaFieldList, String keyField, String where, Object[] args) {
        String[] schemaFields = schemaFieldList == null ? null : schemaFieldList.toArray(new String[schemaFieldList.size()]);
        this.collect(idg, schemaName, schemaFields, keyField, where, args);

    }

    public void collect(IdsGetter idg, String schemaName,
                        String[] schemaFields, String keyField, String where, Object[] args) {
        Schema sc = rc.getSite().getSchema(schemaName);

        if (sc == null) {
            throw new java.lang.NullPointerException("Cannot find schema:" + schemaName);

        }

        if (schemaFields == null || schemaFields.length == 0) {
            schemaFields = new String[]{keyField};
        }
        if (!schemaFields[schemaFields.length - 1].equals(keyField)) {
            String[] nsfs = new String[schemaFields.length + 1];
            System.arraycopy(schemaFields, 0, nsfs, 0, schemaFields.length);
            nsfs[schemaFields.length] = keyField;
            schemaFields = nsfs;
        }
        Map<String, ResultRow> keys = new java.util.HashMap<String, ResultRow>();
        for (ResultRow rr : this.rowset.getRows()) {
            Collection<String> ids = idg.collectIds(rr);
            if (ids == null) {
                continue;
            }
            for (String id : ids) {
                if (id != null)
                    keys.put(id, null);
            }


        }

        ResultMapSet rms = sc.load(schemaFields, keys, keyField, where, args);
        if (null != this.wrappedData.put(schemaName, rms)) {
            throw new java.lang.RuntimeException(
                    "wrappedData can only store one schema:" + schemaName);

        }
    }

    /**
     * fieldName 的值作为Id到schemaName对应的schema中获取schemaField字段的值
     *
     * @param fieldName
     * @param schemaName
     * @param schemaField
     * @return
     */
    public Object get(String fieldName, String schemaName, String schemaField) {
        ResultMapSet rms = this.wrappedData.get(schemaName);
        if (rms == null) {
            return null;
        }
        if (this.currentRow == null) {
            return null;
        }
        String id = String.valueOf(this.currentRow.getField(fieldName));
        ResultRow rr = rms.getRowById(id);
        if (rr == null) {
            return null;
        }
        return rr.getField(schemaField);

    }


    /**
     * 二级级联，比如主题列表，列出用户信息，而用户信息中包含来用户所在公司的id，再通过这个函数获取公司名称
     *
     * @param fieldName
     * @param schemaName
     * @param schemaField
     * @param schemaName2
     * @param schemaField2
     * @return
     */

    public Object get(String fieldName, String schemaName, String schemaField, String schemaName2, String schemaField2) {
        Object o = this.get(fieldName, schemaName, schemaField);
        if (o == null) {
            return null;
        }

        ResultMapSet rms = this.secondLevelWrappedData.get(schemaName2);
        if (rms == null) {
            return null;
        }
        String id = String.valueOf(o);
        ResultRow rr = rms.getRowById(id);
        if (rr == null) {
            return null;
        }
        return rr.getField(schemaField2);

    }


    /**
     * 如果一个字段以空格隔开的方式存储了另外一个schema的多个id，可以使用该方法获取对应的列表
     *
     * @param fieldName
     * @param schemaName
     * @param schemaFields 以逗号的形式隔开多个字段,（数字在mvel书写不方便）
     * @return
     */
    public List<ResultRow> getMultiTag(String fieldName, String schemaName, String schemaFields) {
        return this.getMultiTagA(fieldName, schemaName, schemaFields.split(","));

    }


    /**
     * 如果一个字段以空格隔开的方式存储了另外一个schema的多个id，可以使用该方法获取对应的列表
     *
     * @param fieldName
     * @param schemaName
     * @param schemaFields
     * @return
     */
    List<ResultRow> getMultiTagA(String fieldName, String schemaName, String[] schemaFields) {
        ResultMapSet rms = this.wrappedData.get(schemaName);
        if (rms == null) {
            return null;
        }
        if (this.currentRow == null) {
            return null;
        }
        Object o = this.currentRow.getField(fieldName);
        if (o == null) {
            return null;
        }
        //String id = String.valueOf(o);
        List<String> ls = TagSplitter.split(String.valueOf(o));
        ArrayList result = new ArrayList(ls.size());
        for (String id : ls) {
            ResultRow rr = rms.getRowById(id);
            if (rr != null) {
                result.add(rr);
            }
        }
        return result;

    }

    /**
     * 获取使用使用collect收集的数据，这边可以相应的get出来
     *
     * @param idg
     * @param schemaName
     * @return
     */

    public List<ResultRow> getMultiRow(IdsGetter idg, String schemaName) {

        ResultMapSet rms = this.wrappedData.get(schemaName);
        if (rms == null) {
            return null;
        }
        if (this.currentRow == null) {
            return null;
        }
        Collection<String> ids = idg.collectIds(this.currentRow);
        if (ids == null) {
            return null;
        }
        ArrayList result = new ArrayList(ids.size());
        for (String id : ids) {
            ResultRow rr = rms.getRowById(id);
            if (rr != null) {
                result.add(rr);
            }
        }
        return result;
    }


    public Object get12(String fieldName, String values, String schemas,
                        String fields) {
        return null;

    }


    /**
     * 收集额外的id，一起加入autoWrap
     *
     * @param v
     */
    protected void collectAditionalValues(Map<String, List<String>> v/*
     * schema,ids
     */) {

        // empty
    }

    /**
     * 存在的问题：对于SeQueryResult中的Facet
     * 需要映射成为name时，如果通过schemajoin的方式，相应的schema没有自动加载，如果再list区域也引用了相应的schema就没有问题
     */
    protected void autoWrap() {
        QueryLoader ql = this.config.queryloader;
        if (ql == null) {
            return;
        }
        Map<String, List<String>> additionalIds = new TreeMap<String, List<String>>();
        this.collectAditionalValues(additionalIds);
        for (Map.Entry<String, List<String>> e : ql.schema2Fields.schemaFields
                .entrySet()) {

            List<String> lookfields = e.getValue();
            String schema = e.getKey();
            List<String> loadfields = ql.allSchemas.schemaFields.get(schema);
            Schema schemaobj = rc.getSite().getSchema(schema);
            if (schemaobj == null) {
                log.error("Cannot find schema:{}", schema);
                continue;
            }

            Map<String, ResultRow> ids = new java.util.HashMap<String, ResultRow>();
            List<String> schemaadditionalIds = additionalIds.get(schema);
            if (schemaadditionalIds != null) {
                for (String s : schemaadditionalIds) {
                    ids.put(s, null);
                }
            }


            int cnt = lookfields.size();
            for (ResultRow rr : this.rowset.getRows()) {
                for (int i = 0; i < cnt; i++) {
                    String fldName = lookfields.get(i);
                    Object o = rr.getField(fldName);
                    if (o == null || o.equals("")) {
                        continue;
                    }

                    String sid = String.valueOf(o);
                    if (sid.indexOf(' ') < 0) {
                        ids.put(sid, null);
                    } else {
                        for (String id : TagSplitter.split(sid)) {
                            ids.put(id, null);
                        }
                    }

                }
            }

            ResultMapSet rms = schemaobj.getType().getAccess().load(
                    loadfields.toArray(new String[loadfields.size()]), ids,
                    schemaobj);
            this.wrappedData.put(schema, rms);

        }

        /**
         * Load second leve schemas
         */
        if (ql.secondLevelSchemas != null) {
            this.secondLevelWrappedData = new TreeMap<String, ResultMapSet>();
            for (Map.Entry<String, QueryFields> e : ql.secondLevelSchemas
                    .entrySet()) {
                String schema = e.getKey();
                Schema schemaobj = rc.getSite().getSchema(schema);
                List<String> loadfields = ql.allSchemas.schemaFields.get(schema);
                if (schemaobj == null) {
                    log.error("Cannot find schema:{}", schema);
                    continue;
                }
                Map<String, ResultRow> ids = new java.util.HashMap<String, ResultRow>();
                for (Map.Entry<String, List<String>> sfe : e.getValue().schemaFields.entrySet()) {
                    ResultMapSet rms = this.wrappedData.get(sfe.getKey());
                    for (ResultRow r : rms.getRows()) {
                        if (r != null) {
                            for (String f : sfe.getValue()) {
                                Object o = r.getField(f);
                                if (o != null) {
                                    ids.put(String.valueOf(o), null);
                                }
                            }
                        }
                    }
                }
                //load ids
                ResultMapSet rms = schemaobj.getType().getAccess().load(
                        loadfields.toArray(new String[loadfields.size()]), ids,
                        schemaobj);
                this.secondLevelWrappedData.put(schema, rms);


            }
        }
    }

    public void wrap(String fieldName, String schema, String[] fields) {

    }

    // protected abstract String changeUrl(String pname, String dname, String
    // value);

    protected java.util.Iterator<String> getGETParams() {
        // if (rc.isGet()) {
        // return rc.getParameters().keySet().iterator();
        // } else {
        // 如果列表页面带有form表单，form表单填充数据，会影响url参数
        return rc.getQueryStringParameters().keySet().iterator();

        // }
    }


    public LinkItem emptyLink(String pname) {
        final String fullname = this.config.componentId + "." + pname;
        return new LinkItem() {
            @Override
            public String getLabel() {
                return "全部";
            }

            @Override
            public String getLink() {
                return changeUrl(fullname, null);
            }

            @Override
            public int getCount() {
                return 0;
            }

            @Override
            public boolean isSelected() {
                return rc.hasParam(fullname);
            }

            @Override
            public boolean hasMoreElements() {
                return false;
            }

            @Override
            public LinkItem nextElement() {
                return null;
            }
        };

    }

    /**
     * 生成点击的链接地址
     *
     * @param pname
     * @param value
     * @return
     */


    public String changeUrl(String pname, String value) {
        return changeUrl(pname, value, false);
    }

    public String changeUrl(String pname, String value, boolean keepPage) {
        // boolean keepPage = false;

        StringBuilder ret = new StringBuilder(rc.getUri().length() + 10);
        ret.append(rc.getPath()).append("?");

        java.util.Iterator<String> e = this.getGETParams();// getParameterNames();

        boolean found = false;
        while (e.hasNext()) {
            String p = e.next();
            String v = rc.param(p);
            if (!keepPage) {
                if (p.equals(config.p)) {
                    continue;
                }
            }

            if (p.equals(pname)) {
                found = true;

                addUrlSafe(ret, pname, value);

            } else {
                // ret.append(p).append("=");
                addUrlSafe(ret, p, v);

            }

        }
        if (!found && value != null) {

            addUrlSafe(ret, pname, value);

        }
        if (ret.charAt(ret.length() - 1) == '?') {
            ret.setLength(ret.length() - 1);
        }

        return ret.toString();

    }


    final protected void addUrlSafe(StringBuilder sb, String name, String value) {

        if (value == null) {
            return;
        }
        sb.append(sb.charAt(sb.length() - 1) == '?' ? "" : "&");


        Escaper ec = UrlEscapers.urlFormParameterEscaper();//.escape()

        sb.append(ec.escape(name)).append("=")
                .append(ec.escape(value));


    }

    public String mapLable(String fieldName) {
        Schema sc = config.getSchema(rc);
        if (sc == null) {
            return fieldName;
        }
        SchemaField sf = sc.getField(fieldName);
        if (sf == null) {
            return fieldName;
        }
        return sf.getScreenName();
    }

    public LinkItem getSorts() {
        Sort[] ss = this.config.getSorts();
        if (ss == null) {
            return null;
        }

        return new LinkItem() {
            int i = -1;

            final int si = rc.paramInt(config.sortName, -1);
            final Sort[] ss = config.getSorts();

            @Override
            public LinkItem nextElement() {
                ++i;
                return this;
            }

            @Override
            public boolean hasMoreElements() {
                // TODO Auto-generated method stub
                return i + 1 < ss.length;
            }

            @Override
            public boolean isSelected() {
                // TODO Auto-generated method stub
                return i == si;
            }

            @Override
            public String getLink() {

                return changeUrl(config.sortName, i + "");
            }

            @Override
            public String getLabel() {
                String[] sls = config.sortLables;
                if (sls != null && sls.length > i) {
                    return sls[i];
                }
                return mapLable(ss[i].name) + (ss[i].desc ? "降序" : "升序");
            }

            @Override
            public int getCount() {
                // TODO Auto-generated method stub
                return 0;
            }
        };

    }

    public boolean isExporting() {
        return null != rc.getVar(FKNames.FK_EXPORT_CNT);

    }

    public int getPageSize() {


        Object exportcnt = rc.getVar(FKNames.FK_EXPORT_CNT);
        if (exportcnt != null) {
            return (int) exportcnt;
        }
        return config.getPageSize();
    }

    public boolean hasMorePage() {
        return this.rowset.getRows().size() == getPageSize();
    }

    public LinkItem getPages() {

        int pageIndex = rc.paramInt(config.p, 0);

        int pageCount = getTotal() / getPageSize();
        int plc = config.getPageLabelCount();
        int pc2 = plc / 2;
        int min = Math.max(0, pageIndex - pc2);
        int max = Math.min(pageCount, pageIndex + pc2);
        if (min > pageIndex - pc2) {
            max = Math.max(max, plc - min);
        } else if (max < pageIndex + pc2) {
            min = Math.max(0, Math.min(min, max - plc));
        }

        max = Math.min(pageCount, max);
        final List<Page> pages = new ArrayList<QueryResult.Page>(max - min + 2);
        if (min < max) {
            if (pageIndex > 0) {
                Page p = new Page();
                p.lable = "&laquo;";
                p.link = changeUrl(config.p, String.valueOf(pageIndex - 1));
                pages.add(p);
            }
            for (int i = min; i <= max; i++) {
                Page p = new Page();
                p.lable = String.valueOf(i + 1);
                p.link = changeUrl(config.p, String.valueOf(i));
                p.isSelected = i == pageIndex;
                pages.add(p);

            }
            if (pageIndex < max) {
                Page p = new Page();
                p.lable = "&raquo;";
                p.link = changeUrl(config.p, String.valueOf(pageIndex + 1));
                pages.add(p);
            }

        }
        return new LinkItem() {

            Page p;
            private final java.util.Iterator<Page> i = pages.iterator();

            @Override
            public LinkItem nextElement() {
                p = i.next();
                return this;
            }

            @Override
            public boolean hasMoreElements() {
                return i.hasNext();
            }

            @Override
            public boolean isSelected() {

                return p.isSelected;
            }

            @Override
            public String getLink() {
                return p.link;
            }

            @Override
            public String getLabel() {
                return p.lable;
            }

            @Override
            public int getCount() {
                // TODO Auto-generated method stub
                return 0;
            }
        };

        // 下面这个实现没有问题，只是加上上一页，下一页不太方便
        /*
         * return new LinkItem() {
         *
         *
         * int pageMax; int i;// pageMin - 1;
         *
         * { calPageMinMax(); }
         *
         * void calPageMinMax() { int pageIndex = rc.paramInt(config.p, 0);
         *
         * int pageCount = getTotal() / config.getPageSize(); int plc =
         * config.getPageLabelCount(); int pc2 = plc / 2; int min = Math.max(0,
         * pageIndex - pc2); int max = Math.min(pageCount, pageIndex + pc2); if
         * (min > pageIndex - pc2) { max = Math.max(max, plc - min); } else if
         * (max < pageIndex + pc2) { min = Math.max(0, Math.min(min, max -
         * plc)); } this.i = min - 1; this.pageMax = Math.min(pageCount, max); }
         *
         * @Override public boolean hasMoreElements() { return i < pageMax; //
         * return false; }
         *
         * @Override public LinkItem nextElement() { ++i; return this; }
         *
         * @Override public String getLabel() { // TODO Auto-generated method
         * stub return String.valueOf(i + 1); }
         *
         * @Override public String getLink() { // TODO Auto-generated method
         * stub return changeUrl(config.p, String.valueOf(i)); }
         *
         * @Override public int getCount() { // TODO Auto-generated method stub
         * return 0; }
         *
         * @Override public boolean isSelected() { // TODO Auto-generated method
         * stub return i == rc.paramInt(config.p, 0); }
         *
         * };
         */
    }

    public LinkItem getDefinedQuery(String name) {
        List<QuerysDefine> qds = config.getQuerysDefines();
        if (qds == null) {
            return null;
        }
        for (int i = 0; i < qds.size(); i++) {
            if (qds.get(i).name.equals(name)) {
                return this.getDefinedQuery(i);
            }
        }
        return null;

    }

    /**
     * 为sql制作的根据某个字段模拟类似se的faceted query
     *
     * @param fieldName
     * @return
     */

    public LinkItem getFieldDefinedQuery(String fieldName) {
        return this.getFieldDefinedQuery(fieldName, null);
    }

    public LinkItem getFieldDefinedQueryEx(String fieldName, String groupsql, int cnt, int order) {
        return null;
    }

    /**
     * @param fieldName
     * @param groupsql:如果传入groupsql不为 null，则强制group，否则尽可能不group
     * @return
     */
    public LinkItem getFieldDefinedQuery(String fieldName, String groupsql) {
        Schema sch = this.getSchema();
        if (sch == null) {
            return null;
        }
        SchemaField sf = sch.getField(fieldName);
        if (sf == null) {
            return null;
        }
        ValueList vl = sf.valueList;
        if (vl == null) {
            return null;
        }
        return null;

    }

    //	public LinkItem getDefinedQueryByField(String fieldName) {
//		 Schema sch=this.getSchema();
//		if(sch==null){
//			return null;
//		}
//		SchemaField sf=sch.getField(fieldName);
//		if(sf==null){
//			return null;
//		}
//		ValueList vl= sf.valueList;
//		if(vl==null){
//			return null;
//		}
//
//
//
//
//	}
    public LinkItem getCurrentQueryList() {
        if (this.querys.size() == 0) {
            return null;
        }
        return new LinkItem() {
            int i = -1;

            @Override
            public LinkItem nextElement() {
                ++i;
                return this;
            }

            @Override
            public boolean hasMoreElements() {
                return i + 1 < querys.size();
            }

            @Override
            public boolean isSelected() {
                return false;
            }

            @Override
            public String getLink() {

                return querys.get(i).value;
            }

            @Override
            public String getLabel() {
                StringPair sp = querys.get(i);
                return sp.name;
            }

            @Override
            public int getCount() {
                // TODO Auto-generated method stub
                return 0;
            }
        };
    }

    public QueryConfig getQueryConfig() {
        return this.config;
    }

    public LinkItem getDefinedQuery(final int index) {

        List<QuerysDefine> lqd = config.getQuerysDefines();
        if (lqd == null || lqd.size() == 0) {
            return null;
        }

        return new LinkItem() {

            final QuerysDefine qd = config.getQuerysDefines().get(index);
            final int selectedIndex = rc.paramInt(config.q + index, -1);
            int i = qd.getAllName() == null ? -1 : -2;

            @Override
            public boolean hasMoreElements() {
                // TODO Auto-generated method stub
                return i + 1 < qd.size();
            }

            @Override
            public LinkItem nextElement() {
                i += 1;
                if (i < qd.size()) {

                    return this;
                }
                return null;
            }

            @Override
            public String getLabel() {
                if (i < 0) {
                    return qd.getAllName();
                }
                return qd.get(i).getScreenName();

            }

            @Override
            public String getLink() {

                String s = i < 0 ? null : String.valueOf(i);
                return changeUrl(config.q + index, s);

            }

            @Override
            public int getCount() {
                return -1;

            }

            @Override
            public boolean isSelected() {
                return i == selectedIndex;
            }

        };

    }

    protected void updateRowObj(QueryConfig queryConfig) {
        if (queryConfig.rowClass == null || this.currentRow == null) {
            return;
        }
        try {
            Class rowClass = queryConfig.rowClass;
            if (this.rowObj == null) {
                this.rowObj = rowClass.newInstance();

            }

            Field[] flz = rowClass.getDeclaredFields();
            for (Field field : flz) {
                QueryField queryField = field.getAnnotation(QueryField.class);
                if (queryField != null) {
                    String fn = queryField.field();
                    if (fn.length() == 0) {
                        fn = field.getName();
                    }
                    Object value = null;
                    if (queryField.schema2().length() > 0) {
                        value = this.get(fn, queryField.schema(), queryField.schemaField(), queryField.schema2(), queryField.schemaField2());


                    } else if (queryField.schema().length() > 0) {

                        value = queryField.mapped() ?
                                this.getMapped(fn, queryField.schema(), queryField.schemaField()) :
                                get(fn, queryField.schema(), queryField.schemaField());

                    } else {
                        if (queryField.mapped()) {
                            value = this.getMapped(fn);
                        } else {
                            value = get(fn);
                        }
                    }
                    if (!field.isAccessible())
                        field.setAccessible(true);
                    if (value == null && field.getType().isPrimitive()) {
                        continue;
                    }
                    field.set(rowObj, value);

                }

                // if(field.getTarget)
            }
        } catch (Exception e) {
            throw new RuntimeException(e);

        }

    }

    public Object getRowObj() {
        return rowObj;
    }

    /**
     * 获取整个列表
     *
     * @return
     */
    public List listRowObj() {

        ArrayList list = new ArrayList(this.getPageSize());

        try {
            while (this.hasMoreElements()) {
                this.nextElement();
                list.add(this.getRowObj());
                this.rowObj = config.rowClass.newInstance();


            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return list;

    }

    static class Page {
        String link;
        String lable;
        boolean isSelected;
    }


}
