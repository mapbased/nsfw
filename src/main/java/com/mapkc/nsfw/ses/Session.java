package com.mapkc.nsfw.ses;

import java.util.Map;

public class Session {

    public final String sessionId;

    final long createTime = System.currentTimeMillis();
    long lastAccessTime = this.createTime;

    private Map<String, Object> data;

    public Session(String id) {
        this.sessionId = id;
    }

    public Object remove(String id) {
        if (this.data == null) {
            return null;
        }
        return data.remove(id);
    }

    public Object getValue(String key) {
        if (data == null) {
            return null;
        }
        return data.get(key);
    }

    public void setValue(String key, Object value) {
        if (data == null) {
            data = new java.util.TreeMap<String, Object>();
        }
        data.put(key, value);
    }

    public void invalidate() {
        if (this.data != null)
            this.data.clear();

    }

}
