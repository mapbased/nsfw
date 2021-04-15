/**
 *
 */
package com.mapkc.nsfw.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 对通过dynamaicClassLoader加载的对象自动赋值为相应path对应的XEnum对象
 *
 * @author chy
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface AutoAssign {

    /**
     * 到这个路径下获取XEnum对象
     *
     * @return
     */
    String path() default "";
}
