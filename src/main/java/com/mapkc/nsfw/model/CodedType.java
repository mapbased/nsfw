package com.mapkc.nsfw.model;

import com.mapkc.nsfw.input.FormField;

/**
 * Created by chy on 14-7-28.
 */
public class CodedType extends XEnum {
    @FormField(caption = "Code")
    private int code;
    @FormField(caption = "排序")
    private float sort;
    @FormField(caption = "备注", input = "textarea")
    private String memo;

    public int getCode() {
        return code;
    }

    public float getSort() {
        return sort;
    }

    @Override
    protected String defaultIcon() {
        return "fa   fa-random";
    }

    public String getMemo() {
        return memo;
    }
}
