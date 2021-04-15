package com.mapkc.nsfw.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by chy on 15/3/3.
 * <p>
 * 标记实体类对应哪个schema
 */


@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.TYPE})
public @interface SchemaInfo {
    String value();
}
