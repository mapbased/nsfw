import com.mapkc.nsfw.model.ActionHandler
import com.mapkc.nsfw.model.AutoAssign
import com.mapkc.nsfw.model.RenderContext
import com.mapkc.nsfw.model.Schema
import com.mapkc.nsfw.util.VolatileBag

public class setpwdhtml implements ActionHandler {


    /**
     * 是否已经过滤掉正常的httppage，如果返回true，正常的页面渲染被打断，不再进行后续操作
     *
     * @param rc
     * @return
     */
    @Override
    boolean filterAction(RenderContext rc) {

        if (rc.isPost()) {

            int c = memberBag.value.countBySql("email=?", rc.p("email"))

            if (!c) {
                rc.addError("email", "没有该email，请联系系统管理员添加账号")
            }




        }

        return false
    }
}