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
                .call(new LoginContract.Request())
                .callBack(new TinaSingleCallBack<LoginContract.Response>() {
                    @Override
                    public void onSuccess(LoginContract.Response response) {

                    }

                    @Override
                    public void onFail(TinaException exception) {

                    }
                })
                .request();
    }
}
