package com.mapkc.nsfw.valid;

import com.mapkc.nsfw.input.FormField;
import com.mapkc.nsfw.model.RenderContext;
import com.mapkc.nsfw.model.Schema;

import java.util.Map;

public class Length extends BaseValidator {


    @FormField(caption = "Max Length")
    int max = Integer.MAX_VALUE;
    @FormField(caption = "Min Length")
    int min = 0;
    @FormField(caption = "Method", input = "radio<vl={汉字长度算1:1,汉字长度算2:2,算3:3}>")
    int method = 1;

    // @Override
    // public void validate(RenderContext rc, String value, FormFieldModel ffm,
    // FormModel formModel) {
    //
    // int l = value.length();
    // if (method == 2) {
    // l = value.getBytes(RenderContext.GBK).length;
    // }
    // if (method == 3) {
    // l = value.getBytes(RenderContext.UTF8).length;
    // }
    //
    // if (l > max || l < min) {
    // rc.addError(ffm.getName(), this.getErrorMsg(rc));
    //
    // }
    //
    // }
    //
    @Override
    public void renderJS(RenderContext rc) {
        // TODO Auto-generated method stub

    }

    @Override
    public void validate(RenderContext rc, Schema schema,
                         Map<String, ? extends Object> values, String name) {
        Object o = values.get(name);
        if (o == null) {
            return;
        }
        String value = o.toString();
        int l = value.length();
        if (method == 2) {
            l = value.getBytes(RenderContext.GBK).length;
        }
        if (method == 3) {
            l = value.getBytes(RenderContext.UTF8).length;
        }

        if (l > max || l < min) {
            rc.addError(name, this.getErrorMsg(rc));

        }

    }

}
