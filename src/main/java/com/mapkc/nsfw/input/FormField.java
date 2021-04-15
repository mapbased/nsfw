package com.mapkc.nsfw.input;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * to auto generate formmodel
 *
 * @author howard
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FormField {

    String input() default "";

    String caption();

    // String attributes() default "";

    boolean required() default false;

    boolean key() default false;

    boolean readonly() default false;

    String msg() default "";

    float sort() default 0;

    String hidden() default "false";

    String defaultValue() default "";


    // String valueList() default "";
    //
    // String templateName() default "";

}