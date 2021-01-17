package com.atom.app;

import android.app.Application;

import com.atom.annotation.Impl;
import com.atom.runtime.AtomApi;

@Impl(api = MainApplication.class)
public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AtomApi.init(this);
    }
}
