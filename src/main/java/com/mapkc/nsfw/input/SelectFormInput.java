package com.mapkc.nsfw.input;

import com.mapkc.nsfw.model.RenderContext;

import java.util.ArrayList;
import java.util.List;

public class SelectFormInput extends MultiValueFormInput {

    @Override
    public List<String> toParam(String v, FormFieldModel ffm, RenderContext rc) {
        if (ffm.getBoolAttribute("multiple")) {
            return super.toParam(v, ffm, rc);
        }
        ArrayList<String> al = new ArrayList<String>(1);
        al.add(v);
        return al;

    }

    @Override
    public String fromParam(List<String> v, FormFieldModel ffm, RenderContext rc) {

        if (v == null || v.size() == 0) {
            return null;
        }
        if (ffm.getBoolAttribute("multiple")) {
            String sep = this.getSeparator(ffm);

            StringBuilder sb = new StringBuilder();
            for (String s : v) {
                sb.append(s).append(sep);

            }
            return sb.toString().trim();
        } else {
            return v.get(0);

        }

    }

}
