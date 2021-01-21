package com.atom.runtime;

import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.atom.annotation.bean.ApiImpls;

import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

public class AtomApi {

    public interface ApiContextAware {
        void setApiContextAware(AtomApi atomApi);
    }

    public interface UIThreadHandler {

        boolean post(Runnable action);

        boolean postDelayed(Runnable action, long delayMillis);
    }

    public interface IOThreadHandler {

        void execute(Runnable command);

        Future<?> submit(Runnable task);
    }

    public interface ExceptionHandler {

        void report(String tag, Throwable throwable);
    }

    public interface ApiFilter<T> {

        boolean accept(Class<? extends T> clazz, ApiImpls.NameVersion param);
    }

    private static class SingletonInner {
        private static AtomApi singletonStaticInner = new AtomApi();
    }

    public static AtomApi init(Application application) {
        AtomApi init = init();
        init.mApplication = application;
        return init;
    }

    public static AtomApi init() {
        return SingletonInner.singletonStaticInner;
    }

    private static final Set<Class<? extends ApiImpls>> registerClass = new HashSet<>();

    private static void loadProxyClass() {
    }

    private static void registerClass(String className) {
        if (!TextUtils.isEmpty(className)) {
            try {
                Class<?> clazz = Class.forName(className);
                if (ApiImpls.class.isAssignableFrom(clazz)) {
                    registerClass.add((Class<? extends ApiImpls>) clazz);
                }
            } catch (Exception e) {
                Log.e("register class error:" + className, Objects.requireNonNull(e.getLocalizedMessage()));
            }
        }
    }

    private Application mApplication;
    private AtomApi.UIThreadHandler mUIThreadHandler;
    private AtomApi.IOThreadHandler mIOThreadHandler;
    private AtomApi.ExceptionHandler mExceptionHandler;

    private final Map<String, WeakReference<Object>> mCaches = new HashMap<>();
    private final Map<String, Object> mSingletonBeans = new HashMap<>();
    private final Collection<String> mEnabledImpls = new Vector<>();
    private final Collection<String> mDisabledImpls = new Vector<>();
    private final List<ApiImpls> mApiImpls = new LinkedList<>();
    private long mCachesLastCheckTime = System.currentTimeMillis();

    private AtomApi() {
        loadProxyClass();
        loadPackages();
    }

