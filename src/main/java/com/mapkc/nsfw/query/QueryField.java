package com.mapkc.nsfw.query;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by chy on 15/1/2.
 * <p>
 * 对应 {@link QueryConfig} 或者 {@link QueryInfo } 中的类，通过指定改注解，来加载数据库中的字段
 * <p>
 * field指定获取的字段。
 * 如果提供了schema和SchemaField，则获取与schema对应的schemaField的值（以field的值作为schema的主键）
 * schema2和schemaField2则再获取下一级schema。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface QueryField {

    String field() default "";

    String schema() default "";

    String schemaField() default "";

    String schema2() default "";

    String schemaField2() default "";

    /**
     * 如果对应的字段是一个list，并且指定了schema和sqlwhere，到相应的schema根据 sqlwhere获取数据
     *
     * @return
     */
    String sqlwhere() default "";

    boolean mapped() default false;


}
