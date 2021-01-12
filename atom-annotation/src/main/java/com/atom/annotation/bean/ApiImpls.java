package com.atom.annotation.bean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class ApiImpls {

    private final Map<Class<?>, ApiImps<?>> mApiImps = new HashMap<>();

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

    @SuppressWarnings("unchecked")
    public <T> Collection<Class<? extends T>> getApiImpls(Class<T> apiClass) {
        synchronized (mApiImps) {
            return (ApiImps<T>) mApiImps.get(apiClass);
        }
    }

    private static class ApiImps<T> extends ArrayList<Class<? extends T>> {

    }
}
