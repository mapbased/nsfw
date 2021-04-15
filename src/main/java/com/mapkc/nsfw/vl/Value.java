package com.mapkc.nsfw.vl;

public interface Value {

    String getScreenName();

    String getValue();

    /**
     * 原始对象
     *
     * @return
     */
    Object getSrc();
}
