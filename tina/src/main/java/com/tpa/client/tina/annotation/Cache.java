package com.tpa.client.tina.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * Created by tangqianfeng on 17/1/20.
 * <p>
 *      key:缓存key，默认为请求的url
 *      expire:缓存过期时间，-1为永不过期。
 *      unit:缓存时间单位，默认为秒
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Cache {
    String key() default "";
    int expire() default -1;
    TimeUnit unit() default TimeUnit.SECONDS;
}
