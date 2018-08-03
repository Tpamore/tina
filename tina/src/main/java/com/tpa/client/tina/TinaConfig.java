package com.tpa.client.tina;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;

/**
 * Created by tangqianfeng on 16/9/26.
 */
public interface TinaConfig {

    /** httpclient配置 **/
    public @NonNull OkHttpClient getOkhttpClient();

    /** mediaType配置 **/
    public @Nullable MediaType getMediaType();

    /** 根地址 **/
    public @NonNull String getHost();

    /** 成功请求过滤 **/
    public @Nullable TinaFilter getTinaFilter();

    /** request数据转换器 **/
    public @Nullable TinaConvert getRequestConvert();
}

