package com.mapkc.nsfw.util;

import com.mapkc.nsfw.model.XEnum;

public class VolatileBag<T extends XEnum> {


    public  /*volatile*/ T value;
    private long lastAccessTime = System.currentTimeMillis();

    public VolatileBag() {

    }

    public VolatileBag(T v) {
        this.value = v;
    }

    public T getValue() {
        this.lastAccessTime = System.currentTimeMillis();
        return value;
    }

    public void setValue(T value) {
        this.lastAccessTime = System.currentTimeMillis();
        this.value = value;
    }

    public String getId() {
        if (this.value == null) {
            return null;
        }
        return value.getId();
    }

    public VolatileBag<T> createNew() {
        return new VolatileBag<T>(this.value);
    }
}
