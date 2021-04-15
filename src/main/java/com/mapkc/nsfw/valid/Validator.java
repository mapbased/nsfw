package com.mapkc.nsfw.valid;

import com.mapkc.nsfw.model.RenderContext;
import com.mapkc.nsfw.model.Schema;

import java.util.Map;

/**
 * 可能存在不同类型的Validator
 * <p>
 * 通过一个表单创建，不同类型的使用不同的输入条件
 *
 * @author chy
 */
public interface Validator {

    // public void validate(RenderContext rc, String value, FormFieldModel ffm,
    // FormModel formModel);

    /**
     * @param rc
     * @param schema    :may be null
     * @param values
     * @param fieldname
     */
    public void validate(RenderContext rc, Schema schema,
                         Map<String, ? extends Object> values, String fieldname);

    public void renderJS(RenderContext rc);


}