    private void loadPackages() {
        for (Class<? extends ApiImpls> clazz : registerClass) {
            try {
                ApiImpls apiImpls = clazz.newInstance();
                if (apiImpls instanceof ApiContextAware) {
                    ((ApiContextAware) apiImpls).setApiContextAware(this);
                }
                mApiImpls.add(apiImpls);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public <T> Collection<Class<? extends T>> getApis(@NonNull Class<T> requiredType) {
        List<Map.Entry<Class<? extends T>, ApiImpls.NameVersion>> entryList = filterApiImpls(requiredType);
        List<Class<? extends T>> classes = new ArrayList<>();
        for (Map.Entry<Class<? extends T>, ApiImpls.NameVersion> entry : entryList
        ) {
            classes.add(entry.getKey());
        }
        return Collections.unmodifiableCollection(classes);
    }

    public <T> Collection<Class<? extends T>> getApis(@NonNull Class<T> requiredType, @NonNull String name, boolean useRegex) {
        List<Map.Entry<Class<? extends T>, ApiImpls.NameVersion>> entryList = filterApiImpls(requiredType);
        if (!TextUtils.isEmpty(name)) {
            entryList = filterApiImplsByNameAndRegex(name, entryList, useRegex);
        }
        List<Class<? extends T>> classes = new ArrayList<>();
        for (Map.Entry<Class<? extends T>, ApiImpls.NameVersion> entry : entryList
        ) {
            classes.add(entry.getKey());
        }
        return Collections.unmodifiableCollection(classes);
    }

    public <T> Collection<Class<? extends T>> getApis(@NonNull Class<T> requiredType, @NonNull ApiFilter<T> filter) {
        List<Map.Entry<Class<? extends T>, ApiImpls.NameVersion>> entryList = filterApiImpls(requiredType);
        List<Class<? extends T>> classes = new ArrayList<>();
        for (Map.Entry<Class<? extends T>, ApiImpls.NameVersion> entry : entryList
        ) {
            if (filter.accept(entry.getKey(), entry.getValue())) {
                classes.add(entry.getKey());
            }
        }
        return Collections.unmodifiableCollection(classes);
    }

    public <T> Class<? extends T> getApi(Class<T> requiredType) {
        return getApi(requiredType, null, 0, false);
    }

    public <T> Class<? extends T> getApi(Class<T> requiredType, long version) {
        return getApi(requiredType, null, version, false);
    }

    public <T> Class<? extends T> getApi(Class<T> requiredType, String name, long version, boolean useRegex) {
        return findApiImpl(requiredType, name, version, useRegex);
    }

    public <T> T getImpl(Class<T> requiredType) {
        return getImpl(requiredType, null, 0, false);
    }

    public <T> T getImpl(Class<T> requiredType, long version) {
        return getImpl(requiredType, null, version, false);
    }

    public <T> T getImpl(Class<T> requiredType, String name, long version, boolean useRegex) {
        T impl = hasApi(requiredType, name, version);
        if (impl != null) return impl;
        Class<? extends T> implClass = findApiImpl(requiredType, name, version, useRegex);
        if (implClass == null) {
            if (!Modifier.isAbstract(requiredType.getModifiers())) {
                implClass = requiredType;
            }
        }
        impl = createApiImpl(implClass);
        synchronized (mSingletonBeans) {
            mSingletonBeans.put(convertKey(requiredType, name, version), impl);
        }
        return impl;
    }

    public <T> T hasApi(Class<T> requiredType, String name, long version) {
        String key = convertKey(requiredType, name, version);
        synchronized (mSingletonBeans) {
            if (mSingletonBeans.containsKey(key)) {
                //noinspection unchecked
                return (T) mSingletonBeans.get(key);
            }
        }
        return null;
    }

    public <T> T hasApi(Class<T> requiredType) {
        T t = hasApi(requiredType, null, 0);
        if (t != null) {
            return t;
        }
        String key = convertKey(requiredType, null, 0);
        synchronized (mSingletonBeans) {
            for (Object obj : mSingletonBeans.values()) {
                if (requiredType.isInstance(obj)) {
                    //noinspection unchecked
                    t = (T) obj;
                    mSingletonBeans.put(key, t);
                    return t;
                }
            }
        }
        return null;
    }

    public <T> T newApi(Class<T> api, String name, long version) {
        Class<? extends T> impl;
        if (Modifier.isAbstract(api.getModifiers())) {
            impl = findApiImpl(api, name, version, false);
        } else {
            impl = api;
        }
        return createApiImpl(impl);
    }


    private String convertKey(Class<?> type, String name, long version) {
        String key = type.getSimpleName();
        if (TextUtils.isEmpty(name) || key.equalsIgnoreCase(name)) {
            return key + "$$" + version;
        }
        return key + "$" + name + "$" + version;
    }


    private <T> T createApiImpl(Class<? extends T> impl) {
        if (impl == null) {
            return null;
        }
        T obj = null;
        try {
            obj = impl.newInstance();
        } catch (Exception ex) {

        }
        if (obj instanceof ApiContextAware) {
            ((ApiContextAware) obj).setApiContextAware(this);
        }
        return obj;
    }


    private <T> Class<? extends T> findApiImpl(Class<T> apiClass, String name, long version, boolean useRegex) {
        List<Map.Entry<Class<? extends T>, ApiImpls.NameVersion>> imps = filterApiImpls(apiClass);
        if (!TextUtils.isEmpty(name)) {
            imps = filterApiImplsByNameAndRegex(name, imps, useRegex);
        }
        return filterApiImplByVersion(version, imps);
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
                            enabled = getImplEnabled(name);
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


    /*缓存*/
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

    @Nullable
    public Object cacheRemove(@NonNull String key) {
        synchronized (mCaches) {
            return mCaches.remove(key);
        }
    }

    /*预设api 是否能够获得*/

    public void setImplEnabled(@NonNull String name, Boolean enable) {
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

    public Boolean getImplEnabled(@NonNull String name) {
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

    /*方法拓展,可以方便的使用getString getResources等*/
    public Context getAppContext() {
        return mApplication;
    }

    public String getString(int id) {
        return mApplication.getString(id);
    }

    public Bitmap getBitmap(int id) {
        return BitmapFactory.decodeResource(mApplication.getResources(), id);
    }

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

    @NonNull
    public AtomApi setUIThreadHandler(AtomApi.UIThreadHandler uiThreadHandler) {
        this.mUIThreadHandler = uiThreadHandler;
        return this;
    }

    @NonNull
    public AtomApi setIOThreadHandler(AtomApi.IOThreadHandler ioThreadHandler) {
        this.mIOThreadHandler = ioThreadHandler;
        return this;
    }

    @NonNull
    public AtomApi setExceptionHandler(AtomApi.ExceptionHandler exceptionHandler) {
        this.mExceptionHandler = exceptionHandler;
        return this;
    }

    public boolean post(@NonNull Runnable action) {
        return mUIThreadHandler != null && mUIThreadHandler.post(action);
    }

    public boolean postDelayed(@NonNull Runnable action, long delayMillis) {
        return mUIThreadHandler != null && mUIThreadHandler.postDelayed(action, delayMillis);
    }

    public void execute(@NonNull Runnable command) {
        if (mIOThreadHandler != null) {
            mIOThreadHandler.execute(command);
        }
    }

    public Future<?> submit(@NonNull Runnable task) {
        return mIOThreadHandler != null ? mIOThreadHandler.submit(task) : null;
    }

    public void report(@NonNull String tag, Throwable throwable) {
        if (mExceptionHandler != null) {
            mExceptionHandler.report(tag, throwable);
        }
    }
}
