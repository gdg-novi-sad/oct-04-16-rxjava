package com.gdgns.android.rxjava;

import android.app.Application;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;
import timber.log.Timber;

public class MyApp
      extends Application {

    private static MyApp instance;
    private RefWatcher refWatcher;

    public static MyApp get() {
        return instance;
    }

    public static RefWatcher getRefWatcher() {
        return MyApp.get().refWatcher;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        instance = (MyApp) getApplicationContext();
        refWatcher = LeakCanary.install(this);

        Timber.plant(new Timber.DebugTree());
    }
}
