package com.mapkc.nsfw.input;

import com.mapkc.nsfw.model.LoadContext;
import com.mapkc.nsfw.model.RenderContext;
import com.mapkc.nsfw.model.Renderable;

/**
 * Created by chy on 14-8-11.
 */
public class CustomFormInput extends DefaultFormInput {

    @Override
    public void render(RenderContext rc) {
        FormFieldModel ffm = (FormFieldModel) rc.v("fk-fm");
        if (ffm == null) {
            return;
        }
        Renderable readable = (Renderable)
                rc.getSite().getWeakKeyCache().get(ffm);
        if (readable == null) {
            readable = LoadContext.getRenderable(ffm.getAttribute("content"));
            rc.getSite().getWeakKeyCache().put(ffm, readable);
        }
        readable.render(rc);
    }
}
