package com.atom.api;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collection;
import java.util.concurrent.Future;

public interface ApiContext {

    <T> Collection<Class<? extends T>> getApis(@NonNull Class<T> requiredType);

    <T> Collection<Class<? extends T>> getApis(@NonNull Class<T> requiredType, @NonNull String name, boolean useRegex);

    <T> Collection<Class<? extends T>> getApis(@NonNull Class<T> requiredType, @NonNull ApiFilter<T> filter);

    <T> Class<? extends T> getApi(Class<T> requiredType);

    <T> Class<? extends T> getApi(Class<T> requiredType, long version);

    <T> Class<? extends T> getApi(Class<T> requiredType, String name, long version, boolean useRegex);

    <T> void setImpl(Class<T> requiredType, String name, long version, T entity);

    <T> T getImpl(Class<T> requiredType);

    <T> T getImpl(Class<T> requiredType, long version);

    <T> T getImpl(Class<T> requiredType, String name, long version, boolean useRegex);

    <T> T hasApi(Class<T> requiredType, String name, long version);

    <T> T hasApi(Class<T> requiredType);

    <T> T newApi(Class<T> api, String name, long version);

    @NonNull
    String cachePut(@NonNull Object data);

    @Nullable
    Object cacheGet(@NonNull String key);

    @Nullable
    Object cacheRemove(@NonNull String key);

    void setImplEnabled(@NonNull String name, Boolean enable);

    Boolean getImplEnabled(@NonNull String name);

    Context getAppContext();

    String getString(int id);

    Bitmap getBitmap(int id);

    Bitmap decodeAssets(String path);

    @NonNull
    ApiContext setUIThreadHandler(UIThreadHandler uiThreadHandler);

    @NonNull
    ApiContext setIOThreadHandler(IOThreadHandler ioThreadHandler);

    @NonNull
    ApiContext setExceptionHandler(ExceptionHandler exceptionHandler);

    boolean post(@NonNull Runnable action);

    boolean postDelayed(@NonNull Runnable action, long delayMillis);

    void execute(@NonNull Runnable command);

    Future<?> submit(@NonNull Runnable task);

    void report(@NonNull String tag, Throwable throwable);
}
