package com.mapkc.nsfw.query;

import com.mapkc.nsfw.util.Strings;

/**
 * Created by chy on 15/1/30.
 */
public class SqlJoiner {

    private final StringBuilder builder = new StringBuilder();

    public SqlJoiner add(String fieldName, Object fieldValue, boolean doadd) {
        if (doadd) {
            if (builder.length() > 0)
                builder.append(" and ");
            boolean e = this.needEscape(fieldValue);
            if (!e)
                builder.append(fieldName).append("=").append(fieldValue);
            else {
                builder.append(fieldName).append("='")
                        .append(Strings.escapeSQL(fieldValue.toString())).append("'");
            }
        }
        return this;
    }

    public SqlJoiner add(String fieldName, Object value) {
        return this.add(fieldName, value, true);
    }

    public SqlJoiner addCheckAnd(String sql) {
        if (this.builder.length() > 0) {
            builder.append(" and ");
        }
        this.builder.append(sql);
        return this;
    }

    public String toString() {
        return this.builder.toString();
    }

    private boolean needEscape(Object o) {
        return !(o instanceof Number);
    }
}
