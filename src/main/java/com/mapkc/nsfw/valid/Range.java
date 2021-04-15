package com.mapkc.nsfw.valid;

import com.mapkc.nsfw.input.FormField;
import com.mapkc.nsfw.model.RenderContext;
import com.mapkc.nsfw.model.Schema;

import java.util.Map;

public class Range extends BaseValidator {

    @FormField(caption = "Max Value", input = "text<type=number>")
    long max;
    @FormField(caption = "Min Value", input = "text<type=number>")
    long min;

    // @Override
    // public void validate(RenderContext rc, String value, FormFieldModel ffm,
    // FormModel formModel) {
    //
    // if (value == null) {
    // return;
    // }
    // int l = 0;
    // try {
    // l = Integer.parseInt(value);
    // } catch (java.lang.NumberFormatException E) {
    // rc.addError(ffm.getName(), "请输入整数");
    // return;
    //
    // }
    // if (l > max || l < min) {
    // this.reportError(rc, ffm);
    // // rc.addError(ffm.getName(), this.getErrorMsg(rc));
    //
    // }
    //
    // }
    //
    @Override
    public void renderJS(RenderContext rc) {
        // TODO Auto-generated method stub

    }

    public long getMax() {
        return max;
    }

    public void setMax(long max) {
        this.max = max;
    }

    public long getMin() {
        return min;
    }

    public void setMin(long min) {
        this.min = min;
    }

    @Override
    public void validate(RenderContext rc, Schema schema,
                         Map<String, ? extends Object> values, String name) {
        Object value = values.get(name);
        if (value == null) {
            return;
        }
        long l = 0;
        try {
            l = Long.parseLong(value.toString());
        } catch (java.lang.NumberFormatException E) {
            rc.addError(name, "请输入数字");
            return;

        }
        if (l > max || l < min) {
            this.reportError(rc, name);
            // rc.addError(ffm.getName(), this.getErrorMsg(rc));

        }

    }

}
