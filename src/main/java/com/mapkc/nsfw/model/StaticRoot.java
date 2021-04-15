/**
 *
 */
package com.mapkc.nsfw.model;

import java.io.IOException;


/**
 * @author chy
 */
public class StaticRoot extends XEnum implements ReqHandler {

    @Override
    protected void loadChildren(Site site) throws IOException {

    }

    protected String defaultIcon() {
        return "fa  fa-download";
    }
    // @Override
    // public XEnumType getXEnumType() {
    // return XEnumType.StaticRoot;
    // }

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
