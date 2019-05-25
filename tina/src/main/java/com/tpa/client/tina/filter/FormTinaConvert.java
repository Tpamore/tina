package com.tpa.client.tina.filter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tpa.client.tina.TinaConvert;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by tangqianfeng on 2019/4/4.
 */
public class FormTinaConvert implements TinaConvert {

    @Override
    public String convert(String source) {
        StringBuilder builder = new StringBuilder();
        JSONObject object = JSON.parseObject(source);
        Object[] sets = object.keySet().toArray();
        int length = sets.length;
        for (int i = 0; i < length; i++) {
            String value;
            String orgValue = object.get(sets[i]).toString();
            try {
                value = URLEncoder.encode(orgValue, "UTF8");
            } catch (UnsupportedEncodingException e) {
                value = orgValue;
            }
            builder.append(sets[i].toString())
                    .append("=")
                    .append(value)
                    .append("&");
        }
        if(builder.length() > 0) {
            builder.setLength(builder.length() - 1);
        }
        return builder.toString();
    }
}
