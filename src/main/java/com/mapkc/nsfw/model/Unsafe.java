/**
 *
 */
package com.mapkc.nsfw.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 仅为开发时方便，生产环境可能引起安全问题的方法
 *
 * @author chy
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Unsafe {

}
