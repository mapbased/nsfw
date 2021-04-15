package com.mapkc.nsfw.model;

import com.mapkc.nsfw.input.FormField;
import com.mapkc.nsfw.util.VolatileBag;

import java.util.Map;


public class FormableBag<T> extends XEnum {

    @FormField(caption = "Class Name", input = "text")
    Class<T> className;

    protected String defaultIcon() {
        return "fa  fa-check-square-o";
    }

    public T createObj(Map<String, String> values, Site site)
            throws InstantiationException, IllegalAccessException {
        T t = this.className.newInstance();
        FormModel.fromClass(this.className, site).assign(t, values, site);
        return t;

    }

    @Override
    protected void init(Site site) {
        // TODO Auto-generated method stub

        if (this.className != null) {
            FormModel m = FormModel.fromClass(this.className, site);
            this.items = m.items;

            String id = this.getId();
            for (VolatileBag<XEnum> v : this.items.values()) {
                if (v.getValue() != null) {
                    v.getValue().setParentIdRecursive(id);// = id;
                }
            }

        }

        super.init(site);
    }

}
