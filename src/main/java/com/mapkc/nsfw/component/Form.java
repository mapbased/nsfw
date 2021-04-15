package com.mapkc.nsfw.component;

import com.mapkc.nsfw.FKNames;
import com.mapkc.nsfw.input.FormHandler;
import com.mapkc.nsfw.model.*;
import com.mapkc.nsfw.util.VolatileBag;
import com.mapkc.nsfw.util.logging.ESLogger;
import com.mapkc.nsfw.util.logging.Loggers;
import org.jsoup.nodes.Element;
import org.mvel2.MVEL;

public class Form extends Component implements ActionHandler {

    final static ESLogger log = Loggers.getLogger(Form.class);

    static final Property[] properties = new Property[]{

            // new Property.RenderablePro("model", "模型"),
            new Property.RenderablePro("okPage", "成功地址"),
            new Property.Str("template", "模板"),
            new Property.Str("actionType", "类型"),


    };

    String template;
    String actionType;
    Renderable okPage;
    Renderable content;
    String handlerExp;
    // Renderable model;
    FieldBag<FormModel> modelbag;

    VolatileBag<Fragment> fragmentBag;

    @Override
    public void render(RenderContext rc) {
        // rc.addAttribute(Constants.VAR_FORM_MODEL, fm.getObj());

        this.content.render(rc);

    }


    @Override
    public boolean filterAction(RenderContext rc) {
        FormModel model = this.modelbag.get(rc);
        if (model == null) {
            rc.sendServerError("Cannot find form model", null);
            return true;
        }
        rc.setVar(FKNames.FK_MODEL, model);
        if (this.actionType != null) {
            rc.setVar(FKNames.FK_ACTIONTYPE, this.actionType);
        }

        model.doHeadables(rc);


        if (model.isSubmit(rc)) {
            try {
                boolean stop = model.handleSubmit(rc, this.getFormHandler());
                if (stop) {
                    return true;
                }
            } catch (Exception e) {
                //	e.printStackTrace();
                rc.addError("exception", e.getMessage());
                log.debug("Error while submit:{}", e, model.getScreenName());
            }
            if (rc.hasError()) {
                return false;
            } else {
                if (this.okPage != null) {
                    String page = this.okPage.getRenderValue(rc);
                    if (page != null && page.length() > 0) {
                        rc.sendRedirect(this.okPage.getRenderValue(rc));

                        return true;
                    }
                }
                return false;

            }
        } else {
            model.valueToReq(rc, this.getFormHandler());

        }

        return false;

    }

    private FormHandler getFormHandler() {


        ActionHandler actionHandler = fragmentBag.value.getHandler();
        if (handlerExp == null) {


            if (actionHandler instanceof FormHandler) {
                return (FormHandler) actionHandler;
            }
        } else {

            if (actionHandler != null) {
                Object o = MVEL.eval(handlerExp, actionHandler);
                if (o instanceof FormHandler) {
                    return (FormHandler) o;
                }
                log.error("Cannot set FormHandler:{}, Fragment:{}", handlerExp, fragmentBag.value);

            }

        }
        return null;
    }

    @Override
    public void parseXml(Element ele, LoadContext lc) {
        super.parseProperties(properties, ele, lc);

        this.modelbag = FieldBag.from(lc.fetchAttribute(ele, "model"),
                FormModel.class, lc);
        handlerExp = lc.fetchAttribute(ele, "form-handler");
        this.fragmentBag = (VolatileBag) lc.site.getXEnumBagCreateIfEmpty(lc.getLoadingFragment().getId());//getLoadingFragment();


        //
        if (template == null) {
            this.content = lc.createChild().parseElement(ele).getRenderable();
        } else {
            throw new java.lang.RuntimeException("Not Complete");
        }
    }

    @Override
    public void toXml(XmlContext xc) {
        // TODO Auto-generated method stub

    }
}