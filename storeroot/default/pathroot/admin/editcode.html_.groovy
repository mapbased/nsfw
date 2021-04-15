import com.mapkc.nsfw.input.FormFieldModel
import com.mapkc.nsfw.input.FormHandler
import com.mapkc.nsfw.model.*
import com.mapkc.nsfw.util.VolatileBag

public class EditCodeGroovy extends Page {


    FormModel formModel = new FormModel();

    @Override
    protected void init(Site site) {
        super.init(site)
        FormFieldModel formFieldModel = new FormFieldModel();
        formFieldModel.setName(com.mapkc.nsfw.model.XEnum.KnownAttributes.groovyCode.name())
        formFieldModel.setSchemaField(true)
        formFieldModel.formInput = 'code'
        formModel.addSingleChild('groovy', new VolatileBag<XEnum>(formFieldModel))


        FormHandler formHandler = new FormHandler() {
            @Override
            def Map<String, String> load(RenderContext rc, String id, FormModel model) throws IOException {
                return rc.site.getXEnum(rc.p('id')).attributes
            }

            @Override
            boolean update(FormModel model, String id, RenderContext rc, Map<String, String> values) throws IOException {

                if (!id) {
                    id = rc.p('id')
                }
                Map attrs = rc.site.getSiteStore().getAttributes(id)
                String name = com.mapkc.nsfw.model.XEnum.KnownAttributes.groovyCode.name();
                attrs.put(name, values.get(name))
                rc.site.getSiteStore().saveAttributes(id, attrs)


                return false
            }
        }
        formModel.setFormHandler(formHandler)
    }

    public FormModel formModel() {
        //renderContext.currentFragment.formModel();

        // XEnum xEnum = rc.site.getXEnum(rc.param('id'));

        return formModel


    }
}