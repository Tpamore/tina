package com.tp.library;

import android.app.Activity;
import android.os.Bundle;

import com.tpa.client.tina.Tina;
import com.tpa.client.tina.TinaException;
import com.tpa.client.tina.callback.TinaSingleCallBack;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /**
         * 日志tag ---> okhttp
         */
        Tina.build()
                .call(new LoginContract.LoginRequest())
                .callBack(new TinaSingleCallBack<LoginContract.LoginResponse>() {
                    @Override
                    public void onSuccess(LoginContract.LoginResponse response) {

                    }

                    @Override
                    public void onFail(TinaException exception) {

                    }
                })
                .request();
    }
}
