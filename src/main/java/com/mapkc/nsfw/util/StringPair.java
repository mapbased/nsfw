package com.mapkc.nsfw.util;

import java.io.Serializable;

public class StringPair implements Serializable {
    private static final long serialVersionUID = 3454234325353654757L;
    public String name;
    public String value;

    public StringPair() {
    }

    public StringPair(String name, String value) {
        this.name = name;
        this.value = value;
    }
}

