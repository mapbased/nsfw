//package com.mapkc.nsfw.query;
//
//import com.mapbased.search.req.misc.DocResult;
//
//public class SEResultRowImpl implements ResultRow {
//
//    /**
//     *
//     */
//    private final DocResult doc;
//
//    private String id;
//
//    SEResultRowImpl(DocResult d, String id) {
//        this.doc = d;
//        this.id = id;
//    }
//
//    @Override
//    public Object getField(String fieldName) {
//        // TODO Auto-generated method stub
//        if (doc == null) {
//            return null;
//        }
//        return doc.getValue(fieldName);
//    }
//
//    @Override
//    public String getHighlight(String fieldname) {
//        if (doc == null) {
//            return null;
//        }
//        String s = doc.getHighlight(fieldname);
//        if (s == null || s.equals("")) {
//            s = doc.getValue(fieldname);
//        }
//        return s;
//    }
//
//    @Override
//    public String getId() {
//        return id;
//    }
//
//}