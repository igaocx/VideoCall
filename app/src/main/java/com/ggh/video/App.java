package com.ggh.video;

import android.app.Application;

import com.gyz.voipdemo_speex.util.Speex;


/**
 * Created by Administrator on 2018/1/8 0008.
 */

public class App extends Application {
    public static App app;
    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        Speex.getInstance().init();
    }
}
