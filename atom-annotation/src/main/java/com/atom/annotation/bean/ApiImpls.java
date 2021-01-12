package com.atom.annotation.bean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class ApiImpls {

    private final Map<Class<?>, ApiImps<?>> mApiImps = new HashMap<>();

    private final Map<String, Class<?>> mImplsMap = new HashMap<>();

    public ApiImpls() {
    }

    @SuppressWarnings("unchecked")
    protected <T> void add(Class<T> apiClass, Class<? extends T>... apiImpls) {
        ApiImps<T> imps;
        synchronized (mApiImps) {
            imps = (ApiImps<T>) mApiImps.get(apiClass);
            if (imps == null) {
                imps = new ApiImps<>();
                mApiImps.put(apiClass, imps);
            }
        }
        Collections.addAll(imps, apiImpls);
    }

    protected <T> void add(String name, Class<T> apiClass, Class<? extends T> implClass, long version) {
        if (name == null || name.isEmpty()) return;
        final String finalKey = apiClass.getCanonicalName() + "$" + name + "$" + version;
        synchronized (mImplsMap) {
            mImplsMap.put(finalKey, implClass);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> Collection<Class<? extends T>> getApiImpls(Class<T> apiClass) {
        synchronized (mApiImps) {
            return (ApiImps<T>) mApiImps.get(apiClass);
        }
    }

    public <T> Class<? extends T> getApiImpls(Class<T> apiClass, String name, long version) {
        if (name == null || name.isEmpty()) return null;
        final String finalKey = apiClass.getCanonicalName() + "$" + name + "$" + version;
        synchronized (mImplsMap) {
            return (Class<? extends T>) mImplsMap.get(finalKey);
        }
    }

    private static class ApiImps<T> extends ArrayList<Class<? extends T>> {

    }
}
