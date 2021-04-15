package com.mapkc.nsfw.handler;

import com.mapkc.nsfw.model.Fragment;
import com.mapkc.nsfw.model.RenderContext;
import com.mapkc.nsfw.model.XEnum;
import org.jsoup.nodes.Element;

public class Designer extends BaseRPCActionHandler {

    /**
     * @param args
     */
    public static void main(String[] args) {


    }

    @JsRPCMethod(access = AccessMode.Admin)
    public String eleSrc(String path, int id, RenderContext rc) {
        XEnum x = rc.getSite().getXEnum(path);
        if (x instanceof Fragment) {
            Fragment f = (Fragment) x;
            Element e = f.designElements.get(id);
            return e.outerHtml();
        }
        return null;
    }
}
