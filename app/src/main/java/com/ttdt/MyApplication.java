package com.ttdt;

import android.app.Application;

import com.ttdt.Util.Util;

/**
 * Created by Administrator on 2017/9/19.
 */

public class MyApplication extends Application {

    private static Application instance = null;

    public static Application getInstance(){
         return instance;
    }

    @Override
    public void onCreate() {
        instance = this;
        Util.init(this);
    }
}
