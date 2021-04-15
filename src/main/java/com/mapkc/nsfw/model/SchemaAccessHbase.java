//package com.mapkc.nsfw.model;
//
//import com.mapbased.search.req.Query;
//import com.mapbased.search.req.Req;
//import com.mapbased.search.req.Resp;
//import com.mapbased.search.req.misc.Doc;
//import com.mapbased.search.req.misc.DocResult;
//import com.mapbased.search.req.misc.StringPair;
//import com.mapbased.search.req.query.BoolQuery;
//import com.mapbased.search.req.query.ExpressionQuery;
//import com.mapbased.search.req.routing.*;
//import com.mapkc.nsfw.query.ResultMapSet;
//import com.mapkc.nsfw.query.ResultRow;
//import com.mapkc.nsfw.query.SEResultMapSet;
//import com.mapkc.nsfw.util.EasyMap;
//import com.mapkc.nsfw.util.SchemaAccessHelper;
//
//import java.io.IOException;
//import java.util.*;
//
///**
// * Created by chy on 16/12/17.
// */
//public class SchemaAccessHbase extends SchemaAccess {
//
//
//    @Override
//    public Map<String, Object> getFields(Schema schema,
//                                         final Map<String, Object> fields, String idValue) {
//
//        String[] flz = fields.keySet().toArray(new String[fields.size()]);
//        GetDocIfModified req = new GetDocIfModified(schema.getTableName(),
//                idValue, flz, null, -1);
//        Resp resp = schema.getDataSource().sendSearchReq(req);
//        if (resp instanceof ErrorResp) {
//            ErrorResp er = (ErrorResp) resp;
//            throw new java.lang.RuntimeException("SE Error:" + er.errorMsg);
//        }
//        QueryResp qr = (QueryResp) resp;
//        if (qr.docs == null || qr.docs.length < 1) {
//            return null;
//        }
//        for (String s : fields.keySet()) {
//            fields.put(s, qr.docs[0].getValue(s));
//        }
//        return fields;
//
//        // qr.docs[0].g
//
//    }
//
//    @Override
//    public Map<String, String> load(FormModel model, String id,
//                                    RenderContext rc) {
//        throw new java.lang.RuntimeException("implement this");
//    }
//
//    @Override
//    public Map<String, Object> getFieldsByQuery(Schema schema,
//                                                final Map<String, Object> fields,
//                                                final Map<String, ? extends Object> conditions) {
//        throw new java.lang.RuntimeException("implement this");
//    }
//
//    @Override
//    public Map<String, Object> getFieldsBySql(Schema schema,
//                                              final Map<String, Object> fields, String wheresql,
//                                              Object[] conditions) {
//
//        throw new java.lang.RuntimeException("implement this");
//        // return this.deleteByQuery(schema,fields, conditions);
//    }
//
//    @Override
//    public int deleteByQuery(Schema schema, Map<String, Object> conditions) {
//        throw new java.lang.RuntimeException("implement this");
//
//    }
//
//    public void delete(String id, Schema schema) {
//        DeleteByIdReq req = new DeleteByIdReq(schema.getTableName(), id);
//        Resp resp = schema.getDataSource().sendSearchReq(req);
//        if (resp instanceof ErrorResp) {
//            ErrorResp er = (ErrorResp) resp;
//            throw new java.lang.RuntimeException("Error while delete from SE:" + schema.getTableName() + " " + er.errorMsg);
//        }
//        log.debug("Delete {} from SE:{} affectedCount:{}", id, schema.getId(), ((AcceptedResp) resp).affectedCount);
//
//
//    }
//
//    @Override
//    public int deleteBySQL(Schema schema, String wheresql, Object[] params) {
//        ExpressionQuery eq = new ExpressionQuery(wheresql);
//        Resp resp = schema.getDataSource().sendSearchReq(
//                new DeleteByQueryReq(schema.getTableName(), eq));
//        if (resp instanceof ErrorResp) {
//            ErrorResp er = (ErrorResp) resp;
//            throw new java.lang.RuntimeException("SE Error:" + er.errorMsg);
//        }
//        AcceptedResp ar = (AcceptedResp) resp;
//        return ar.affectedCount;
//
//    }
//
//    @Override
//    public void addUsingGivenId(String id, Schema schema,
//                                Map<String, String> values)
//            throws IOException {
//        this.addOrUpdateUsingGivenId(id, schema, values,
//                AddDocReq.IMPORT_METHOD_DEFAULT);
//    }
//
//    @Override
//    public void updateUsingGivenId(String id, Schema schema,
//                                   Map<String, String> values)
//            throws IOException {
//        this.addOrUpdateUsingGivenId(id, schema, values,
//                AddDocReq.IMPORT_METHOD_PARTIAL);
//    }
//
//    @Override
//    public void addOrUpdateUsingGivenId(String id, Schema schema,
//                                        Map<String, String> values)
//            throws IOException {
//        this.addOrUpdateUsingGivenId(id, schema, values,
//                AddDocReq.IMPORT_METHOD_PARTIAL);
//
//    }
//
//    @Override
//    public int changeFieldValue(Schema schema,
//                                final Map<String, ? extends Object> values,
//                                final Map<String, ? extends Object> conditions) {
//        throw new java.lang.RuntimeException("implement this!");
//
//    }
//
//    @Override
//    public int changeFieldValue(Schema schema,
//                                final Map<String, ? extends Object> values, String wheresql,
//                                final Object[] conditions) {
//        throw new java.lang.RuntimeException("implement this!");
//    }
//
//    @Override
//    public int changeFieldValue(String id, Schema schema,
//                                final Map<String, ? extends Object> values) {
//
//        String indexName = schema.getTableName();
//
//        StringPair[] sps = new StringPair[values.size()];
//        boolean allAttribute = true;
//        int i = 0;
//        for (Map.Entry<String, ? extends Object> e : values.entrySet()) {
//            String fn = e.getKey();
//            SchemaField sf = schema.getField(fn);
//            if (sf == null || sf.getType() != SchemaFieldType.SEAttribute) {
//                allAttribute = false;
//            }
//            sps[i++] = new StringPair(fn, String.valueOf(e.getValue()));
//
//        }
//        Req req;
//        if (allAttribute) {
//            req = new EditAttributeReq(indexName, id, sps,
//                    EditAttributeReq.AOP_UPDATE);
//
//        } else {
//            req = new AddDocReq(indexName, new Doc[]{new Doc(id, sps)},
//                    AddDocReq.IMPORT_METHOD_PARTIAL);
//        }
//        Resp resp = schema.getDataSource().sendSearchReq(req);
//        if (resp instanceof ErrorResp) {
//            ErrorResp er = (ErrorResp) resp;
//            throw new java.lang.RuntimeException("SE Error:" + er.errorMsg);
//        }
//        AcceptedResp ar = (AcceptedResp) resp;
//        return ar.affectedCount;
//
//    }
//
//    public <T> T getObjectById(Schema schema, String[] fields, Class<T> t, Object id) {
//        Map<String, Object> map = this.getFields(schema, calFields(schema, fields, t), String.valueOf(id));
//        if (map == null) {
//            return null;
//        }
//        return SchemaAccessHelper.toObj(t, EasyMap.toStrMap(map), schema.site);
//
//    }
//
//    @Override
//    public <T> List<T> listObjectBySql(Schema schema, String[] fields,
//                                       Class<T> t, String wheresql, Object[] conditions) {
//
//        Query query;
//        List<Query> a = new java.util.ArrayList<Query>(2);
//        if (wheresql != null) {
//            a.add(new ExpressionQuery(wheresql));
//        }
//        if (conditions != null)
//            for (Object o : conditions) {
//                if (o instanceof Query) {
//                    a.add((Query) o);
//                } else {
//                    throw new java.lang.RuntimeException(
//                            "unknown condition:" + o);
//                }
//            }
//
//        if (a.size() == 1) {
//            query = a.getTarget(0);
//        } else {
//            query = new BoolQuery(a.toArray(new Query[a.size()]));
//        }
//        if (fields == null) {
//            fields = new String[]{" "};
//        }
//
//        QueryReq req = new QueryReq(schema.getTableName(), query);
//        req.fields = fields;
//
//        Resp resp = schema.getDataSource().sendSearchReq(req);
//        if (!(resp instanceof QueryResp)) {
//            ErrorResp er = (ErrorResp) resp;
//            throw new java.lang.RuntimeException("SE Error:" + er.errorMsg);
//        }
//
//
//        QueryResp queryResp = (QueryResp) resp;
//        if (queryResp.docIds == null) {
//            return Collections.EMPTY_LIST;
//        }
//        List<T> result = new ArrayList<>(queryResp.docIds.length);
//        for (int i = 0; i < queryResp.docIds.length; i++) {
//            Map<String, String> values = new HashMap<>();
//            DocResult docResult = queryResp.docs[i];
//            for (StringPair stringPair : docResult.values) {
//                values.put(stringPair.name, stringPair.value);
//
//            }
//            values.put("_id", queryResp.docIds[i]);
//
//            result.add(SchemaAccessHelper.toObj(t, values, schema.site));
//        }
//
//        // queryResp.docs[0].getValue()
//
//        return result;
//    }
//
//    @Override
//    public int incFieldValue(Schema schema, String idValue,
//                             Map<String, ? extends Object> values) {
//
//        String indexName = schema.getTableName();
//
//        StringPair[] sps = new StringPair[values.size()];
//
//        int i = 0;
//        for (Map.Entry<String, ? extends Object> e : values.entrySet()) {
//            String fn = e.getKey();
//            SchemaField sf = schema.getField(fn);
//            if (sf == null || sf.getType() != SchemaFieldType.SEAttribute) {
//                throw new java.lang.RuntimeException(
//                        "Only inc on attriute field");
//            }
//            sps[i++] = new StringPair(fn, String.valueOf(e.getValue()));
//
//        }
//        Req req = new EditAttributeReq(indexName, idValue, sps,
//                EditAttributeReq.AOP_INC);
//        Resp resp = schema.getDataSource().sendSearchReq(req);
//        if (resp instanceof ErrorResp) {
//            ErrorResp er = (ErrorResp) resp;
//            throw new java.lang.RuntimeException("SE Error:" + er.errorMsg);
//        }
//        AcceptedResp ar = (AcceptedResp) resp;
//        return ar.affectedCount;
//
//    }
//
//    @Override
//    public boolean exist(Schema schema, String id) {
//
//        GetDocIfModified req = new GetDocIfModified(schema.getTableName(),
//                id, null, null, -1);
//        Resp resp = schema.getDataSource().sendSearchReq(req);
//        if (resp instanceof ErrorResp) {
//            ErrorResp er = (ErrorResp) resp;
//            throw new java.lang.RuntimeException("SE Error:" + er.errorMsg);
//        }
//        QueryResp qr = (QueryResp) resp;
//        return qr.total > 0;
//
//    }
//
//    private void addOrUpdateUsingGivenId(String id, Schema schema,
//                                         Map<String, String> values, int method)
//            throws IOException {
//
//        List<StringPair> ls = new ArrayList<StringPair>(values.size());
//        for (Map.Entry<String, String> e : values.entrySet()) {
//            if (e.getValue() == null) {
//                continue;
//            }
//            if (!schema.hasField(e.getKey())
//                    && e.getKey().indexOf('_') <= 0) {
//                continue;
//            }
//            ls.add(new StringPair(e.getKey(), e.getValue()));
//        }
//
//        Doc d = new Doc(id, ls.toArray(new StringPair[ls.size()]));
//
//        AddDocReq req = new AddDocReq(schema.getTableName(),
//                new Doc[]{d}, method);
//
//        Resp resp = schema.getDataSource().sendSearchReq(req);
//        if (resp instanceof ErrorResp) {
//            log.error("Error while add to {},values:{},reason:{} , req:{}",
//                    schema.getId(), values, ((ErrorResp) resp).errorMsg,
//                    req);
//            throw new java.lang.RuntimeException(
//                    "Error while add to se schema:" + schema.getId());
//        }
//
//        log.debug("Add {} ok,values:{}}", schema.getId(), values);
//
//    }
//
//    @Override
//    public ResultMapSet load(String[] fields, Map<String, ResultRow> ids,
//                             Schema schema) {
//        if (ids.size() == 0) {
//            return new SEResultMapSet(null, null, ids);
//        }
//        QueryDocListReq gdlq = new QueryDocListReq();
//        gdlq.indexName = schema.getTableName();
//        gdlq.fields = fields;
//        gdlq.outIds = ids.keySet().toArray(new String[ids.size()]);
//        Resp resp = schema.getDataSource().sendSearchReq(gdlq);
//        if (resp instanceof QueryDocListResp) {
//            QueryDocListResp qd = (QueryDocListResp) resp;
//            SEResultMapSet ss = new SEResultMapSet(qd.docs, qd.outIds, ids);
//
//            return ss;
//        } else {
//            throw new java.lang.RuntimeException("Cannot load ids from:"
//                    + schema.getTableName() + resp.toString());
//        }
//
//    }
//}
