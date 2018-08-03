package com.tpa.client.tina.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by tangqianfeng on 17/2/16.
 * <p>
 *     put请求注解。
 *     host为根地址，可以为空，默认为tinaconfig里的配置的地址。
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Put {
    String value() default "";
}
