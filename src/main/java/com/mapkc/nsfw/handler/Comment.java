package com.mapkc.nsfw.handler;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

/**
 * Created by chy on 15/1/26.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {CONSTRUCTOR, FIELD, METHOD, PACKAGE, PARAMETER, TYPE})
public @interface Comment {

    String value();


}
