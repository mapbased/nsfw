package com.mapkc.nsfw.model;

import com.mapkc.nsfw.util.logging.ESLogger;
import com.mapkc.nsfw.util.logging.Loggers;

import java.util.HashMap;
import java.util.Map;

public enum SchemaFieldType {

    TinyInt {
        public String javaStr() {
            return "int";
        }
    },
    SmallInt {
        public String javaStr() {
            return "int";
        }
    },
    Int() {
        public String javaStr() {
            return "int";
        }

    },
    BigInt() {
        public String javaStr() {
            return "long";
        }
    },
    Float() {
        public String javaStr() {
            return "float";
        }
    },
    Real() {
        public String javaStr() {

            return "double";
        }
    },
    Double() {
        public String javaStr() {
            return "double";
        }

    },
    BigDecimal() {
        public String javaStr() {
            return "java.math.BigDecimal";
        }

    },


    // Img() {
    //
    // },

    Tag() {
        public String javaStr() {
            return "String";
        }
    },
    Text() {
        @Override
        public String filter(String s) {
            return s;
        }

        public String javaStr() {
            return "String";
        }
    },

    Varchar() {
        public String javaStr() {
            return "String";
        }

        @Override
        public String filter(String s) {
            return s;
        }

    },
    Date() {
        public String javaStr() {
            return "java.sql.Date";
        }
    },
    Time() {
        public String javaStr() {
            return "java.sql.Time";
        }
    },
    Datetime() {
        public String javaStr() {
            return "java.sql.Timestamp";
        }
    },
    Timestamp() {
        public String javaStr() {
            return "java.sql.Timestamp";
        }
    },
    Boolean() {
        public String javaStr() {
            return "boolean";
        }
    },
    SEAttribute() {
        public String javaStr() {
            return "long";
        }
    },
    Json() {
        public String javaStr() {
            return "String";
        }

    },
    Point() {
        /**
         * s:输入格式形如：POINT(113.954748947394 22.5471485673931)
         */

        @Override
        public String updateFunction() {
            return "GeomFromText(?)";
        }

        @Override
        public String convertFieldName(String s) {

            StringBuilder sb = new StringBuilder(s.length() * 2 + 24);
            sb.append("cast(ASTEXT(").append(s).append(") as char) ").append(s);
            return sb.toString();

        }
    };

    final static ESLogger log = Loggers.getLogger(SchemaFieldType.class);
    // static private Map<Integer, SchemaFieldType> sqlTypesMap = new HashMap();
    static private final Map<String, SchemaFieldType> seTypesMap = new HashMap();
    static private final Map<String, SchemaFieldType> mysqlTypesMap = new HashMap();
    static private final Map<String, SchemaFieldType> pgCockTypesMap = new HashMap();

