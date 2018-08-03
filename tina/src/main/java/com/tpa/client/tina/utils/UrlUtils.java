package com.tpa.client.tina.utils;

import com.tpa.client.tina.annotation.Path;
import com.tpa.client.tina.model.TinaBaseRequest;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URLEncoder;

/**
 * Created by tangqianfeng on 17/2/5.
 */
public class UrlUtils {

    /**
     * 根据request vo对象生成url属性上下文。
     * 带有参数
     * @param request
     * @return
     */
    public static String generatePathWithParams(String path, TinaBaseRequest request) {
        String[] schemas = path.split("/");
        Class clazz = request.getClass();
        Field[] fields = clazz.getDeclaredFields();
        if (fields == null || fields.length <= 0) {
            return parseArrayToString(schemas, path);
        }
        StringBuilder urlParams = new StringBuilder();
        urlParams.append("?");
        for (int i = 0; i < fields.length; i++) {

            Field field = fields[i];
            /**
             * 忽略静态类型
             */
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            field.setAccessible(true);
            String paramValue = null;
            Object obValue = null;
            try {
                obValue = field.get(request);
                if (obValue != null) {
                    paramValue = obValue.toString();
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                continue;
            }

            if (paramValue == null) {
                continue;
            }

            paramValue = URLEncoder.encode(paramValue);

            Path pathAnn = field.getAnnotation(Path.class);
            if (pathAnn != null) {
                String pathKey = pathAnn.value();
                schemas = replace(schemas, "{" + pathKey + "}", paramValue);
                continue;
            } else {
                try {
                    String pathKey = ":" + field.getName();
                    if (path.contains(pathKey + "?")) {
                        schemas = replace(schemas, pathKey + "?", paramValue);
                        continue;
                    } else if (path.contains(pathKey)) {
                        schemas = replace(schemas, pathKey, paramValue);
                        continue;
                    }

                } catch (Exception e) {
                    throw new IllegalArgumentException("rest path build failed , " + e.getMessage());
                }
            }

            try {
                String paramName = field.getName();
                /**
                 * 基础类型不序列化
                 */
                if (!obValue.getClass().getName().contains("java.lang")) {
                    paramValue = JSONHelper.objToJson(obValue);
                }

                urlParams.append(paramName);
                urlParams.append("=");
                urlParams.append(paramValue);
                if (i != fields.length - 1) {
                    urlParams.append("&");
                }
            } catch (Exception e) {
                continue;
            }
        }

        if (urlParams.length() <= 1) {
            return parseArrayToString(schemas, path);
        }

        return parseArrayToString(schemas, path) + urlParams.toString();
    }


    /**
     * 根据request vo对象生成url属性上下文。
     * 不带有参数
     *
     * @param request
     * @return
     */
    public static String generatePathWithoutParams(String path, TinaBaseRequest request) {
        String[] schemas = path.split("/");
        Class clazz = request.getClass();
        Field[] fields = clazz.getDeclaredFields();
        if (fields == null || fields.length <= 0) {
            return parseArrayToString(schemas, path);
        }
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];

            /**
             * 忽略静态类型
             */
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            field.setAccessible(true);
            String paramValue = null;
            try {
                Object obValue = field.get(request);
                if (obValue != null) {
                    paramValue = obValue.toString();
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                continue;
            }

            if (paramValue == null) {
                continue;
            }

            Path pathAnn = field.getAnnotation(Path.class);
            if (pathAnn != null) {
                String pathKey = pathAnn.value();
                schemas = replace(schemas, "{" + pathKey + "}", paramValue);
            } else {
                String pathKey = ":" + field.getName();
                if (path.contains(pathKey + "?")) {
                    schemas = replace(schemas, pathKey + "?", paramValue);
                } else if (path.contains(pathKey)) {
                    schemas = replace(schemas, pathKey, paramValue);
                }
            }
        }
        return parseArrayToString(schemas, path);
    }


    private static String[] replace(String[] params, String oldChar, String newChar) {
        for (int i = 0; i < params.length; i++) {
            if (params[i].equals(oldChar)) {
                params[i] = newChar;
            }
        }
        return params;
    }

    private static String parseArrayToString(String[] schemas, String path) {
        StringBuilder builder = new StringBuilder();
        boolean spe = path.endsWith("/");
        for (int i = 0; i < schemas.length; i++) {
            /**
             *  如果剩余的schemas里包含':value?'形式的字段则过滤掉
             *  如果剩余的schemas里包含':value'形式的字段则表示有未匹配替换成功的字段 或者 匹配的字段为空
             */
            if (schemas[i].endsWith("?") && schemas[i].startsWith(":")) {
                continue;
            } else if (schemas[i].startsWith(":")) {
                throw new IllegalArgumentException("There is a illegal param in url," + schemas[i] + ", param is null or not match?");
            }
            builder.append(schemas[i]);
            if (i == schemas.length - 1 && !spe) {
                continue;
            }
            builder.append("/");
        }
        return builder.toString();
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
