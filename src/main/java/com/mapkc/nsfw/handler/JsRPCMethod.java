/**
 *
 */
package com.mapkc.nsfw.handler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author chy
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface JsRPCMethod {

    public AccessMode access() default AccessMode.User;
    public String privilege() default "";

}
