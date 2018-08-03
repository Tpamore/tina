package com.tpa.client.tina.utils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Created by tangqianfeng on 17/4/10.
 */

public class ClassUtils {
    /**
     * 获取类的泛型对象类型（T的对象类型）
     * @return
     */
    public static Class getGenericType(Class clazz) {
        Type genType = clazz.getGenericInterfaces()[0];
        if (!(genType instanceof ParameterizedType)) {
            return Object.class;
        }
        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
        if (!(params[0] instanceof Class)) {
            return Object.class;
        }
        return (Class) params[0];
    }
}
