package com.mapkc.nsfw.input;

import com.mapkc.nsfw.model.FormModel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface FormModelInfo {
    String caption();

    String info() default "";

    String schemaName() default "";

    FormModel.ActionType actionType() default FormModel.ActionType.AddOrUpdateUsingGivenId;

}
