//package com.mapkc.nsfw.query;
//
//import com.mapbased.search.req.misc.DocResult;
//
//import java.util.List;
//
//public class SEResultRowSet implements ResultRowSet {
//    final List<ResultRow> rows;
//
//    public SEResultRowSet(DocResult[] docs, String[] outIds) {
//        if (outIds == null) {
//            rows = java.util.Collections.EMPTY_LIST;
//        } else {
//
//            rows = new java.util.ArrayList<ResultRow>(outIds.length);
//            for (int i = 0; i < docs.length; i++) {
//                rows.add(new SEResultRowImpl(docs[i], outIds[i]));
//            }
//        }
//    }
//
//    @Override
//    public List<ResultRow> getRows() {
//
//        return rows;
//    }
//
//}