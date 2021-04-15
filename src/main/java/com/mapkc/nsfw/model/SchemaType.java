package com.mapkc.nsfw.model;

// @edu.umd.cs.findbugs.annotations.SuppressFBWarnings({"SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING"})
public enum SchemaType {


    PgCock {
        final SchemaAccess access = new SchemaAccessPgCock();

        @Override
        public SchemaAccess getAccess() {
            return access;
        }
    },
    XEnum {
        final SchemaAccess access = new SchemaAccessXEnum();

        @Override
        public SchemaAccess getAccess() {
            return access;
        }
    },

    Hbase {
        final SchemaAccess access = null;

        @Override
        public SchemaAccess getAccess() {
            return access;
        }
    },
    Search {
        final SchemaAccess access = null;

        @Override
        public SchemaAccess getAccess() {
            return access;
        }
    },
    FLink {
        final SchemaAccess access = null;

        @Override
        public SchemaAccess getAccess() {
            return access;
        }
    },
    MySQL {
        final SchemaAccess access = new SchemaAccessMysql();

        @Override
        public SchemaAccess getAccess() {
            return access;
        }
    }

    // ,
    // HTML() {
    //
    // @Override
    // public ResultMapSet load(String[] fields, Map<String, ResultRow> ids,
    // Schema schema, RenderContext rc) {
    // // TODO Auto-generated method stub
    // return null;
    // }
    //
    // }
    //
    ;

    public abstract SchemaAccess getAccess();

}
