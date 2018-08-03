package com.tpa.client.tina.utils;

import com.alibaba.fastjson.JSON;

/**
 * Created by tangqianfeng on 17/1/22.
 */

public class JSONHelper {

    public static String objToJson(Object obj) {
        try {
            return JSON.toJSONString(obj);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    public static Object jsonToObject(String json, Class t) {
        try {
            Object object = JSON.parseObject(json, t);
            return object;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }
}
