import com.mapkc.nsfw.model.GroovyCode
import com.mapkc.nsfw.model.Site
import com.mapkc.nsfw.site.SiteCustomize

public class GroovyCodeSite extends GroovyCode {
    @Override
    protected void init(Site site) {
        super.init(site)

        SiteCustomize siteCustomize = super.getGroovyObj()

        siteCustomize.init(site)

    }
}

