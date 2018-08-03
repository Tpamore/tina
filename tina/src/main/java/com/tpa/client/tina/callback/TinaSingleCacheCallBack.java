package com.tpa.client.tina.callback;

import android.support.annotation.MainThread;

import com.tpa.client.tina.TinaException;

/**
 * Created by tangqianfeng on 17/8/30.
 */
public interface TinaSingleCacheCallBack<T>{

    @MainThread
    public void onSuccess(T response);

    @MainThread
    public void onCache(T response);

    @MainThread
    public void onFail(TinaException exception);

}
