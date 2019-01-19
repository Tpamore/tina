package com.tpa.client.tina.callback;

import android.support.annotation.MainThread;

/**
 * Created by tangqianfeng on 17/8/30.
 */
public interface TinaSingleCacheCallBack<T> extends TinaSingleCallBack<T> {

    @MainThread
    public void onCache(T response);

}
