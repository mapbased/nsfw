package com.mapkc.nsfw.handler;

import com.mapkc.nsfw.input.FormFieldModel;
import com.mapkc.nsfw.input.ParamFormModel;
import com.mapkc.nsfw.model.FormModel;
import com.mapkc.nsfw.model.ParamField;
import com.mapkc.nsfw.model.RenderContext;
import com.mapkc.nsfw.model.XEnum;
import com.mapkc.nsfw.util.VolatileBag;

import java.lang.reflect.Field;
import java.lang.reflect.Parameter;

/**
 * Created by chy on 15/2/5.
 */
public class MethodCall extends BaseRPCActionHandler {

    @Override
    public boolean filterAction(RenderContext rc) {
        String id = rc.param("id");
        BaseRPCActionHandler baseRPCActionHandler = (BaseRPCActionHandler) rc.getSite().getFragment(id).getHandler();
        MethodBag methodBag = baseRPCActionHandler.getMehthodBag(rc.p("name"));
        Parameter[] ps = methodBag.getMethod().getParameters();
        FormModel model = new FormModel();

        Comment comment = methodBag.getMethod().getAnnotation(Comment.class);
        rc.setVar("comment", methodBag.getMethodName() + ":" + (comment == null ? "" : comment.value()));

        float sort = 0;
        for (Parameter parameter : ps) {
            ParamField paramField = parameter.getAnnotation(ParamField.class);

            if (paramField != null) {

                Class c = parameter.getType();


                while (c != null) {
                    FormModel formMode = null;
                    ParamFormModel formModelName = (ParamFormModel) c.getAnnotation(ParamFormModel.class);
                    if (formModelName != null) {
                        formMode = (FormModel) rc.getSite().getXEnum(formModelName.value());
                    }

                    Field[] fields = c.getDeclaredFields();
                    for (Field fld : fields) {

                        ParamField pf = fld.getAnnotation(ParamField.class);
                        if (pf != null) {
                            String name = pf.field().length() > 0 ? pf.field() : fld.getName();
                            if (formMode != null) {
                                VolatileBag<XEnum> formFieldModelVolatileBag = formMode.getItems().get(name);
                                if (formFieldModelVolatileBag != null) {
                                    model.addSingleChild(name, formFieldModelVolatileBag);
                                    continue;
                                }
                            }
                            FormFieldModel formFieldModel = new FormFieldModel();
                            formFieldModel.setName(name);
                            formFieldModel.setSchemaField(false);
                            formFieldModel.setSort(sort++);
                            formFieldModel.formInput = "text";
                            // formFieldModel.setMsg();
                            formFieldModel.setScreenName(name);

                            model.addSingleChild(name, new VolatileBag<XEnum>(formFieldModel));


                        }

                        //if(fld.getTarget)

                    }

                    c = c.getSuperclass();
                }


            } else if (parameter.getType() == RenderContext.class) {
                continue;

            } else {
                FormFieldModel formFieldModel = new FormFieldModel();


                formFieldModel.setName(parameter.getName());
                Name name = parameter.getAnnotation(Name.class);
                if (name != null) {
                    formFieldModel.setName(name.value());
                }
                formFieldModel.setSchemaField(false);
                formFieldModel.setSort(sort++);
                formFieldModel.formInput = "text";//+ (parameter.getType().isPrimitive() ? "<type=number>" : "");

                formFieldModel.setScreenName(formFieldModel.getName());
                Comment comment1 = parameter.getAnnotation(Comment.class);
                if (comment1 != null)
                    formFieldModel.setMsg(comment1.value());

                model.addSingleChild(formFieldModel.getName(), new VolatileBag<XEnum>(formFieldModel));


            }

        }
        rc.setVar("fm", model);


        return super.filterAction(rc);
    }
}
