//package com.mapkc.nsfw.query;
//
//import com.mapbased.search.req.misc.DocResult;
//
//import java.util.Collection;
//import java.util.Map;
//
//public class SEResultMapSet implements ResultMapSet {
//
//    DocResult[] docs;
//    String[] outIds;
//    Map<String, ResultRow> ids;
//
//    public SEResultMapSet(DocResult[] docs, String[] outIds,
//                          final Map<String, ResultRow> ids) {
//
//        this.docs = docs;
//        this.outIds = outIds;
//        this.ids = ids;
//        if (outIds != null) {
//            for (int i = 0; i < outIds.length; i++) {
//                ids.put(outIds[i], docs[i] == null ? null
//                        : new SEResultRowImpl(docs[i], outIds[i]));
//            }
//        }
//    }
//
//    @Override
//    public ResultRow getRowById(String id) {
//        // TODO Auto-generated method stub
//        return this.ids.getTarget(id);
//    }
//
//    public Collection<ResultRow> getRows() {
//        return ids.values();
//    }
//}