    static {

        seTypesMap.put("text", SchemaFieldType.Text);
        seTypesMap.put("all", SchemaFieldType.Text);
        seTypesMap.put("htmltext", SchemaFieldType.Text);
        seTypesMap.put("tag", SchemaFieldType.Tag);
        seTypesMap.put("id", SchemaFieldType.Text);
        seTypesMap.put("type", SchemaFieldType.Text);
        seTypesMap.put("byte", SchemaFieldType.TinyInt);
        seTypesMap.put("short", SchemaFieldType.SmallInt);
        seTypesMap.put("int", SchemaFieldType.Int);
        seTypesMap.put("float", SchemaFieldType.Float);
        seTypesMap.put("double", SchemaFieldType.Double);
        // /seTypesMap.put("numeric", SchemaFieldType.Int);
        seTypesMap.put("char", SchemaFieldType.Varchar);
        seTypesMap.put("datetime", SchemaFieldType.Datetime);
        seTypesMap.put("attribute", SchemaFieldType.SEAttribute);


        /*
         * sqlTypesMap.put(Types.BIT, SchemaFieldType.Boolean);
         * sqlTypesMap.put(Types.TINYINT, SchemaFieldType.TinyInt);
         * sqlTypesMap.put(Types.SMALLINT, SchemaFieldType.SmallInt);
         * sqlTypesMap.put(Types.INTEGER, SchemaFieldType.Int);
         * sqlTypesMap.put(Types.BIGINT, SchemaFieldType.BigInt);
         * sqlTypesMap.put(Types.FLOAT, SchemaFieldType.Float);
         * sqlTypesMap.put(Types.REAL, SchemaFieldType.Real);
         * sqlTypesMap.put(Types.DOUBLE, SchemaFieldType.Double);
         * sqlTypesMap.put(Types.NUMERIC, SchemaFieldType.Int);
         * sqlTypesMap.put(Types.DECIMAL, SchemaFieldType.Double);
         * sqlTypesMap.put(Types.CHAR, SchemaFieldType.Varchar);
         * sqlTypesMap.put(Types.VARCHAR, SchemaFieldType.Varchar);
         * sqlTypesMap.put(Types.LONGVARCHAR, SchemaFieldType.Text);
         * sqlTypesMap.put(Types.DATE, SchemaFieldType.Date);
         * sqlTypesMap.put(Types.TIME, SchemaFieldType.Time);
         * sqlTypesMap.put(Types.TIMESTAMP, SchemaFieldType.Timestamp);
         */
        // ///////////////////////////////////////////////////////////////////

        mysqlTypesMap.put("bool", SchemaFieldType.Boolean);
        mysqlTypesMap.put("boolean", SchemaFieldType.Boolean);

        mysqlTypesMap.put("tinyint", SchemaFieldType.TinyInt);
        mysqlTypesMap.put("smallint", SchemaFieldType.SmallInt);
        mysqlTypesMap.put("int", SchemaFieldType.Int);
        mysqlTypesMap.put("mediumint", SchemaFieldType.Int);

        mysqlTypesMap.put("bigint", SchemaFieldType.BigInt);
        mysqlTypesMap.put("float", SchemaFieldType.Float);
        mysqlTypesMap.put("real", SchemaFieldType.Real);
        mysqlTypesMap.put("double", SchemaFieldType.Double);
        // /mysqlTypesMap.put("numeric", SchemaFieldType.Int);
        mysqlTypesMap.put("decimal", SchemaFieldType.BigDecimal);
        mysqlTypesMap.put("char", SchemaFieldType.Varchar);
        mysqlTypesMap.put("varchar", SchemaFieldType.Varchar);
        mysqlTypesMap.put("text", SchemaFieldType.Text);
        mysqlTypesMap.put("date", SchemaFieldType.Date);
        mysqlTypesMap.put("datetime", SchemaFieldType.Datetime);

        mysqlTypesMap.put("time", SchemaFieldType.Time);
        mysqlTypesMap.put("timestamp", SchemaFieldType.Timestamp);
        mysqlTypesMap.put("point", SchemaFieldType.Point);
        mysqlTypesMap.put("json", SchemaFieldType.Json);


        ////////////////////////PG COCK////////////////////////////////////////////////

        pgCockTypesMap.put("INT", SchemaFieldType.Int);
        pgCockTypesMap.put("SERIAL", SchemaFieldType.BigInt);
        pgCockTypesMap.put("SMALLSERIAL", SchemaFieldType.BigInt);
        pgCockTypesMap.put("BIGSERIAL", SchemaFieldType.BigInt);


        pgCockTypesMap.put("DECIMAL", SchemaFieldType.BigDecimal);
        pgCockTypesMap.put("FLOAT", SchemaFieldType.Float);
        pgCockTypesMap.put("BOOL", SchemaFieldType.Boolean);
        pgCockTypesMap.put("DATE", SchemaFieldType.Date);
        pgCockTypesMap.put("TIMESTAMP", SchemaFieldType.Timestamp);
        pgCockTypesMap.put("STRING", SchemaFieldType.Text);
        pgCockTypesMap.put("CHARACTER", SchemaFieldType.Text);
        pgCockTypesMap.put("CHAR", SchemaFieldType.Text);
        pgCockTypesMap.put("VARCHAR", SchemaFieldType.Text);
        pgCockTypesMap.put("TEXT", SchemaFieldType.Text);
        pgCockTypesMap.put("STRING", SchemaFieldType.Text);

        pgCockTypesMap.put("TIMESTAMP", SchemaFieldType.Timestamp);


    }

    static public SchemaFieldType fromSEType(String seType) {

        SchemaFieldType sft = seTypesMap.get(seType);
        return sft;
    }

    static public SchemaFieldType fromSqlType(String mysqltype) {
        SchemaFieldType sft = mysqlTypesMap.get(mysqltype);
        return sft;
        // /log.warn("Cannot find SchemaFieldType for:{}", mysqltype);
    }

    static public SchemaFieldType fromPgCockType(String mysqltype) {
        SchemaFieldType sft = pgCockTypesMap.get(mysqltype);
        return sft;
        // /log.warn("Cannot find SchemaFieldType for:{}", mysqltype);
    }

    /**
     * 用户在表单输入的数据，在转换到db时，做一些过滤。<br/>
     * 比如在一个int类型的字段，用户什么都没输，应该转换为null，而不是空字符
     *
     * @param s
     * @return
     */

    public String filter(String s) {

        if (s == null || s.trim().equals("")) {
            return null;
        }
        return s.trim();
    }

    // static public SchemaFieldType fromSqlType(int sqltype) {
    // SchemaFieldType sft=sqlTypesMap.getTarget(sqltype);
    // if(sft!=null){
    // return sft;
    // }
    // return null;
    // }

    /**
     * 到数据库查询时，转换字段的形式<br/>
     * 比如 coord Point，字段的名字为coord,类型为point,到数据库查询时重写为：<br />
     * ASWKT(coord) coord
     *
     * @param s
     * @return
     */
    public String convertFieldName(String s) {
        return s;

    }

    public String javaStr() {
        return "String";
    }

    public String updateFunction() {
        return "?";
    }
}
