import com.mapkc.nsfw.model.ActionHandler
import com.mapkc.nsfw.model.AutoAssign
import com.mapkc.nsfw.model.RenderContext
import com.mapkc.nsfw.model.Site
import com.mapkc.nsfw.ses.SessionValueCreator
import com.mapkc.nsfw.site.SiteCustomize
import io.netty.handler.codec.http.HttpResponseStatus

public class SiteCust implements SiteCustomize {


    @AutoAssign
    Site site;


    /**
     * 站点自定义的RenderContext
     */
    @Override
    void init(Site site) {

        print("Reloaded...")


        //  site.addType(site.xe('/ds/rocksdb').class)
    }

    /**
     * 通过这个actionhandler可以过滤所有请求
     *
     * @return
     */
    @Override
    ActionHandler createSiteFilter() {
        return null
    }

    @Override
    Object getUserId(RenderContext rc) {

        return 1;//rc.getSessionValue('uid')
    }

    /**
     * 当前用户为管理员
     *
     * @return
     */
    @Override
    boolean isAdmin(RenderContext rc) {

        // return rc.getUserIdAsInt() == 1
     return  true;//   rc.can('admin')
    }

    /**
     * Alpha测试环境
     *
     * @return
     */
    @Override
    boolean isAlpha() {

        return false
    }

    @Override
    String loginPage() {
        return "/login"
    }


    @Override
    String errorPage(HttpResponseStatus status) {
        return "/errors/" + status.code + ".html";
    }
/**
 * 子对象应该缓存SessionValueCreator，避免每次调用创建对象
 *
 * @return
 */
    @Override
    SessionValueCreator getSessionValueCreator() {
        return null
    }

    @Override
    boolean can(String right, RenderContext rc) {
        if (right == null || right.length() == 0) {
            return true
        }
        Set set = rc.getSessionValue('_rights')

        if (!set) {
            return false
        }
        return set.contains("admin") || set.contains(right.trim())
    }

    public String getName(){
        return 'NSFW'
    }



}