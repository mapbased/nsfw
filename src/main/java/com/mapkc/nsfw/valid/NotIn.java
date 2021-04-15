package com.mapkc.nsfw.valid;

import com.mapkc.nsfw.input.FormField;
import com.mapkc.nsfw.model.RenderContext;
import com.mapkc.nsfw.model.Schema;
import com.mapkc.nsfw.model.XEnum;
import com.mapkc.nsfw.util.VolatileBag;

import java.util.Map;

/**
 * 目前仅适用于mysql
 *
 * @author chy
 */
public class NotIn extends BaseValidator {

    @FormField(caption = "schema", input = "typeahead<path=/handler/rpcmisc?xtype=schema>")
    VolatileBag<XEnum> schema;

    @FormField(caption = "field name")
    String fieldName;

    private Schema getSchema() {
        if (this.schema == null) {
            return null;
        }
        return (Schema) this.schema.getValue();
    }

    // @Override
    // public void validate(RenderContext rc, String value, FormFieldModel ffm,
    // FormModel formModel) {
    //
    // Schema c=this.getSchema();
    // if (c == null) {
    // c = formModel.getSchema();
    // }
    // String fn = this.fieldName;
    // if (fn == null || fn.equals("")) {
    // fn = ffm.getName();
    // }
    //
    // StringBuilder sb = new StringBuilder();
    // sb.append(fn).append("=? ");
    // String id = formModel.getId(rc);
    //
    // boolean selfschema = false;
    // if (formModel.getSchema() == c && id != null) {
    //
    // sb.append(" AND ").append(c.getKeyFieldName()).append("<>?");
    // selfschema = true;
    // }
    //
    // int cnt = c.countBySql(
    //
    // sb.toString(), (
    // selfschema ? new Object[] { value, id }
    // : new Object[] { value }));
    //
    // if (cnt > 0) {
    // this.reportError(rc, ffm);
    // }
    // }
    //
    @Override
    public void renderJS(RenderContext rc) {


    }

    @Override
    public void validate(RenderContext rc, Schema schema,
                         Map<String, ? extends Object> values, String name) {

        Schema c = this.getSchema();
        if (c == null) {
            c = schema;
        }
        String fn = this.fieldName;
        if (fn == null || fn.equals("")) {
            fn = name;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(fn).append("=? ");
        String id = schema == null ? null : schema.getIdValue(rc);

        boolean selfschema = false;
        if (schema == c && id != null) {

            sb.append(" AND ").append(c.getKeyFieldName()).append("<>?");
            selfschema = true;
        }
        Object value = values.get(name);

        int cnt = c.countBySql(

                sb.toString(), (selfschema ? new Object[]{value, id}
                        : new Object[]{value})
        );

        if (cnt > 0) {
            this.reportError(rc, name);
        }

    }

}
