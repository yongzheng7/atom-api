package com.atom.core;

import android.app.Service;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.multidex.MultiDexApplication;

import com.atom.api.AbstractApiImplContext;
import com.atom.api.ApiImplContext;
import com.atom.api.ApiImplContextApplication;

import java.lang.ref.WeakReference;

public abstract class AbstractApplication extends MultiDexApplication implements ApiImplContextApplication {

    protected final Handler mHandler = new Handler(getMainLooper());
    protected WeakReference<ApiImplContext> ApiImplContextWeakReference = null;

    @NonNull
    @Override
    public ApiImplContext getApiImplContext() {
        ApiImplContext apiImplContext = null;
        if (ApiImplContextWeakReference != null) {
            apiImplContext = ApiImplContextWeakReference.get();
        }
        if (apiImplContext == null) {
            apiImplContext = new AbstractApiImplContext(this) {

                @Override
                public boolean post(Runnable command) {
                    return mHandler.post(command);
                }

                @Override
                public boolean postDelayed(Runnable command, long delayMillis) {
                    return mHandler.postDelayed(command, delayMillis);
                }
            };
            ApiImplContextWeakReference = new WeakReference<>(apiImplContext);
        }
        return apiImplContext;
    }
}
