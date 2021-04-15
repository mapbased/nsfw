package com.mapkc.nsfw.util;

import java.io.Serializable;

public class FacetDefine implements Serializable {
    public static final String METHOD_FC = "fc";
    public static final String METHOD_ENUM = "enum";
    public static final String METHOD_COUNT_TERM = "countTerm";
    private static final long serialVersionUID = 34543223654757L;
    public String fieldName;
    public int minCount = 1;
    public int limit = 20;
    public int order = 0;
    public Range[] ranges;
    public String method = "fc";
    public String selectedValue;

    public FacetDefine() {
    }

    public FacetDefine(String fieldname) {
        this.fieldName = fieldname;
    }

    public FacetDefine(String fieldname, int minCount, int limit) {
        this.fieldName = fieldname;
        this.minCount = minCount;
        this.limit = limit;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("FacetDefine:");
        sb.append("fieldName:").append(this.fieldName).append("\n");
        sb.append("minCount:").append(this.minCount).append("\n");
        sb.append("limit:").append(this.limit).append("\n");
        sb.append("order:").append(this.order).append("\n");
        sb.append("method:").append(this.method).append("\n");
        Strings.arrayToString(sb, "ranges", this.ranges);
        return sb.toString();
    }


    public static final class Range implements Serializable {
        private static final long serialVersionUID = 3454363354757L;
        public String low;
        public String up;
        public String showName;

        public Range() {
        }

        public Range(String showName, String low, String up) {
            this.showName = showName;
            this.low = low;
            this.up = up;
        }

        public String toZKNodeStr() {
            StringBuilder sb = new StringBuilder();
            sb.append("low=").append(this.low).append(";").append("up=").append(this.up).append(";").append("showName=").append(this.showName);
            return sb.toString();
        }

        public String toString() {
            StringBuilder sb = new StringBuilder("Range:");
            sb.append("low:").append(this.low).append(" ");
            sb.append("up:").append(this.up).append(" ");
            sb.append("showName:").append(this.showName).append(" ");
            return sb.toString();
        }
    }
}
