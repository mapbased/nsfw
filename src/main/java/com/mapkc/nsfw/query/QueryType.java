package com.mapkc.nsfw.query;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;

/**
 * Created by chy on 15/2/1.
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(value = {FIELD})

public @interface QueryType {

    String field();

    Class[] types();

    String[] values();

    String[] schemas();

}
