package com.mapkc.nsfw.model;

import com.mapkc.nsfw.input.FormField;

import java.io.IOException;

/**
 * Created by chy on 14-4-21.
 */
public class StaticFile extends XEnum implements ReqHandler {


    @FormField(caption = "Content", input = "richtext", sort = 10000)
    String content;

    @Override
    protected void loadChildren(Site site) throws IOException {

    }

    protected String defaultIcon() {
        return "fa  fa-download";
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.dianziq.nsfw.model.Handler#handle(RenderContext
     * )
     */
    @Override
    public void handle(RenderContext rc) {
        rc.getSiteStore().serviceRes(rc.getPath(), rc);

        // TODO Auto-generated method stub

    }
}
