package com.tpa.client.tina.callback;

import android.support.annotation.MainThread;

import com.tpa.client.tina.TinaException;

/**
 * Created by tangqianfeng on 16/9/22.
 */
public interface TinaChainCallBack<T>{

    @MainThread
    public  Object onSuccess(Object feedbackResult , T response);

    @MainThread
    public  void onFail(TinaException exception);

}
