package com.atom.apt.annotation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by HYW on 2017/5/25.
 */

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
                imps = new ApiImps<T>();
                mApiImps.put(apiClass, imps);
            }
        }
        Collections.addAll(imps, apiImpls);
    }

    @SuppressWarnings("unchecked")
    public <T> Collection<Class<? extends T>> getApiImpls(Class<T> apiClass) {
        synchronized (mApiImps) {
            System.out.println("getApiImpls " +mApiImps.size() + "  " + mApiImps);
            return (ApiImps<T>) mApiImps.get(apiClass);
        }
    }

    /**
     * (Optional) The resource id of the icon for the bundle.
     *
     * @return The resource id of the icon for the bundle.
     */
    public int getBundleIcon() {
        return 0;
    }

    /**
     * (Optional) The resource id of the label for the bundle.
     *
     * @return The resource id of the label for the bundle.
     */
    public int getBundleLabel() {
        return 0;
    }

    /**
     * (Optional) The resource id of the details for the bundle.
     *
     * @return The resource id of the details for the bundle.
     */
    public int getBundleDetails() {
        return 0;
    }

    private class ApiImps<T> extends ArrayList<Class<? extends T>> {
    }
}
