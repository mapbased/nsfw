import com.mapkc.nsfw.model.ActionHandler
import com.mapkc.nsfw.model.RenderContext

public class  Logout implements ActionHandler{

    @Override
    boolean filterAction(RenderContext rc) {
        rc.addCookie('mobile','',0)
        rc.addCookie('token','',0)
        rc.getSession().invalidate()
        return false
    }
}