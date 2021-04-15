package com.mapkc.nsfw.handler;

import com.mapkc.nsfw.input.FormField;
import com.mapkc.nsfw.input.FormHandler;
import com.mapkc.nsfw.model.FormModel;
import com.mapkc.nsfw.model.RenderContext;
import com.mapkc.nsfw.model.XEnum;
import com.mapkc.nsfw.util.VolatileBag;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class FormModelFromSchema implements FormHandler {

    @Override
    public Map<String, String> load(RenderContext rc, String id, FormModel model)
            throws IOException {

        String sn = rc.param("id");
        rc.setVar("k", rc.getSite().getXEnum(sn));
        return null;
    }

    @Override
    public boolean update(FormModel model, String id, RenderContext rc,
                          Map<String, String> values) throws IOException {

        String sn = rc.param("id");
        rc.setVar("k", rc.getSite().getXEnum(sn));

        List<String> flz = rc.params("fields");
        VolatileBag<XEnum> vfm = FormModel.fromSchema(rc.getSite(),
                rc.param("id"), flz,
                rc.param("modelName"));
        FormModel fm = (FormModel) vfm.getValue();
        XEnum x = rc.getSite().getXEnum(fm.getParentId());
        if (x == null) {
            rc.addError("modelName", "Cannot find parent");
            return false;
        }
        x.addSingleChild(fm.getName(), vfm);

        fm.store(rc.getSite());
        return false;

    }

    @FormField(caption = "输出路径", input = "typeahead<path=/handler/rpcmisc>")
    public String modelName;
    @FormField(caption = "选择字段", input = "checkboxgroup<vl=[screenName+'('+name+')':name]@{k.getChildren('SchemaField')}>")
    public String fields;

}
