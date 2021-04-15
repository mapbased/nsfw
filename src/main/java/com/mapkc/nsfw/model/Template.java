package com.mapkc.nsfw.model;

import com.mapkc.nsfw.input.FormHandler;

import java.io.IOException;
import java.util.Map;

/**
 * Created by chy on 15/5/25.
 */
public class Template extends Fragment implements FormHandler {

    protected String defaultIcon() {
        return "fa  fa-truck";
    }

    public FormModel getFormModel() {
        FormModel formModel = new FormModel();
        formModel.setId(this.getId());
        formModel.items = this.items;
        formModel.handler = this;
        return formModel;
    }

    @Override
    public Map<String, String> load(RenderContext rc, String id, FormModel model) throws IOException {
        return null;
    }

    @Override
    public boolean update(FormModel model, String id, RenderContext rc, Map<String, String> values) throws IOException {
        System.out.println(values);
        return false;
    }
}
