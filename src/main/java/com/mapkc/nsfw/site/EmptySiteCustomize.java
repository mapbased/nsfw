package com.mapkc.nsfw.site;

import com.mapkc.nsfw.model.ActionHandler;
import com.mapkc.nsfw.model.RenderContext;
import com.mapkc.nsfw.model.Site;
import com.mapkc.nsfw.ses.SessionValueCreator;
import io.netty.handler.codec.http.HttpResponseStatus;

public class EmptySiteCustomize implements SiteCustomize {

    public static final EmptySiteCustomize INSTANCE = new EmptySiteCustomize();

    private EmptySiteCustomize() {

    }

    @Override
    public ActionHandler createSiteFilter() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object getUserId(RenderContext rc) {
        // TODO Auto-generated method stub
        return 1;
    }

    @Override
    public boolean isAdmin(RenderContext renderContext) {
        return true;
    }

    @Override
    public boolean isAlpha() {
        return true;
    }

    @Override
    public String loginPage() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String errorPage(HttpResponseStatus status) {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public SessionValueCreator getSessionValueCreator() {
        return null;
    }


    @Override
    public void init(Site site) {
        // TODO Auto-generated method stub

    }

}
