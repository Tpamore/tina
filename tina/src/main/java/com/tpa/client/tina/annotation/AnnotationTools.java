package com.tpa.client.tina.annotation;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by tangqianfeng on 16/9/28.
 */
public class AnnotationTools {

    public static final String[] NUMBER_PARSE = {"String", "float", "double"};

    /**
     * 数据模型自动注入 填充所有空对象
     * "干掉所有model判空代码!"
     * "这是后台该干的事?"
     *
     * @param injectHostClass
     * @param host
     */
    public static void inflateClassBean(Class<?> injectHostClass, Object host) {

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
                 * 对数值进行scale操作  同BigDecimal.setScale()
                 */
                if (field.isAnnotationPresent(NumberScale.class) && isCanParse(field)) {
                    NumberScale numberScale = field.getAnnotation(NumberScale.class);
                    int scale = numberScale.value();
                    String typeName = field.getType().getSimpleName();
                    switch (typeName) {
                        case "String":
                            try {
                                field.setAccessible(true);
                                String fieldValue = (String) field.get(host);
                                if (fieldValue == null || "".equals(fieldValue)) {
                                    field.set(host, "0");
                                } else {
                                    String parseValue = numberFormat(Double.parseDouble(fieldValue), scale);
                                    field.set(host, parseValue);
                                }
                            } catch (Exception e) {
                            }
                            break;
                        case "float":
                            try {
                                field.setAccessible(true);
                                float fieldValue = field.getFloat(host);
                                float parseValue = Float.parseFloat(numberFormat(fieldValue, scale));
                                field.setFloat(host, parseValue);
                            } catch (Exception e) {
                            }
                            break;
                        case "double":
                            try {
                                field.setAccessible(true);
                                double fieldValue = field.getDouble(host);
                                double parseValue = Double.parseDouble(numberFormat(fieldValue, scale));
                                field.setDouble(host, parseValue);
                            } catch (Exception e) {
                            }
                            break;
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
     * 判断字段是否可以转换成数字
     *
     * @param field
     * @return
     */
    private static boolean isCanParse(Field field) {
        String simpleName = field.getType().getSimpleName();
        for (String name : NUMBER_PARSE) {
            if (name.equals(simpleName)) {
                return true;
            }
        }
        return false;
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

    public static String numberFormat(double value, int scale) {

        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMaximumFractionDigits(scale);

        nf.setRoundingMode(RoundingMode.HALF_EVEN);
        /**
         * 是否需要用逗号隔开
         */
        nf.setGroupingUsed(true);
        return nf.format(value);
    }

    public static String numberFormat(float value, int scale) {

        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMaximumFractionDigits(scale);

        nf.setRoundingMode(RoundingMode.HALF_EVEN);
        /**
         * 是否需要用逗号隔开
         */
        nf.setGroupingUsed(true);
        return nf.format(value);
    }

}
