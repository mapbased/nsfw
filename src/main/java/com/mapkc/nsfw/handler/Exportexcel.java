package com.mapkc.nsfw.handler;

import com.mapkc.nsfw.FKNames;
import com.mapkc.nsfw.model.ActionHandler;
import com.mapkc.nsfw.model.Page;
import com.mapkc.nsfw.model.RenderContext;
import com.mapkc.nsfw.model.ReqHandler;
import io.netty.handler.codec.http.HttpHeaders;

/**
 * Created by chy on 14-11-26.
 */
public class Exportexcel implements ActionHandler {
    @Override
    public boolean filterAction(RenderContext rc) {
        String referer = rc.getReferer();
        String cid = rc.param("cid", "qr");
        //java.net.URL url=new URL(page);
        //String p=url.getPath();
        referer = referer.substring(referer.indexOf('/', 8));
        rc.extractParams(referer);
        rc.setParam(cid + ".p", "0");
        ReqHandler h = rc.getSite().getReqHandlerWithPathFix(rc.getPath(), rc);
        rc.setVar(FKNames.FK_EXPORT_CNT, 4000);
        if (h instanceof Page) {

            Page p = (Page) h;
            rc.setPage(p);
            p.doActions(rc);
            p.getComponent(cid).render(rc);
            rc.addHeader(HttpHeaders.Names.CONTENT_TYPE, "application/vnd.ms-excel");
            rc.addHeader("Content-Disposition", "attachment; filename=" + p.getName() + ".xls");
            rc.finish();
            return true;

        }


        return false;
    }
}
