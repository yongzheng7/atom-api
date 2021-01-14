package com.atom.core;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.multidex.MultiDexApplication;

import com.atom.api.ApiImplContext;
import com.atom.api.ApiImplContextApplication;

import java.lang.ref.WeakReference;

public abstract class AbstractApplication extends MultiDexApplication implements ApiImplContextApplication {

    protected WeakReference<ApiImplContext> ApiImplContextWeakReference = null;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("ApiImplContext" , " AbstractApiImplContext onCreate   000 ") ;
    }

    @NonNull
    @Override
    public ApiImplContext getApiImplContext() {
        Log.e("ApiImplContext" , " AbstractApiImplContext init   000123 ") ;
        ApiImplContext apiImplContext = null;
        if (ApiImplContextWeakReference != null) {
            apiImplContext = ApiImplContextWeakReference.get();
        }
        if (apiImplContext == null) {
            apiImplContext = new AbstractApiImplContext(this) {

                @Override
                public void post(Runnable command) {
                }

                @Override
                public void postDelayed(Runnable command, long delayMillis) {
                }
            };
            ApiImplContextWeakReference = new WeakReference<>(apiImplContext);
        }
        return apiImplContext;
    }
}
