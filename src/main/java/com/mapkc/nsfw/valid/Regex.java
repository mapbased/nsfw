package com.mapkc.nsfw.valid;

import com.mapkc.nsfw.input.FormField;
import com.mapkc.nsfw.input.FormModelInfo;
import com.mapkc.nsfw.model.RenderContext;
import com.mapkc.nsfw.model.Schema;

import java.util.Map;
import java.util.regex.Pattern;

@FormModelInfo(caption = "")
public class Regex extends BaseValidator {

    @FormField(caption = "表达式", required = true)
    Pattern regex;


    @Override
    public void renderJS(RenderContext rc) {


    }

    @Override
    public void validate(RenderContext rc, Schema schema,
                         Map<String, ? extends Object> values, String name) {
        Object value = values.get(name);
        if (value == null || value.equals("")) {
            return;
        }
        if (!regex.matcher(value.toString()).matches()) {
            rc.addError(name, this.getErrorMsg(rc));
        }

    }

}
