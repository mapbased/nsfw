package com.mapkc.nsfw.input;

import com.mapkc.nsfw.model.RenderContext;

import java.util.List;

public class RichtextFormInput extends DefaultFormInput {

    @Override
    public String fromParam(List<String> v, FormFieldModel ffm, RenderContext rc) {

        String s = super.fromParam(v, ffm, rc);
        // s = org.jsoup.Jsoup.clean(s, Whitelist.relaxed());
        return s;

    }


}
