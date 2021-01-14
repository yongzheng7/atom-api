package com.atom.annotation.bean;

import java.util.HashMap;
import java.util.Map;


public abstract class ApiImpls {

    private final Map<Class<?>, ApiImplsMap<?>> mApiImpsMap = new HashMap<>();

    public ApiImpls() {
    }

    protected <T> void add(String name, Class<T> apiClass, Class<? extends T> implClass, long version) {
        ApiImplsMap<T> implsMap;
        synchronized (mApiImpsMap) {
            ApiImplsMap<?> apiImplsMap = mApiImpsMap.get(apiClass);
            if (apiImplsMap == null) {
                implsMap = new ApiImplsMap<>();
                mApiImpsMap.put(apiClass, implsMap);
            } else {
                implsMap = (ApiImplsMap<T>) apiImplsMap;
            }
        }
        implsMap.put(implClass, new NameVersion(name, version));
    }

    @SuppressWarnings("unchecked")
    public <T> Map<Class<? extends T>, NameVersion> getApiImpls(Class<T> apiClass) {
        synchronized (mApiImpsMap) {
            return (ApiImplsMap<T>) mApiImpsMap.get(apiClass);
        }
    }

    private static class ApiImplsMap<T> extends HashMap<Class<? extends T>, NameVersion> {

    }

    public static class NameVersion {
        final String name;
        final long version;

        public NameVersion(String name, long value) {
            this.name = name;
            this.version = value;
        }

        public String getName() {
            return name;
        }

        public long getVersion() {
            return version;
        }
    }
}
