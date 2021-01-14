package com.atom.core;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.atom.annotation.Impl;
import com.atom.annotation.bean.ApiImpls;
import com.atom.api.ApiImplContext;
import com.atom.api.ApiImplContextAware;
import com.atom.core.utils.ClassUtils;

import android.text.TextUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.ref.WeakReference;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

public abstract class AbstractApiImplContext implements ApiImplContext {

    private final Map<String, WeakReference<Object>> mCaches = new HashMap<>();
    private final Map<String, Object> mSingletonBeans = new HashMap<>();
    private final Collection<String> mEnabledImpls = new Vector<>();
    private final Collection<String> mDisabledImpls = new Vector<>();
    private final Application mApplication;
    private final List<ApiImpls> mApiImpls = new LinkedList<>();
    private final ExecutorService mExecutorService;
    private long mCachesLastCheckTime = System.currentTimeMillis();
    private boolean mIsDebug = false;

    public AbstractApiImplContext(Application application) {
        mApplication = application;
        mExecutorService = Executors.newCachedThreadPool();
        loadPackages();
    }

    private void loadPackages() {
        List<Class<?>> classes = ClassUtils.getClasses("com.atom.apt");
        for (Class<?> clazz : classes
        ) {
            ApiImpls apiImpls = null;
            try {
                apiImpls = (ApiImpls) clazz.newInstance();
            } catch (IllegalAccessException | InstantiationException e) {
                e.printStackTrace();
            }
            if (apiImpls instanceof ApiImplContextAware) {
                ((ApiImplContextAware) apiImpls).setApiImplContext(this);
            }
            mApiImpls.add(apiImpls);
        }
    }

    @Override
    public <T> Collection<Class<? extends T>> getApiImpls(Class<T> requiredType) {
        List<Map.Entry<Class<? extends T>, ApiImpls.NameVersion>> entryList = filterApiImpls(requiredType);
        List<Class<? extends T>> classes = new ArrayList<>();
        for (Map.Entry<Class<? extends T>, ApiImpls.NameVersion> entry : entryList
        ) {
            classes.add(entry.getKey());
        }
        return Collections.unmodifiableCollection(classes);
    }

    @Override
    public <T> Class<? extends T> getApiImplByVersion(Class<T> requiredType, long version) {
        return getApiImpl(requiredType, null, version, false);
    }

    @Override
    public <T> Class<? extends T> getApiImplByName(Class<T> requiredType, String name) {
        return getApiImplByName(requiredType, name, 0);
    }

    @Override
    public <T> Class<? extends T> getApiImplByName(Class<T> requiredType, String name, long version) {
        return getApiImpl(requiredType, name, version, false);
    }

    @Override
    public <T> Class<? extends T> getApiImplByRegex(Class<T> requiredType, String regex) {
        return getApiImplByRegex(requiredType, regex, 0);
    }

    @Override
    public <T> Class<? extends T> getApiImplByRegex(Class<T> requiredType, String regex, long version) {
        return getApiImpl(requiredType, regex, version, true);
    }

    @Override
    public <T> Class<? extends T> getApiImpl(Class<T> requiredType, String name, long version, boolean useRegex) {
        return findApiImpl(name, version, requiredType, useRegex);
    }

    @Override
    public <T> T getApi(Class<T> requiredType) {
        return getApi(requiredType, null, 0, false);
    }

    @Override
    public <T> T getApiByName(Class<T> requiredType, String name) {
        return getApiByName(requiredType, name, 0);
    }

    @Override
    public <T> T getApiByName(Class<T> requiredType, String name, long version) {
        return getApi(requiredType, name, version, false);
    }

    @Override
    public <T> T getApiByRegex(Class<T> requiredType, String regex) {
        return getApiByRegex(requiredType, regex, 0);
    }

    @Override
    public <T> T getApiByRegex(Class<T> requiredType, String regex, long version) {
        return getApi(requiredType, regex, version, true);
    }

    @Override
    public <T> T getApi(Class<T> requiredType, String name, long version, boolean useRegex) {
        String key = convertKey(name, requiredType);
        synchronized (mSingletonBeans) {
            if (mSingletonBeans.containsKey(key)) {
                return (T) mSingletonBeans.get(key);
            }
        }
        Class<? extends T> impl = findApiImpl(name, version, requiredType, useRegex);
        if (impl == null) {
            if (!Modifier.isAbstract(requiredType.getModifiers())) {
                impl = requiredType;
            }
        }
        T api = createApiImpl(impl);
        synchronized (mSingletonBeans) {
            mSingletonBeans.put(key, api);
        }
        return api;
    }

    @Override
    public <T> T newApiImpl(Class<T> api) {
        Class<? extends T> impl;
        if (Modifier.isAbstract(api.getModifiers())) {
            impl = findApiImpl(null, 0, api, false);
        } else {
            impl = api;
        }
        return createApiImpl(impl);
    }

