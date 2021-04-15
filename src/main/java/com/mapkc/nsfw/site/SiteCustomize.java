package com.mapkc.nsfw.site;

import com.mapkc.nsfw.model.ActionHandler;
import com.mapkc.nsfw.model.RenderContext;
import com.mapkc.nsfw.model.Site;
import com.mapkc.nsfw.ses.SessionValueCreator;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * TODO 未完成
 * <p>
 * 期望可以注入第三方实现的类，扩展
 *
 * @author chy
 */
public interface SiteCustomize {

    /**
     * 站点自定义的RenderContext
     */
    // public RenderContext createRenderContext();
    void init(Site site);

    /**
     * 通过这个actionhandler可以过滤所有请求
     *
     * @return
     */
    ActionHandler createSiteFilter();

    Object getUserId(RenderContext rc);

    /**
     * 当前用户为管理员
     *
     * @return
     */
    boolean isAdmin(RenderContext rc);

    /**
     * Alpha测试环境
     *
     * @return
     */
    boolean isAlpha();

    String loginPage();

    String errorPage(HttpResponseStatus status);


    /**
     * 子对象应该缓存SessionValueCreator，避免每次调用创建对象
     *
     * @return
     */
    SessionValueCreator getSessionValueCreator();


    default boolean can(String right, RenderContext rc) {
        return false;
    }


}
