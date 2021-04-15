package com.mapkc.nsfw.model;

import com.mapkc.nsfw.input.FormField;

import java.util.Map;

/**
 * form表单保存数据时触发业务逻辑
 *
 * @author chy
 */
public class Trigger extends XEnum {


    @FormField(caption = "排序", msg = "按照从小到大的顺序执行。如果小于0，将在update之前执行")
    private float sort;

    public void doAction(RenderContext rc, Map<String, String> values, String id) {

    }

    protected String defaultIcon() {
        return "fa   fa-flash";
    }

    protected void init(Site site) {
        Object o = site.getPathObject(this.getId());


    }
}