    private String convertKey(String name, Class<?> type) {
        String key = type.getSimpleName();
        if (TextUtils.isEmpty(name)) {
            return key;
        }
        if (key.equalsIgnoreCase(name)) {
            return key;
        }
        return key + "#" + name;
    }

    private <T> Class<? extends T> findApiImpl(String name, long version, Class<T> apiClass, boolean useRegex) {
        List<Map.Entry<Class<? extends T>, ApiImpls.NameVersion>> imps = filterApiImpls(apiClass);
        if (!TextUtils.isEmpty(name)) {
            imps = filterApiImplsByNameAndRegex(name, imps, useRegex);
        }
        return filterApiImplByVersion(version, imps);
    }

    private <T> List<Map.Entry<Class<? extends T>, ApiImpls.NameVersion>> filterApiImpls(Class<T> requiredType) {
        List<Map.Entry<Class<? extends T>, ApiImpls.NameVersion>> apiImpls = new LinkedList<>();
        String name;
        Boolean enabled;
        for (ApiImpls imp : mApiImpls) {
            Map<Class<? extends T>, ApiImpls.NameVersion> apiImplsMap = imp.getApiImpls(requiredType);
            if (apiImplsMap != null) {
                for (Map.Entry<Class<? extends T>, ApiImpls.NameVersion> entry : apiImplsMap.entrySet()) {
                    ApiImpls.NameVersion value = entry.getValue();
                    if (value != null) {
                        name = value.getName();
                        if (!name.isEmpty()) {
                            enabled = isImplEnabled(name);
                            if (enabled != null && !enabled) {
                                continue;
                            }
                        }
                    }
                    apiImpls.add(entry);
                }
            }
        }
        return apiImpls;
    }

    private <T> Class<? extends T> filterApiImplByVersion(long version, List<Map.Entry<Class<? extends T>, ApiImpls.NameVersion>> apiImps) {
        if (version >= 0) {
            for (Map.Entry<Class<? extends T>, ApiImpls.NameVersion> entry : apiImps
            ) {
                if (entry.getValue() != null && entry.getValue().getVersion() == version) {
                    return entry.getKey();
                }
            }
        } else {
            int size = apiImps.size();
            Collections.sort(apiImps, (o1, o2) -> (int) (o1.getValue().getVersion() - o2.getValue().getVersion()));
            int idx = size + (int) version;
            if (idx < 0) {
                idx = 0;
            }
            return apiImps.get(idx).getKey();
        }
        return null;
    }

    private <T> List<Map.Entry<Class<? extends T>, ApiImpls.NameVersion>> filterApiImplsByNameAndRegex(String name, List<Map.Entry<Class<? extends T>, ApiImpls.NameVersion>> apiImps, boolean useRegex) {
        List<Map.Entry<Class<? extends T>, ApiImpls.NameVersion>> temp = new ArrayList<>();
        for (Map.Entry<Class<? extends T>, ApiImpls.NameVersion> imp : apiImps) {
            if (imp.getValue() == null) continue;
            if (!useRegex && name.equals(imp.getValue().getName())) {
                temp.add(imp);
            } else if (useRegex && Pattern.matches(name, imp.getValue().getName())) {
                temp.add(imp);
            }
        }
        return temp;
    }

    @Nullable
    private <T> T createApiImpl(Class<? extends T> impl) {
        if (impl == null) {
            return null;
        }
        T obj = null;
        try {
            obj = impl.newInstance();
        } catch (Exception ex) {

        }
        if (obj instanceof ApiImplContextAware) {
            ((ApiImplContextAware) obj).setApiImplContext(this);
        }
        return obj;
    }

    @Override
    public <T> T hasApi(Class<T> requiredType) {
        T api = hasApi(requiredType, "");
        if (api != null) {
            return api;
        }
        api = hasApi(requiredType, requiredType.getSimpleName());
        if (api != null) {
            return api;
        }
        String key = convertKey("", requiredType);
        synchronized (mSingletonBeans) {
            for (Object obj : mSingletonBeans.values()) {
                if (requiredType.isInstance(obj)) {
                    //noinspection unchecked
                    api = (T) obj;
                    mSingletonBeans.put(key, api);
                    return api;
                }
            }
        }
        return null;
    }

    @Override
    public <T> T hasApi(Class<T> requiredType, String name) {
        if (name == null) {
            name = "";
        }
        String key = convertKey(name, requiredType);
        synchronized (mSingletonBeans) {
            if (mSingletonBeans.containsKey(key)) {
                //noinspection unchecked
                return (T) mSingletonBeans.get(key);
            }
        }
        return null;
    }

    @Override
    public <T> T newApi(Class<T> api) {
        Class<? extends T> impl;
        if (Modifier.isAbstract(api.getModifiers())) {
            impl = findApiImpl(null,  0, api, false);
        } else {
            impl = api;
        }
        return createApiImpl(impl);
    }

