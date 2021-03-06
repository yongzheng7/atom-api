package com.atom.core;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.atom.annotation.bean.ApiImpls;
import com.atom.api.ApiContext;
import com.atom.api.ApiContextAware;
import com.atom.api.ApiFilter;
import com.atom.api.ExceptionHandler;
import com.atom.api.IOThreadHandler;
import com.atom.api.UIThreadHandler;

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

public class AtomApi implements ApiContext {

    private static final Set<Class<? extends ApiImpls>> registerClass = new HashSet<>();

    private static final String META_DATA_NAME = "com.atom.apt.proxy";

    private static volatile AtomApi singleTon = null;

    public static AtomApi getInstance() {
        return singleTon;
    }

    public static AtomApi newInstance(Application application, boolean loadPlugin) {
        if (singleTon == null) {
            synchronized (AtomApi.class) {
                if (singleTon == null) {
                    singleTon = new AtomApi(application, loadPlugin);
                }
            }
        }
        return singleTon;
    }

    private static void loadProxyClass() {

    }

    private static void loadProxyClassByManifest(Application application) {
        if (application == null) {
            throw new RuntimeException("Application is NULL");
        }
        ApplicationInfo appInfo;
        try {
            appInfo = application.getPackageManager().getApplicationInfo(application.getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
        for (String key : appInfo.metaData.keySet()) {
            if (!key.startsWith(META_DATA_NAME)) {
                continue;
            }
            registerClass(appInfo.metaData.getString(key, null));
        }
    }

    private static void registerClass(String className) {
        Log.e("register class ", Objects.requireNonNull(className));
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

    private final Application mApplication;
    private UIThreadHandler mUIThreadHandler;
    private IOThreadHandler mIOThreadHandler;
    private ExceptionHandler mExceptionHandler;

    private final Map<String, WeakReference<Object>> mCaches = new HashMap<>();
    private final Map<String, Object> mSingletonBeans = new HashMap<>();
    private final Collection<String> mEnabledImpls = new Vector<>();
    private final Collection<String> mDisabledImpls = new Vector<>();
    private final List<ApiImpls> mApiImpls = new LinkedList<>();
    private long mCachesLastCheckTime = System.currentTimeMillis();

    private AtomApi(Application application, boolean isLoadPlugin) {
        this.mApplication = application;
        if (isLoadPlugin) {
            loadProxyClass();
        } else {
            loadProxyClassByManifest(mApplication);
        }
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
    @Override
    public <T> Collection<Class<? extends T>> getApis(@NonNull Class<T> requiredType) {
        List<Map.Entry<Class<? extends T>, ApiImpls.NameVersion>> entryList = filterApiImpls(requiredType);
        List<Class<? extends T>> classes = new ArrayList<>();
        for (Map.Entry<Class<? extends T>, ApiImpls.NameVersion> entry : entryList
        ) {
            classes.add(entry.getKey());
        }
        return Collections.unmodifiableCollection(classes);
    }
    @Override
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
    @Override
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
    @Override
    public <T> Class<? extends T> getApi(Class<T> requiredType) {
        return getApi(requiredType, null, 0, false);
    }
    @Override
    public <T> Class<? extends T> getApi(Class<T> requiredType, long version) {
        return getApi(requiredType, null, version, false);
    }
    @Override
    public <T> Class<? extends T> getApi(Class<T> requiredType, String name, long version, boolean useRegex) {
        return findApiImpl(requiredType, name, version, useRegex);
    }
    @Override
    public <T> void setImpl(Class<T> requiredType, String name, long version, T entity) {
        String key = convertKey(requiredType, name, version);
        synchronized (mSingletonBeans) {
            if (!mSingletonBeans.containsKey(key)) {
                mSingletonBeans.put(key, entity);
            }
        }
    }
    @Override
    public <T> T getImpl(Class<T> requiredType) {
        return getImpl(requiredType, null, 0, false);
    }
    @Override
    public <T> T getImpl(Class<T> requiredType, long version) {
        return getImpl(requiredType, null, version, false);
    }
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
    public Object cacheRemove(@NonNull String key) {
        synchronized (mCaches) {
            return mCaches.remove(key);
        }
    }

    /*预设api 是否能够获得*/
    @Override
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
    @Override
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
    public ApiContext setUIThreadHandler(UIThreadHandler uiThreadHandler) {
        this.mUIThreadHandler = uiThreadHandler;
        return this;
    }
    @Override
    @NonNull
    public ApiContext setIOThreadHandler(IOThreadHandler ioThreadHandler) {
        this.mIOThreadHandler = ioThreadHandler;
        return this;
    }
    @Override
    @NonNull
    public ApiContext setExceptionHandler(ExceptionHandler exceptionHandler) {
        this.mExceptionHandler = exceptionHandler;
        return this;
    }
    @Override
    public boolean post(@NonNull Runnable action) {
        return mUIThreadHandler != null && mUIThreadHandler.post(action);
    }
    @Override
    public boolean postDelayed(@NonNull Runnable action, long delayMillis) {
        return mUIThreadHandler != null && mUIThreadHandler.postDelayed(action, delayMillis);
    }
    @Override
    public void execute(@NonNull Runnable command) {
        if (mIOThreadHandler != null) {
            mIOThreadHandler.execute(command);
        }
    }
    @Override
    public Future<?> submit(@NonNull Runnable task) {
        return mIOThreadHandler != null ? mIOThreadHandler.submit(task) : null;
    }
    @Override
    public void report(@NonNull String tag, Throwable throwable) {
        if (mExceptionHandler != null) {
            mExceptionHandler.report(tag, throwable);
        }
    }
}
