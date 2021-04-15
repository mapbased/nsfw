package com.mapkc.nsfw.input;

import com.mapkc.nsfw.model.RenderContext;

import java.util.ArrayList;
import java.util.List;

/**
 * @author chy
 * todo: 转译特殊字符
 */
public class
MultiValueFormInput extends DefaultFormInput {

    protected String getSeparator(FormFieldModel ffm) {

        String s = ffm.getAttribute("seperate");
        if (s == null || s.equals(""))
            return " ";
        return s;
    }

    @Override
    public String fromParam(List<String> v, FormFieldModel ffm, RenderContext rc) {
        if (v == null || v.size() == 0) {
            return null;
        }
        String sep = this.getSeparator(ffm);

        StringBuilder sb = new StringBuilder();
        for (String s : v) {
            sb.append(s).append(sep);

        }
        return sb.toString().trim();

    }

    @Override
    public List<String> toParam(String v, FormFieldModel ffm, RenderContext rc) {
        if (v == null) {
            return null;
        }
        ArrayList<String> al = new ArrayList<String>(5);


        java.util.Iterator<String> i = com.google.common.base.Splitter
                .on(this.getSeparator(ffm)).trimResults().omitEmptyStrings()
                .split(v).iterator();
        while (i.hasNext()) {
            al.add(i.next());
        }

        return al;
    }

}
