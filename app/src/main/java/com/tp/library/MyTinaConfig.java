package com.tp.library;

import android.content.Context;

import com.readystatesoftware.chuck.ChuckInterceptor;
import com.tpa.client.tina.TinaConfig;
import com.tpa.client.tina.TinaConvert;
import com.tpa.client.tina.TinaFilter;
import com.tpa.client.tina.filter.JsonFilter;
import com.tpa.client.tina.interceptor.HttpLoggingInterceptor;

import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;

/**
 * Created by tangqianfeng on 2019/1/14.
 */

public class MyTinaConfig implements TinaConfig{

    public static final String host = "http://192.168.31.233:8080";
    Context context;

    public MyTinaConfig(Context context) {
        this.context = context;
    }

    @Override
    public OkHttpClient getOkhttpClient() {
        OkHttpClient client = new OkHttpClient.Builder()
                .cache(new Cache(context.getCacheDir() , 1024 * 1024 * 20)) //缓存区大小配置
                .connectTimeout(30000L, TimeUnit.MILLISECONDS)
                .readTimeout(30000L, TimeUnit.MILLISECONDS)
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))//后台日志
                .addInterceptor(new ChuckInterceptor(context)) // 前台日志
                .build();
        return client;
    }

    @Override
    public MediaType getMediaType() {
        return MediaType.parse("application/json;charset=utf-8");
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public TinaFilter getTinaFilter() {
        return JsonFilter.build();//未对code和数据校验，建议根据业务需求重写。
    }

    @Override
    public TinaConvert getRequestConvert() {
        return MyRequestConverter.build();//
    }
}
