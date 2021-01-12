package com.atom.api;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collection;
import java.util.concurrent.RejectedExecutionException;

public interface ApiImplContext {

    <T> Collection<Class<? extends T>> getApiImpls(Class<T> requiredType);

    <T> Class<? extends T> getApiImpl(String name, Class<T> requiredType);

    <T> T getApi(Class<T> requiredType);

    <T> T getApi(String name, Class<T> requiredType);

    <T> T getApi(String name, Class<T> requiredType, long version);

    <T> T hasApi(Class<T> requiredType);

    <T> T hasApi(String name, Class<T> requiredType);

    <T> T newApi(Class<T> requiredType);

    Context getAppContext();

    String getString(int id);

    Bitmap getBitmap(int id);

    Bitmap decodeAssets(String path);

    @NonNull
    String cachePut(@NonNull Object data);

    @Nullable
    Object cacheGet(@NonNull String key);

    @Nullable
    Object cacheRemove(@NonNull String key);

    void enableImpl(@NonNull String name, Boolean enable);

    @Nullable
    Boolean isImplEnabled(@NonNull String name);

    boolean isDebugEnabled();

    void debug(Object msg);

    void debug(StackTraceElement stack, Object msg);

    void enableDebug(boolean enable);

    void reportException(String tag, Throwable throwable);

    void execute(Runnable command);

    void execute(Runnable command, long priority);

    boolean post(Runnable command);

    boolean postDelayed(Runnable command, long delayMillis);

}
