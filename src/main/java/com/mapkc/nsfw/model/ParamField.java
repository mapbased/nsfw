package com.mapkc.nsfw.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by chy on 15/2/3.
 * <p>
 * 表示该字段的值可以从｛@link RenderContext｝的请求参数获取。
 * <p>
 * 在很多字段的提交接口，可用到
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.FIELD, ElementType.PARAMETER})
public @interface ParamField {
    String field() default "";
}
