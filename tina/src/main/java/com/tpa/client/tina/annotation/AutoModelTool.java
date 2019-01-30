package com.tpa.client.tina.annotation;

import com.tpa.client.tina.TinaDataException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by tangqianfeng on 16/9/28.
 */
public class AutoModelTool {

    /**
     * 数据模型自动注入 填充所有空对象
     * "干掉所有model判空代码!"
     * "这是后台该干的事?"
     *
     * @param injectHostClass
     * @param host
     */
    public static void inflateClassBean(Class<?> injectHostClass, Object host) throws TinaDataException {

        /**
         * 空对象不注入
         */
        if (host == null) {
            return;
        }

        /**
         * 基础数据类型不填充
         */
        if (host.getClass().getName().contains("java.lang")) {
            return;
        }

        /**
         * 有忽略注解不填充
         */
        if (injectHostClass.isAnnotationPresent(IgnoreInfate.class)) {
            return;
        }

        /**
         * 集合数据inflate
         */
        if (host instanceof Collection) {
            Collection collect = (Collection) host;
            for (Iterator it = collect.iterator(); it.hasNext(); ) {
                Object item = it.next();
                if (item != null) {
                    inflateClassBean(item.getClass(), item);
                }
            }
            return;
        }

        /**
         * map数据inflate
         */
        if (host instanceof Map) {
            Map map = (Map) host;
            for (Object key : map.keySet()) {
                Object item = map.get(key);
                if (item != null) {
                    inflateClassBean(item.getClass(), item);
                }
            }
        }

        /**
         * 原生类型不注入
         */
        if (isJavaClass(injectHostClass)) {
            return;
        }

        Field[] fields = injectHostClass.getDeclaredFields();
        if (fields != null && fields.length > 0) {
            for (Field field : fields) {
                /**
                 * 递归不注入
                 */
                if (field.getType().equals(injectHostClass)) {
                    continue;
                }

                /**
                 * 自定义注解解析
                 */
                if (field.getAnnotations() != null) {
                    for (Annotation annotation : field.getAnnotations()) {
                        TinaAnnotationHandler handler = TinaAnnotationManager.getInstance().getHanlder(annotation.annotationType());
                        handler.hanld(annotation, host, field);
                    }
                }


                /**
                 * 基础类型,静态类型及标注IgnoreInfate 不填充
                 */
                else if (!field.getType().isPrimitive() && field.getModifiers() != Modifier.STATIC
                        && !field.isAnnotationPresent(IgnoreInfate.class)) {

                    if (field.getType().isArray()) {
                        continue;
                    }

                    field.setAccessible(true);
                    Class<?> clazz = field.getType();
                    try {
                        Object object = field.get(host);
                        if (object != null) {
                            inflateClassBean(object.getClass(), object);
                            continue;
                        }
                        if (clazz.isInterface()) {
                            String className = clazz.getName();
                            switch (className) {
                                case "java.util.List":
                                    clazz = ArrayList.class;
                                    break;
                                case "java.util.Map":
                                    clazz = HashMap.class;
                                    break;
                                case "java.util.Set":
                                    clazz = HashSet.class;
                                    break;
                                default:
                                    return;
                            }
                        }
                        Object newInstance = clazz.newInstance();
                        field.set(host, newInstance);
                        inflateClassBean(newInstance.getClass(), newInstance);
                    } catch (IllegalArgumentException | IllegalAccessException | InstantiationException ignored) {
                    }
                }
            }
        }
    }

    /**
     * 判断一个类是JAVA类型还是用户定义类型
     *
     * @param clz
     * @return
     */
    public static boolean isJavaClass(Class<?> clz) {
        return clz != null && clz.getClassLoader() == null;
    }

}
