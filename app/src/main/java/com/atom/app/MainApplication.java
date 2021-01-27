package com.atom.app;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.atom.annotation.Impl;
import com.atom.core.AtomApi;


import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Impl(api = MainApplication.class)
public class MainApplication extends Application {

    private final ExecutorService pool = Executors.newCachedThreadPool() ;
    private final Handler handler = new Handler(Looper.getMainLooper()) ;
    @Override
    public void onCreate() {
        super.onCreate();
        AtomApi.newInstance(this , false)
        .setIOThreadHandler(new AtomApi.IOThreadHandler() {
            @Override
            public void execute(Runnable command) {
                pool.execute(command);
            }

            @Override
            public Future<?> submit(Runnable task) {
                return pool.submit(task);
            }
        })
        .setUIThreadHandler(new AtomApi.UIThreadHandler() {
            @Override
            public boolean post(Runnable action) {
                return handler.post(action);
            }

            @Override
            public boolean postDelayed(Runnable action, long delayMillis) {
                return handler.postDelayed(action, delayMillis);
            }
        })
        .setExceptionHandler((tag, throwable) -> {
            Log.e(tag , Objects.requireNonNull(throwable.getLocalizedMessage())) ;
        });
    }
}
