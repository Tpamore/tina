package com.tp.library;

import android.app.Application;

import com.tpa.client.tina.Tina;

/**
 * Created by tangqianfeng on 2019/1/14.
 */

public class BaseApplication extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        Tina.initConfig(new MyTinaConfig(this));
    }
}
