import com.mapkc.nsfw.handler.BaseRPCActionHandler
import com.mapkc.nsfw.model.AutoAssign
import com.mapkc.nsfw.model.RenderContext
import com.mapkc.nsfw.model.Schema
import com.mapkc.nsfw.util.VolatileBag

public class login extends BaseRPCActionHandler {


    @AutoAssign(path = "/ds/db/member")
    VolatileBag<Schema> user;
    /**
     * 是否已经过滤掉正常的httppage，如果返回true，正常的页面渲染被打断，不再进行后续操作
     *
     * @param rc
     * @return
     */
    @Override
    boolean filterAction(RenderContext rc) {
        String from = rc.p("from");
        if (from == null || from.length() == 0 || from == '/') {
            from = "/";
        }
        if (rc.isPost()) {


        }
        return false;
    }


}