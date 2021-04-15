package com.mapkc.nsfw.handler;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;

/**
 * Created by chy on 17/5/23.
 */


@Retention(RetentionPolicy.RUNTIME)
@Target(value = {PARAMETER, TYPE})
public @interface Name {
    String value();
}
