package com.mapkc.nsfw.valid;

import com.mapkc.nsfw.input.FormField;
import com.mapkc.nsfw.model.*;

import java.util.Map;

public class DefaultValidator extends XEnum implements Validator {

    @Override
    public String getXTypeName() {
        return "Validator";
    }

    public final static String VALID_ROOT = "/valid";
    @FormField(required = true, caption = "校验器", input = "radio<dynamicFields=true;vl=/valid>")
    private String validName;


    private Validator valid;

    // @Override
    // public XEnumType getXEnumType() {
    // // TODO Auto-generated method stub
    // return XEnumType.Validator;
    // }

    protected String defaultIcon() {
        return "fa   fa-check-square-o";
    }

    @Override
    protected void init(Site site) {
        // TODO Auto-generated method stub
        super.init(site);
        XEnum x = site.getXEnum(this.VALID_ROOT + "/" + this.validName);
        if (x instanceof FormableBag) {
            try {
                valid = (Validator) ((FormableBag) x).createObj(
                        this.attributes,
                        site);

            } catch (InstantiationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
        if (this.valid == null) {
            this.valid = ValidatorParser.get(this.validName, this.attributes,
                    site);

        }

    }

    @Override
    public void renderJS(RenderContext rc) {

    }

    @Override
    public void validate(RenderContext rc, Schema schema,
                         Map<String, ? extends Object> values, String name) {
        this.valid.validate(rc, schema, values, name);

    }

}
