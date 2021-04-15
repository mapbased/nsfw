/**
 *
 */
package com.mapkc.nsfw.handler;

import com.mapkc.nsfw.model.ActionHandler;
import com.mapkc.nsfw.model.RenderContext;

/**
 * @author chy
 *         默认的ActionHandler,没有太多实际用途
 */
public class DefaultActionHandler implements ActionHandler {

    /**
     *
     */
    public DefaultActionHandler() {
        // TODO Auto-generated constructor stub
    }

    public String cfId(RenderContext rc) {
        return rc.getCurrentFragment().getId();
    }

    /* (non-Javadoc)
     * @see ActionHandler#filterAction(RenderContext)
     */
    @Override
    public boolean filterAction(RenderContext rc) {

        return false;
    }

}
