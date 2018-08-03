package com.tpa.client.tina.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by tangqianfeng on 16/9/27.
 * <p>
 *     对tina的response进行自动注入，填充所有空对象
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AutoMode {
}