    @Override
    public Context getAppContext() {
        return mApplication;
    }

    @Override
    public String getString(int id) {
        return mApplication.getString(id);
    }

    @Override
    public Bitmap getBitmap(int id) {
        return BitmapFactory.decodeResource(mApplication.getResources(), id);
    }

    @Override
    public Bitmap decodeAssets(String path) {
        InputStream localInputStream = null;
        try {
            AssetManager am = mApplication.getAssets();
            localInputStream = am.open(path);
            return BitmapFactory.decodeStream(localInputStream);
        } catch (IOException ex) {
            throw new IllegalArgumentException(ex);
        } finally {
            if (localInputStream != null) {
                try {
                    localInputStream.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    @Override
    @NonNull
    public String cachePut(@NonNull Object data) {
        if (System.currentTimeMillis() - mCachesLastCheckTime > 24 * 3600 * 1000L) {
            mCachesLastCheckTime = System.currentTimeMillis();
            cacheGC();
        }
        String key = String.valueOf(System.currentTimeMillis());
        synchronized (mCaches) {
            while (mCaches.containsKey(key)) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException ignored) {
                }
                key = String.valueOf(System.currentTimeMillis());
            }
            mCaches.put(key, new WeakReference<>(data));
        }
        return key;
    }

    protected void cacheGC() {
        synchronized (mCaches) {
            Iterator<Map.Entry<String, WeakReference<Object>>> iterator = mCaches.entrySet().iterator();
            Map.Entry<String, WeakReference<Object>> entry;
            while (iterator.hasNext()) {
                entry = iterator.next();
                if (entry.getValue().get() == null) {
                    iterator.remove();
                }
            }
        }
    }

    @Override
    @Nullable
    public Object cacheGet(@NonNull String key) {
        WeakReference<Object> weakReference;
        synchronized (mCaches) {
            weakReference = mCaches.get(key);
        }
        if (weakReference == null) {
            return null;
        }
        return weakReference.get();
    }

    @Override
    @Nullable
    public Object cacheRemove(@NonNull String key) {
        synchronized (mCaches) {
            return mCaches.remove(key);
        }
    }

    /**
     * Enable or disable implements by name
     *
     * @param name   name  of implements
     * @param enable true enable, false disable
     */
    @Override
    public void enableImpl(@NonNull String name, Boolean enable) {
        name = name.trim();
        synchronized (mEnabledImpls) {
            if (enable == null) {
                mEnabledImpls.remove(name);
                mDisabledImpls.remove(name);
            } else if (enable) {
                if (!mEnabledImpls.contains(name)) {
                    mEnabledImpls.add(name);
                }
                mDisabledImpls.remove(name);
            } else {
                mEnabledImpls.remove(name);
                if (!mDisabledImpls.contains(name)) {
                    mDisabledImpls.add(name);
                }
            }
        }
    }

    /**
     * Return the implements is enabled or not
     *
     * @param name name of implements
     * @return true or false
     */
    @Override
    public Boolean isImplEnabled(@NonNull String name) {
        synchronized (mEnabledImpls) {
            if (mEnabledImpls.contains(name)) {
                return true;
            }
            if (mDisabledImpls.contains(name)) {
                return false;
            }
        }
        return null;
    }

    @Override
    public boolean isDebugEnabled() {
        return mIsDebug;
    }

    @Override
    public void debug(Object msg) {
        if (mIsDebug) {
            debug(Thread.currentThread().getStackTrace()[3], msg);
        }
    }

    @Override
    public void debug(StackTraceElement stack, Object msg) {
        Log.w("AppDebug", stack.getClassName() + "." + stack.getMethodName() + "(" + stack.getFileName() + ":" + stack.getLineNumber() + ") " + msg);
    }

    @Override
    public void enableDebug(boolean enable) {
        mIsDebug = enable;
    }

    @Override
    public void reportException(String tag, Throwable ex) {
        // TODO 进行bug的本地和远程上传等操作
    }

    @Override
    public void execute(Runnable command) {
        mExecutorService.execute(command);
    }

    @Override
    public void execute(Runnable command, long priority) {
        mExecutorService.execute(new PrioritizedRunnable(command, priority));
    }

    /**
     * Prioritized Runnable
     */
    protected static class PrioritizedRunnable implements Runnable, Comparable<PrioritizedRunnable> {
        private final long timestamp;
        private final Runnable runnable;

        public PrioritizedRunnable(Runnable runnable, long timestamp) {
            this.runnable = runnable;
            this.timestamp = timestamp;
        }

        public long getTimestamp() {
            return timestamp;
        }

        @Override
        public int compareTo(PrioritizedRunnable secondOne) {
            return Long.compare(this.getTimestamp(), secondOne.getTimestamp());
        }

        @Override
        public void run() {
            runnable.run();
        }
    }
}
