package com.tpa.client.tina.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by tangqianfeng on 17/2/16.
 * <p>
 *     restful path构造
 * </p>
 */
@Retention(RUNTIME)
@Target(ElementType.FIELD)
@Inherited
public @interface Path {
    String value() default "";
}
