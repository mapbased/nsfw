package com.mapkc.nsfw.input;

import com.mapkc.nsfw.model.Fragment;
import com.mapkc.nsfw.model.RenderContext;

import java.util.List;

/**
 * @author chy
 */
public interface FormInput {


    String fromParam(List<String> v, FormFieldModel ffm, RenderContext rc);

    Fragment getFragment();

    List<String> toParam(String v, FormFieldModel ffm, RenderContext rc);


}
