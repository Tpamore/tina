package com.tp.library;

import com.tpa.client.tina.TinaConvert;

/**
 * Created by tangqianfeng on 2019/1/14.
 * 用来对请求body数据进行加密等操作
 */

public class MyRequestConverter implements TinaConvert{

    @Override
    public String convert(String source) {
//        return Base64.encodeToString(source.getBytes() , Base64.DEFAULT);
        return source;
    }

    public static final TinaConvert build() {
        return new MyRequestConverter();
    }
}
