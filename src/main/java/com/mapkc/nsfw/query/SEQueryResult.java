//package com.mapkc.nsfw.query;
//
//import com.mapbased.search.req.Query;
//import com.mapbased.search.req.Resp;
//import com.mapbased.search.req.misc.*;
//import com.mapbased.search.req.query.*;
//import com.mapbased.search.req.routing.QueryReq;
//import com.mapbased.search.req.routing.QueryResp;
//import com.mapkc.nsfw.model.RenderContext;
//import com.mapkc.nsfw.model.Renderable;
//import com.mapkc.nsfw.model.Schema;
//import com.mapkc.nsfw.model.SchemaField;
//import com.mapkc.nsfw.query.QuerysDefine.QueryDefine;
//
//import java.util.*;
//
//public class SEQueryResult extends QueryResult {
//
//    QueryResp resp;
//    Schema schema;
//    private Map<String, String> facetQuerys;
//
//
//    public String getFacetQuery(String name) {
//        return this.facetQuerys.getTarget(name);
//    }
//
//    public SEQueryResult(ListQueryConfig config, RenderContext rc) {
//        this.fill(config, rc);
//
//    }
//
//    @Override
//    public boolean hasFacet(String fieldname) {
//        if (this.resp == null || this.resp.facets == null) {
//            return false;
//        }
//        for (final Facet f : this.resp.facets) {
//            if (f.fieldName.equals(fieldname)) {
//                return f.items.length > 0;
//
//            }
//        }
//        return false;
//
//    }
//
//    public List<StringPair> getFacets() {
//        if (this.resp == null || this.resp.facets == null) {
//            return null;
//        }
//        List<StringPair> rt = new ArrayList<>(resp.facets.length);
//        for (final Facet f : this.resp.facets) {
//            if (f.items == null || f.items.length <= 1) {
//                continue;
//            }
//            rt.add(new StringPair(this.mapLable(f.fieldName), f.fieldName));
//        }
//        return rt;
//
//    }
//
//    @Override
//    public LinkItem getFacet(String fieldname) {
//
//        if (this.resp == null || this.resp.facets == null) {
//            return null;
//        }
//        for (final Facet f : this.resp.facets) {
//            if (f.fieldName.equals(fieldname)) {
//                LinkItem li = new LinkItem() {
//
//                    int i = -1;
//
//                    @Override
//                    public boolean hasMoreElements() {
//                        // TODO Auto-generated method stub
//                        return i + 1 < f.items.length;
//                    }
//
//                    @Override
//                    public LinkItem nextElement() {
//                        i += 1;
//                        if (i < f.items.length) {
//
//                            return this;
//                        }
//                        return null;
//                    }
//
//                    @Override
//                    public String getLabel() {
//
//                        return SEQueryResult.this.mapValue(f.fieldName,
//                                f.items[i].name);
//
//                    }
//
//                    @Override
//                    public String getLink() {
//
//                        return SEQueryResult.this.changeUrl(config.fq,
//                                f.fieldName,
//                                f.items[i].name);
//
//                    }
//
//                    @Override
//                    public int getCount() {
//                        // TODO Auto-generated method stub
//                        return f.items[i].value;
//                    }
//
//                    @Override
//                    public boolean isSelected() {
//
//                        String k = f.items[i].name;
//                        if (k == null) {
//                            return false;
//                        }
//                        return k.equals(facetQuerys.getTarget(f.fieldName));
//                    }
//
//                };
//                return li;
//            }
//        }
//        return null;
//
//    }
//
//
//    @Override
//    public LinkItem getFacet(String fieldname, String schemaName,
//                             final String schemaFieldName) {
//
//        if (this.resp == null || this.resp.facets == null) {
//            return null;
//        }
//        for (final Facet f : this.resp.facets) {
//            if (f.fieldName.equals(fieldname)) {
//                Schema sc = rc.getSite().getSchema(schemaName);
//                if (sc == null) {
//                    throw new java.lang.RuntimeException("Cannot find schema:" + schemaName);
//                }
//                Map<String, ResultRow> m = new HashMap<String, ResultRow>();
//                for (StrIntBag si : f.items) {
//                    m.put(si.name, null);
//                }
//
//                final ResultMapSet rms = sc.load(
//                        new String[]{schemaFieldName}, m, rc);
//                LinkItem li = new LinkItem() {
//
//                    int i = -1;
//
//                    @Override
//                    public boolean hasMoreElements() {
//                        // TODO Auto-generated method stub
//                        return i + 1 < f.items.length;
//                    }
//
//                    @Override
//                    public LinkItem nextElement() {
//                        i += 1;
//                        if (i < f.items.length) {
//
//                            return this;
//                        }
//                        return null;
//                    }
//
//                    @Override
//                    public String getLabel() {
//                        String r = f.items[i].name;
//                        ResultRow rr = rms.getRowById(r);//
//                        if (rr == null) {
//                            return r;
//                        }
//                        return String.valueOf(rr.getField(schemaFieldName));
//                        // return SEQueryResult.this.mapValue(f.fieldName,
//                        // );
//
//                    }
//
//                    @Override
//                    public String getLink() {
//                        // TODO Auto-generated method stub
//                        return SEQueryResult.this.changeUrl(config.fq,
//                                f.fieldName, f.items[i].name);
//
//                    }
//
//                    @Override
//                    public int getCount() {
//
//                        return f.items[i].value;
//                    }
//
//                    @Override
//                    public boolean isSelected() {
//
//                        String k = f.items[i].name;
//                        return k.equals(facetQuerys.getTarget(f.fieldName));
//
//                    }
//
//                };
//                return li;
//            }
//        }
//        return null;
//
//    }
//
//    static void addQuery(Query qda, List<Query> queries) {
//        if (qda != null)
//            queries.add(qda);
//    }
//
//    protected void processDefinedQuerys(List<Query> queries) {
//        List<QuerysDefine> qds = this.config.getQuerysDefines();
//        if (qds == null) {
//            return;
//        }
//        for (int i = 0; i < qds.size(); i++) {
//            int qdi = rc.paramInt(config.q + i, -1);
//            QuerysDefine qd = qds.getTarget(i);
//            if (qdi >= 0) {
//                QueryDefine qda = qd.getTarget(qdi);//.querys[qdi];
//                this.querys.add(new StringPair(qd.screenName, qda
//                        .getScreenName()));
//                Object o = qda.getQuery(rc);
//                Query definedq;
//                if (o instanceof String) {
//                    definedq = new ExpressionQuery(o.toString());
//                } else {
//                    definedq = (Query) o;
//                }
//                addQuery(definedq, queries);
//            }
//
//        }
//
//    }
//
//    protected Query makeQuery(String q) {
//        rc.setVar("q", q);
//        Renderable r = this.config.getKeywordQuery();
//        if (r != null) {
//            q = r.getRenderValue(rc);
//        }
//        return new ExpressionQuery(q);
//    }
//
//    private FacetDefine copyFacetDefine(FacetDefine src) {
//        FacetDefine dest = new FacetDefine();
//        dest.fieldName = src.fieldName;
//        dest.limit = src.limit;
//        dest.method = src.method;
//        dest.minCount = src.minCount;
//        dest.order = src.order;
//        dest.ranges = src.ranges;
//        return dest;
//    }
//
//    @Override
//    protected Schema getSchema() {
//        return this.schema;
//    }
//
//    private void removeFacetedQuery(List<Query> qs, String name, String value) {
//        for (Query q : qs) {
//            if (q instanceof TermQuery) {
//                TermQuery tq = (TermQuery) q;
//                if (tq.field.equals(name)) {
//                    qs.remove(tq);
//                    return;
//                }
//            }
//        }
//    }
//
//    public void fill(ListQueryConfig config, RenderContext rc) {
//        QueryReq req = new QueryReq();
//        this.config = config;
//        this.rc = rc;
//        this.schema = config.getSchema(rc);
//        req.doExtendLimit = rc.paramInt("fk-doExtendLimit", -1);
//
//        req.fields = config.getFields();
//        int pageIndex = rc.paramInt(config.p, 0);
//
//        req.from = pageIndex * getPageSize();
//        req.size = getPageSize();
//        req.group = config.getGroup();
//        req.indexName = config.getSchema(rc).getTableName();
//
//
//        req.omitFilter = rc.paramBool("fk-omitFilter", false);
//
//        String fq = rc.param(config.fq, "");
//        Map<String, String> qnames = new HashMap<String, String>();
//        this.facetQuerys = qnames;
//        String[] fqs = fq.split(",");
//        List<Query> queries = new ArrayList<Query>(5);
//        int treeIndex = -1;
//        FacetDefine[] treefd = config.getTreeFacetDefines();
//        for (String ts : fqs) {
//            String s = ts.trim();
//            if (s.isEmpty()) {
//                continue;
//            }
//            String[] ss = s.split("\\:");
//            if (ss.length != 2 || ss[0].isEmpty() || ss[1].isEmpty()) {
//                continue;
//            }
//
//            String name = ss[0];
//            String value = ss[1];
//            qnames.put(name, value);
//            StringPair sp = new StringPair(name, value);
//            this.mapValue(sp);
//            // mapLable(sp.name)
//            sp.name = this.mapLable(name) + ":" + sp.value;
//            sp.value = this.changeUrl(config.fq, name, null);
//            querys.add(sp);
//            queries.add(this.buildQuery(name, value));
//
//            if (treefd != null) {
//                for (int i = 0; i < treefd.length; i++) {
//                    if (name.equals(treefd[i].fieldName)) {
//                        treeIndex = Math.max(treeIndex, i);
//                    }
//                }
//            }
//
//        }
//        this.treeQueryIndex = treeIndex;
//
//        List<FacetDefine> fds = new ArrayList(7);
//        if (treefd != null) {
//            if (treeIndex < 0) {
//
//                fds.add(treefd[0]);
//            } else if (treeIndex + 1 < treefd.length) {
//                fds.add(treefd[treeIndex + 1]);
//            } else if (treefd[treefd.length - 1].selectedValue != null) {
//                FacetDefine fd = treefd[treefd.length - 1];
//                FacetDefine nfd = this.copyFacetDefine(fd);
//                String fdv = qnames.getTarget(fd.fieldName);
//                if (fdv != null && fdv.trim().equals("")) {
//                    fdv = null;
//                }
//                nfd.selectedValue = fdv;
//                fds.add(nfd);
//                removeFacetedQuery(queries, nfd.fieldName, fdv);
//            }
//        }
//        FacetDefine[] propertyfd = config.getPropertyFacetDefines(this.rc);
//        if (propertyfd != null) {
//            for (FacetDefine fd : propertyfd) {
//                if (fd.selectedValue != null) {
//                    FacetDefine nfd = this.copyFacetDefine(fd);
//                    String fdv = qnames.getTarget(fd.fieldName);
//                    if (fdv != null && fdv.trim().equals("")) {
//                        fdv = null;
//                    }
//                    nfd.selectedValue = fdv;
//                    fds.add(nfd);
//
//                    if (!schema.getField(fd.fieldName).tokenized) {
//                        removeFacetedQuery(queries, nfd.fieldName, fdv);
//                    }
//
//                } else if (!qnames.containsKey(fd.fieldName)) {
//                    fds.add(fd);
//                }
//            }
//        }
//        req.facets = fds.toArray(new FacetDefine[fds.size()]);
//        this.processDefinedQuerys(queries);
//        addQuery(config.getFixedQuery(rc), queries);
//
//        String q = rc.param(config.q);
//
//        if (q == null || q.equals("")) {
//            if (queries.size() == 0)
//                queries.add(new MatchAll());
//        } else {
//            StringPair sp = new StringPair("查询词:" + q, this.changeUrl(config.q,
//                    null));
//            // mapLable(sp);
//            req.highlights = config.getHighlights();// 有搜索词才加highlight
//            querys.add(sp);
//            queries.add(this.makeQuery(q));// new ExpressionQuery(q));
//        }
//
//        req.query = this.normalQuery(queries);
//
//
//        // this.config.makeQuery();
//
//        req.sorts = this.config.fixedSorts;
//        Sort[] ss = this.config.getSorts();
//
//        if (ss != null) {
//            int si = rc.paramInt(config.sortName, -1);
//            if (si >= 0 && si < ss.length) {
//                req.sorts = new Sort[]{ss[si]};
//            }
//        }
//        if (req.sorts == null) {
//            req.sorts = ListQueryConfig.parseSorts(rc.p(this.config.sortName + "a"));
//        }
//
//
//        Resp resp = schema.getDataSource().sendSearchReq(req, 60000);
//        log.debug("req:{}", req.toString());
//        if (resp instanceof QueryResp) {
//
//            this.resp = (QueryResp) resp;
//            if (this.resp.warnings != null) {
//                for (String s : this.resp.warnings) {
//                    log.warn(s);
//                }
//            }
//        } else {
//            log.error("SE Error: req:{} resp:{}", req.toString(),
//                    resp.toString());
//            return;
//        }
//
//        // this.calPageMinMax(this.resp.total, config);
//        this.rowset = new SEResultRowSet(this.resp.docs, this.resp.docIds);
//        this.autoWrap();
//        this.it = this.rowset.getRows().iterator();
//
//        // this.resp.
//
//    }
//
//    private boolean isTopQuery(Query q) {
//        if (q instanceof TermsFilter || q instanceof MultiGroupQuery) {
//            return true;
//        }
//        return false;
//    }
//
//    private Query mergeQuery(Query q1, Query q2) {
//        if (q1 == null) {
//            return q2;
//        }
//        if (q2 == null) {
//            return q1;
//        }
//        if (q1 instanceof MatchAll) {
//            return q2;
//        }
//        if (q2 instanceof MatchAll) {
//            return q1;
//        }
//
//        return new BoolQuery(new Query[]{q1, q2});
//    }
//
//    private void setInner(Query top, Query inner) {
//        if (top instanceof TermsFilter) {
//            TermsFilter tf = ((TermsFilter) top);
//            tf.query = this.mergeQuery(inner, tf.query);
//            return;
//        }
//        throw new java.lang.RuntimeException("Unsupported top query :" + top);
//    }
//
//    protected Query normalQuery(List<Query> qs) {
//        int cnt = qs.size();
//        if (cnt == 0) {
//            return new MatchAll();
//        }
//        if (cnt == 1) {
//            return qs.getTarget(0);
//        }
//        List<Query> nq = new ArrayList<Query>(cnt);
//        Query top = null;
//        for (Query q : qs) {
//            if (this.isTopQuery(q)) {
//                if (top != null) {
//                    throw new java.lang.RuntimeException(
//                            "Can only have one top query!");
//                }
//                top = q;
//                continue;
//            }
//            if (q instanceof MatchAll) {
//                continue;
//            }
//            nq.add(q);
//        }
//        Query inner;
//        if (nq.size() == 0) {
//            inner = new MatchAll();
//        } else if (nq.size() == 1) {
//            inner = nq.getTarget(0);
//        } else {
//            inner = new BoolQuery(nq.toArray(new Query[nq.size()]));
//        }
//        if (top != null) {
//            this.setInner(top, inner);
//            return top;
//
//        }
//        return inner;
//
//    }
//
//    /**
//     * 收集facet中出现的id，以便自动转化成文本的方式 <br>
//     * TODO :过滤掉range等类型的faced
//     */
//    @Override
//    protected void collectAditionalValues(Map<String, List<String>> v/*
//     * schema,ids
//	 */) {
//        Facet fs[] = this.resp.facets;
//        if (fs != null) {
//            for (Facet f : fs) {
//                if (f.items == null || f.items.length == 0) {
//                    continue;
//                }
//                SchemaField sf = this.schema.getField(f.fieldName);
//
//                if (sf != null && sf.hasJoinField()) {
//                    List<String> lv = new ArrayList<String>(f.items.length);
//                    for (StrIntBag sb : f.items) {
//                        lv.add(sb.name);
//                    }
//                    v.put(sf.joinSchema.getValue().getId(), lv);
//                }
//
//            }
//        }
//
//        // empty
//    }
//
//
//    final private void mapValue(StringPair sp) {
//        sp.value = mapValue(sp.name, sp.value);
//
//    }
//
//    protected Query buildQuery(String fieldname, String query) {
//        TermQuery tq = new com.mapbased.search.req.query.TermQuery();
//        tq.field = fieldname;
//        tq.term = query;
//        return tq;
//    }
//
//    public String changeUrl(String pname, String dname, String value) {
//        return changeUrl(pname, dname, value, false);
//    }
//
//    public String changeUrl(String pname, String dname, String value, boolean keepPage) {
//
//        StringBuilder ret = new StringBuilder(rc.getUri().length() + 10);
//        ret.append(rc.getPath()).append("?");
//
//        java.util.Iterator<String> e = this.getGETParams();// getParameterNames();
//
//        boolean found = false;
//        while (e.hasNext()) {
//            String p = e.next();
//            String v = rc.param(p);
//            if (!keepPage) {
//                if (p.equals(config.p)) {
//                    continue;
//                }
//            }
//
//            if (p.equals(pname)) {
//                found = true;
//                if (dname == null) {
//
//                    addUrlSafe(ret, pname, value);
//
//                } else {
//
//                    // ///////////////////////////
//
//                    StringBuilder sb = new StringBuilder();
//
//                    String[] vs = v.split(",");
//                    boolean fd = false;
//                    for (String s : vs) {
//                        String[] ss = s.split("\\:");
//                        String d = ss[0];
//                        String dv = ss[1];
//
//                        if (this.treeParentNodeFilter(d, dname)) {
//                            continue;
//                        }
//
//                        // sb.append(d).append(":");
//                        String tv = null;
//                        if (d.equals(dname)) {
//                            fd = true;
//                            // sb.append(value);
//                            tv = value;
//
//                        } else {
//                            // sb.append(dv);
//                            tv = dv;
//
//                        }
//                        this.addFQSafe(sb, d, tv);
//                        // sb.append(",");
//
//                    }
//                    if (!fd) {
//                        // addUrlSafe(sb, dname, value);
//                        // sb.append(dname).append(":").append(value);
//                        this.addFQSafe(sb, dname, value);
//                    }
//                    if (sb.length() > 0) {
//                        if (sb.charAt(sb.length() - 1) == ',') {
//                            sb.setLength(sb.length() - 1);
//                        }
//
//                        addUrlSafe(ret, p, sb.toString());
//                    }
//
//                    // ////////////////////////////
//
//                }
//            } else {
//                // ret.append(p).append("=");
//                addUrlSafe(ret, p, v);
//
//            }
//
//        }
//        if (!found && value != null) {
//            if (dname != null) {
//                value = dname + ":" + value;
//
//            }
//            addUrlSafe(ret, pname, value);
//
//        }
//
//        return ret.toString();
//
//    }
//
//    private boolean treeParentNodeFilter(String newFiledName,
//                                         String oldFiledName) {
//        if (!config.needTreeParentNodeFilter()) {
//            return false;
//        }
//        int n = this.indexOfTreeNodeFilter(newFiledName);
//        int o = this.indexOfTreeNodeFilter(oldFiledName);
//        return (n >= 0 && o >= 0);
//
//    }
//
//    private void addFQSafe(StringBuilder sb, String name, String value) {
//
//        if (value == null)
//            return;
//
//        sb.append(name).append(":").append(value).append(",");
//
//    }
//
//    private int indexOfTreeNodeFilter(String fname) {
//        FacetDefine[] treeFacetDefines = config.getTreeFacetDefines();
//        for (int i = 0; i < treeFacetDefines.length; i++) {
//            if (treeFacetDefines[i].fieldName.equals(fname)) {
//                return i;
//            }
//
//        }
//        return -1;
//    }
//
//    @Override
//    public int getTotal() {
//        if (this.resp == null)
//            return -1;
//        return this.resp.total;
//    }
//
//    @Override
//    public int getTime() {
//        if (this.resp == null)
//            return -1;
//        return this.resp.queryTime;
//    }
//
//    /**
//     * 重新计算类别层级统计关。搜索出来的统计是简单的按照个数统计，没考虑层级关系
//     */
//    public void calCategoryFacet(Facet f) {
//
//        Map<String, Integer>[] level = new Map[8];//最大8级
//        for (int i = 0; i < level.length; i++) {
//            level[i] = new TreeMap<>();
//        }
//        for (StrIntBag si : f.items) {
//            int idx = si.name.length() / 2;
//            level[idx].put(si.name, si.value);
//        }
//        for (int i = level.length - 1; i > 0; i--) {
//            Map<String, Integer> mdown = level[i];
//            Map<String, Integer> mup = level[i - 1];
//            for (Map.Entry<String, Integer> e : mdown.entrySet()) {
//                String k = e.getKey().substring(0, e.getKey().length() - 2);
//                Integer old = mup.getTarget(k);
//
//                mup.put(k, old == null ? e.getValue() : e.getValue() + old);
//
//            }
//
//
//        }
//        for (int i = 0; i < level.length; i++) {
//            Map<String, Integer> m = level[i];
//            if (m.size() > 1) {
//                StrIntBag[] sis = new StrIntBag[m.size()];
//                int j = 0;
//                for (Map.Entry<String, Integer> e : m.entrySet()) {
//                    sis[j++] = new StrIntBag(e.getKey(), e.getValue());
//                }
//                f.items = sis;
//                return;
//            }
//        }
//
//    }
//}
