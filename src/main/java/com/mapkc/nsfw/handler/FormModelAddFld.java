package com.mapkc.nsfw.handler;

import com.mapkc.nsfw.input.FormField;
import com.mapkc.nsfw.input.FormFieldModel;
import com.mapkc.nsfw.input.FormHandler;
import com.mapkc.nsfw.model.FormModel;
import com.mapkc.nsfw.model.RenderContext;
import com.mapkc.nsfw.model.SchemaField;
import com.mapkc.nsfw.model.XEnum;
import com.mapkc.nsfw.util.VolatileBag;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FormModelAddFld implements FormHandler {


    @Override
    public Map<String, String> load(RenderContext rc, String id, FormModel model)
            throws IOException {

        Map<String, SchemaField> flz = new HashMap<>(2);
        String sn = rc.param("id");
        FormModel fm = (FormModel) rc.getSite().getXEnum(sn);
        for (String s : fm.getSchema().getItems().keySet()) {
            if (fm.getFieldModel(s) == null) {
                flz.put(s, fm.getSchema().getField(s));
            }
        }
        rc.setVar("flz", flz);

        return null;
    }

    @Override
    public boolean update(FormModel model, String id, RenderContext rc,
                          Map<String, String> values) throws IOException {

        String sn = rc.param("id");
        FormModel fm = (FormModel) rc.getSite().getXEnum(sn);

        List<String> flz = rc.params("fields");

        for (String s : flz) {

            VolatileBag<XEnum> ve = fm.getSchema().getItems().get(s);
            FormFieldModel ffm = new FormFieldModel();
            ffm.from(ve);
            ffm.setParentIdRecursive(fm.getId());
            fm.addSingleChild(s, new VolatileBag<XEnum>(ffm));
            ffm.store(rc.getSite());
        }


        return false;

    }

    @FormField(caption = "选择字段", input = "checkboxgroup<vl=[value.screenName+' '+key:key]@{flz}>")
    public String fields;

}
