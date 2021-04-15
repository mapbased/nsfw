package com.mapkc.nsfw.input;

import com.mapkc.nsfw.model.RenderContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chy on 15/8/14.
 */
public class SelTxtFormInput extends DefaultFormInput {


    @Override
    public String fromParam(List<String> v, FormFieldModel ffm, RenderContext rc) {
        if (v == null || v.size() == 0) {
            return null;
        }
        return rc.p(ffm.getName() + "__prx") + "::" + v.get(0);


    }

    @Override
    public List<String> toParam(String v, FormFieldModel ffm, RenderContext rc) {
        if (v == null) {
            return null;
        }
        ArrayList<String> al = new ArrayList<String>(5);

        int idx = v.indexOf("::");

        if (idx >= 0) {
            String result = v.substring(idx + 2);
            al.add(result);
            rc.setParam(ffm.getName() + "__prx", v.substring(0, idx));
            return al;
        }
        al.add(v);
        return al;


    }
}
