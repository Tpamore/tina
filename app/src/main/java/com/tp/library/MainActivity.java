package com.tp.library;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.tpa.client.tina.Tina;
import com.tpa.client.tina.TinaException;
import com.tpa.client.tina.callback.TinaChainCallBack;
import com.tpa.client.tina.callback.TinaEndCallBack;
import com.tpa.client.tina.callback.TinaSingleCallBack;
import com.tpa.client.tina.callback.TinaStartCallBack;

import static android.content.ContentValues.TAG;


public class MainActivity extends Activity {

    LoginContract.Request request = new LoginContract.Request();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.single).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                singleRequest();
            }
        });

        findViewById(R.id.concurrent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                concurrentRequest();
            }
        });

        findViewById(R.id.chains).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chainRequest();
            }
        });

    }

    private void singleRequest() {

        Tina.build()
                .startCallBack(new TinaStartCallBack() {
                    @Override
                    public void start() {
                        Log.d(TAG, "start: ");
                    }
                })
                .endCallBack(new TinaEndCallBack() {
                    @Override
                    public void end() {
                        Log.d(TAG, "end: ");
                    }
                })
                .call(request)
                .callBack(new TinaSingleCallBack<LoginContract.Response>() {
                    @Override
                    public void onSuccess(LoginContract.Response response) {
                        Log.d(TAG, "onSuccess: 1");
                    }

                    @Override
                    public void onFail(TinaException exception) {
                        Log.d(TAG, "onFail: 1");
                    }
                })
                .request();
    }

    private void concurrentRequest() {

        Tina.build(Tina.CONCURRENT)
                .startCallBack(new TinaStartCallBack() {
                    @Override
                    public void start() {
                        Log.d(TAG, "start: ");
                    }
                })
                .endCallBack(new TinaEndCallBack() {
                    @Override
                    public void end() {
                        Log.d(TAG, "end: ");
                    }
                })
                .call(request)
                .call(request)
                .call(request)
                .callBack(new TinaSingleCallBack<LoginContract.Response>() {
                    @Override
                    public void onSuccess(LoginContract.Response response) {
                        Log.d(TAG, "onSuccess: 1");
                    }

                    @Override
                    public void onFail(TinaException exception) {
                        Log.d(TAG, "onFail: 1");
                    }
                })
                .callBack(new TinaSingleCallBack<LoginContract.Response>() {
                    @Override
                    public void onSuccess(LoginContract.Response response) {
                        Log.d(TAG, "onSuccess: 2");
                    }

                    @Override
                    public void onFail(TinaException exception) {
                        Log.d(TAG, "onFail: 2");
                    }
                })
                .callBack(new TinaSingleCallBack<LoginContract.Response>() {
                    @Override
                    public void onSuccess(LoginContract.Response response) {
                        Log.d(TAG, "onSuccess: 3");
                    }

                    @Override
                    public void onFail(TinaException exception) {
                        Log.d(TAG, "onFail: 3");
                    }
                })
                .request();
    }

    /**
     * 请求失败，请求链则会熔断
     */
    private void chainRequest() {
        Tina.build(Tina.CHAINS)
                .startCallBack(new TinaStartCallBack() {
                    @Override
                    public void start() {
                        Log.d(TAG, "start: ");
                    }
                })
                .endCallBack(new TinaEndCallBack() {
                    @Override
                    public void end() {
                        Log.d(TAG, "end: ");
                    }
                })
                .call(request)
                .call(request)
                .call(request)
                .callBack(new TinaChainCallBack<LoginContract.Response>() {
                    @Override
                    public Object onSuccess(Object feedbackResult, LoginContract.Response response) {
                        Log.d(TAG, "onSuccess: 1");
                        return null;
                    }

                    @Override
                    public void onFail(TinaException exception) {
                        Log.d(TAG, "onFail: 1");
                    }
                })
                .callBack(new TinaChainCallBack<LoginContract.Response>() {
                    @Override
                    public Object onSuccess(Object feedbackResult, LoginContract.Response response) {
                        Log.d(TAG, "onSuccess: 2");
                        return null;
                    }

                    @Override
                    public void onFail(TinaException exception) {
                        Log.d(TAG, "onFail: 2");
                    }
                })
                .callBack(new TinaChainCallBack<LoginContract.Response>() {
                    @Override
                    public Object onSuccess(Object feedbackResult, LoginContract.Response response) {
                        Log.d(TAG, "onSuccess: 3");
                        return null;
                    }

                    @Override
                    public void onFail(TinaException exception) {
                        Log.d(TAG, "onFail: 3");
                    }
                })
                .request();
    }




}
