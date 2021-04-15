package com.mapkc.nsfw.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记某个字段是一个数据库或其他存储引擎的一个列。
 * <p>
 * 如果字段的名字和数据不一致，使用 field注解。
 * 如果需要到另外一个schema获取关联的值，idValue指定哪个字段的值作为另外一个schema的id值。
 * schema指定另外schema的完整路径。
 *
 * @author chy
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Column {
    /**
     * 对应的数据裤裤字段名
     * 如果返回“”使用字段名
     *
     * @return
     */
    String field() default "";

    /**
     * 本类的这个字段或方法取值作为新schema的id，
     * 如果是方法，方法不能包含任何参数
     *
     * @return
     */
    String idValue() default "";

    /**
     * 到这个schema load数据
     *
     * @return
     */
    String schema() default "";

}
