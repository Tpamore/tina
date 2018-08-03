package com.tpa.client.tina.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by tangqianfeng on 16/11/2.
 * <p>
 *    保留小数位操作，银行家四舍五入，支持对string、float、double类型的小数位转换。
 *    只在AutoMote模式下才生效。
 *    支持对集合里的model操作。
 * </p>
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface NumberScale {
    public int value() default 0;
}
