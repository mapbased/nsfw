package com.mapkc.nsfw.model;

import com.mapkc.nsfw.input.FormFieldModel;
import com.mapkc.nsfw.input.FormHandler;
import com.mapkc.nsfw.util.VolatileBag;

import java.io.IOException;
import java.util.Map;

/**
 * Created by chy on 14-10-12.
 */
public class ConfigGroup extends XEnum {

    public FormModel getFormModel(Site site) {

        FormModel fm = FormModel.fromClass(this.getClass(), site).clone();
        fm.handler = new FormHandler() {
            @Override
            public Map<String, String> load(RenderContext rc, String id, FormModel model) throws IOException {
                return model.load(rc, id, model);
            }

            @Override
            public boolean update(FormModel model, String id, RenderContext rc, Map<String, String> values) throws IOException {
                values.put(XEnum.KnownAttributes.Type
                        .name(), getXTypeName());
                return model.update(model, id, rc, values);

            }
        };

        XEnum p = site.getXEnum(this.getParentId());
        if (p == null) {
            return fm;
        }
        XEnum x = p.getChild("configfields");
        if (x == null) {
            return fm;
        }
        for (Map.Entry<String, VolatileBag<XEnum>> e : x.items.entrySet()) {
            if (e.getValue().getValue() instanceof FormFieldModel) {
                if (fm.items.get(e.getKey()) == null)
                    fm.items.put(e.getKey(), e.getValue());
            }
        }


        //fm.copyFrom();
        /*
         * String xn = this.getXTypeName(); EBag eb = allEnums.getTarget(xn); if (eb
         * == null) { eb = this.createEbag(); allEnums.put(xn, eb); } return
         * eb.formModel;
         */

        return fm;
    }

    protected String defaultIcon() {
        return "fa  fa-gears";
    }

}
