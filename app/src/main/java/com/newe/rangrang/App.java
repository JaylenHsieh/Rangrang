package com.newe.rangrang;

import android.app.Application;

import com.tencent.bugly.crashreport.CrashReport;

/**
 * @author Jaylen Hsieh
 * @date 2018/04/30
 * @email jaylenhsieh@qq.com
 */
public class App extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        CrashReport.initCrashReport(getApplicationContext(), "4c33c18762", BuildConfig.DEBUG);
    }
}
