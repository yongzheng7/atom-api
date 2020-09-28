package com.atom.api;

import java.util.Collection;

/**
 * <pre>
 * Package Information interface, call before call any other API.
 *
 * implements must has a constructor().
 *
 * </pre>
 */
public interface ApiImplBundle {

    /**
     * Get Api Implements class.
     *
     * @param api Api class
     * @param <T> Application Programming Interface
     * @return Api Implements class
     */
    <T> Collection<Class<? extends T>> getApiImpls(Class<T> api);

    /**
     * (Optional) The resource id of the icon for the bundle.
     *
     * @return The resource id of the icon for the bundle.
     */
    int getBundleIcon();

    /**
     * (Optional) The resource id of the label for the bundle.
     *
     * @return The resource id of the label for the bundle.
     */
    int getBundleLabel();

    /**
     * (Optional) The resource id of the details for the bundle.
     *
     * @return The resource id of the details for the bundle.
     */
    int getBundleDetails();
}
