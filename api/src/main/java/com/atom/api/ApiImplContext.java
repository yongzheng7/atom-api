package com.atom.api;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collection;

public interface ApiImplContext {

    <T> Collection<Class<? extends T>> getApiImpls(Class<T> requiredType);

    <T> Class<? extends T> getApiImplByVersion(Class<T> requiredType, long version);

    <T> Class<? extends T> getApiImplByName(Class<T> requiredType, String name);

    <T> Class<? extends T> getApiImplByName(Class<T> requiredType, String name, long version);

    <T> Class<? extends T> getApiImplByRegex(Class<T> requiredType, String regex);

    <T> Class<? extends T> getApiImplByRegex(Class<T> requiredType, String regex, long version);

    <T> Class<? extends T> getApiImpl(Class<T> requiredType, String name, long version, boolean useRegex);

    <T> T getApi(Class<T> requiredType);

    <T> T getApiByName(Class<T> requiredType, String name);

    <T> T getApiByName(Class<T> requiredType, String name, long version);

    <T> T getApiByRegex(Class<T> requiredType, String regex);

    <T> T getApiByRegex(Class<T> requiredType, String regex, long version);

    <T> T getApi(Class<T> requiredType, String name, long version, boolean useRegex);

    <T> T newApiImpl(Class<T> requiredType);

    <T> T hasApi(Class<T> requiredType);

    <T> T hasApi(Class<T> requiredType , String name);

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

    void post(Runnable command);

    void postDelayed(Runnable command, long delayMillis);
}
