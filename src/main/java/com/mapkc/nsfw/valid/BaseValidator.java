package com.mapkc.nsfw.valid;

import com.mapkc.nsfw.input.FormField;
import com.mapkc.nsfw.input.FormFieldModel;
import com.mapkc.nsfw.input.FormModelInfo;
import com.mapkc.nsfw.model.RenderContext;
import com.mapkc.nsfw.model.Renderable;

@FormModelInfo(caption = "")
public abstract class BaseValidator implements Validator {

    @FormField(caption = "Error Msg")
    protected Renderable errorMsg;

    public String getErrorMsg(RenderContext rc) {
        return this.errorMsg.getRenderValue(rc);
    }

    protected void reportError(RenderContext rc, FormFieldModel ffm) {
        rc.addError(ffm.getName(), this.getErrorMsg(rc));
    }

    protected void reportError(RenderContext rc, String fieldName ) {
        rc.addError(fieldName, this.getErrorMsg(rc));
    }

}
