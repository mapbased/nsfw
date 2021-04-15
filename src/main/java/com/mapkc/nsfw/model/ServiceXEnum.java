package com.mapkc.nsfw.model;

import com.mapkc.nsfw.input.FormField;

/**
 * 创建跟路径完全一致的javaclass，方便代码重用.
 * TODO:目前遇到的问题，动态加载类之后，不能赋值给之前的同名类，虽然有迂回的办法，但比较啰嗦，暂时先放着
 * Created by chy on 14-9-11.
 */
public class ServiceXEnum extends XEnum {
    @FormField(caption = "注释")
    private String comment;

    protected String defaultIcon() {
        return "fa  fa-gear";
    }
}
