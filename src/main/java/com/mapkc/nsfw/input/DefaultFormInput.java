package com.mapkc.nsfw.input;

import com.mapkc.nsfw.model.Fragment;
import com.mapkc.nsfw.model.RenderContext;

import java.util.ArrayList;
import java.util.List;

/**
 * @author chy
 */
@FormModelInfo(caption = "")
public class DefaultFormInput extends Fragment implements FormInput {

    @Override
    public String getXTypeName() {
        // TODO Auto-generated method stub
        return "FormInput";
    }


    @Override
    public Fragment getFragment() {
        return this;
    }


    protected String defaultIcon() {
        return "fa fa-pencil ";
    }

    @Override
    public String fromParam(List<String> v, FormFieldModel ffm, RenderContext rc) {
        if (v == null || v.size() == 0) {
            return null;
        }

        return v.get(0);
    }

    @Override
    public List<String> toParam(String v, FormFieldModel ffm, RenderContext rc) {
        ArrayList<String> al = new ArrayList<String>(1);
        al.add(v);
        return al;
    }


    // @Override
    // public XEnumType getXEnumType() {
    // // TODO Auto-generated method stub
    // return XEnumType.FormInput;
    // }


}
