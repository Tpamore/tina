package com.tpa.client.tina.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by tangqianfeng on 16/9/28.
 * <p>
 *     忽略字段的注入。
 *     在AutoMode下运行。
 * </p>
 */
@Retention(RUNTIME)
@Target({ElementType.FIELD,ElementType.TYPE})
@Inherited
public @interface IgnoreInfate {

}
