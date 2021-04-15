package com.mapkc.nsfw.util;

import java.io.Serializable;

public class Sort implements Serializable {
    private static final long serialVersionUID = 3453434343443436547L;
    public String name;
    public boolean desc;

    public Sort() {
    }

    public Sort(String name, boolean desc) {
        this.name = name;
        this.desc = desc;
    }
}
