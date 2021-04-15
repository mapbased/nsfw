package com.mapkc.nsfw.input;

import com.mapkc.nsfw.model.FormModel;
import com.mapkc.nsfw.model.RenderContext;

import java.io.IOException;
import java.util.Map;

/**
 * @author chy
 */
public interface FormHandler {
    /**
     * 从底层存储加载数据，用于表单显示
     *
     * @param rc
     * @param id
     * @param model
     * @return
     * @throws IOException
     */
    <T> Map<String, T> load(RenderContext rc, String id, FormModel model)
            throws IOException;

    /**
     * 将用户提交的内容，存储到底层存储
     *
     * @param model
     * @param id
     * @param rc
     * @param values
     * @return :true 改handler做了跳转，不需要额外处理
     * @throws IOException
     */
    boolean update(FormModel model, String id, RenderContext rc,
                   Map<String, String> values) throws IOException;
}
