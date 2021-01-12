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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

/**
 * Load All ApiImpls implements from META DATA it's name start with "com.atom."
 * <p>
 * Created by HYW on 2017/11/10.
 */
@SuppressWarnings("WeakerAccess")
public abstract class AbstractApiImplContext implements ApiImplContext {

    private static final String TAG = "AbstractApiImplContext";
    /**
     * Map of cached data
     */
    private final Map<String, WeakReference<Object>> mCaches = new HashMap<>();
    /**
     * Map of singleton beans, keyed by name
     */
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

    public static List<Class<?>> getClasses(String packageName) {
        // 第一个class类的集合
        List<Class<?>> classes = new ArrayList<Class<?>>();
        // 获取包的名字 并进行替换
        String packageDirName = packageName.replace('.', '/');
        // 定义一个枚举的集合 并进行循环来处理这个目录下的things
        Enumeration<URL> dirs;
        try {
            dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
            // 循环迭代下去
            while (dirs.hasMoreElements()) {
                URL url = dirs.nextElement();
                // 得到协议的名称
                String protocol = url.getProtocol();
                if ("file".equals(protocol)) {
                    // 获取包的物理路径
                    String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                    // 以文件的方式扫描整个包下的文件 并添加到集合中
                    classes.addAll(findClassByDirectory(packageName, filePath));
                } else if ("jar".equals(protocol)) {
                    classes.addAll(findClassInJar(packageName, url));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return classes;
    }

    public static List<Class<?>> findClassByDirectory(String packageName, String packagePath) {
        // 获取此包的目录 建立一个File
        File dir = new File(packagePath);
        if (!dir.exists() || !dir.isDirectory()) {
            return new ArrayList<>(0);
        }
        File[] dirs = dir.listFiles();
        List<Class<?>> classes = new ArrayList<>();
        if (dirs == null) return classes;
        // 循环所有文件
        for (File file : dirs) {
            // 如果是目录 则继续扫描
            if (file.isDirectory()) {
                classes.addAll(findClassByDirectory(packageName + "." + file.getName(),
                        file.getAbsolutePath()));
            } else if (file.getName().endsWith(".class")) {
                // 如果是java类文件，去掉后面的.class 只留下类名
                String className = file.getName().substring(0, file.getName().length() - 6);
                try {
                    Class<?> aClass = Class.forName(packageName + '.' + className);
                    if (ApiImpls.class.isAssignableFrom(aClass)) {
                        classes.add(aClass);
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }

        return classes;
    }

    public static List<Class<?>> findClassInJar(String packageName, URL url) {

        List<Class<?>> classes = new ArrayList<Class<?>>();
        String packageDirName = packageName.replace('.', '/');
        // 定义一个JarFile
        JarFile jar;
        try {
            // 获取jar
            jar = ((JarURLConnection) url.openConnection()).getJarFile();
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                // 获取jar里的一个实体 可以是目录 和一些jar包里的其他文件 如META-INF等文件
                JarEntry entry = entries.nextElement();
                if (entry.isDirectory()) {
                    continue;
                }
                String name = entry.getName();
                if (name.charAt(0) == '/') {
                    // 获取后面的字符串
                    name = name.substring(1);
                }

                // 如果前半部分和定义的包名相同
                if (name.startsWith(packageDirName) && name.endsWith(".class")) {
                    // 去掉后面的".class"
                    String className = name.substring(0, name.length() - 6).replace('/', '.');
                    try {
                        // 添加到classes
                        Class<?> aClass = Class.forName(className);
                        if (ApiImpls.class.isAssignableFrom(aClass)) {
                            classes.add(aClass);
                        }
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return classes;
    }

    @SuppressWarnings("WeakerAccess")
    protected void loadPackages() {
        List<Class<?>> classes = getClasses("com.atom.apt");
        for (Class<?> clazz: classes
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

    @SuppressWarnings("unchecked")
    public <T> T getApi(Class<T> requiredType) {
        T api = getApi("", requiredType, false);
        if (api != null) {
            return api;
        }
        api = getApi(requiredType.getSimpleName(), requiredType, false);
        if (api != null) {
            return api;
        }
        String key = convertKey("", requiredType);
        synchronized (mSingletonBeans) {
            for (Object obj : mSingletonBeans.values()) {
                if (requiredType.isInstance(obj)) {
                    api = (T) obj;
                    mSingletonBeans.put(key, api);
                    return api;
                }
            }
            api = newApi(requiredType);
            //noinspection ConstantConditions
            if (api != null) {
                mSingletonBeans.put(key, api);
                Impl annotation = getAnnotation(api.getClass(), Impl.class);
                if (annotation != null && !annotation.name().isEmpty()) {
                    key = convertKey(annotation.name(), requiredType);
                    mSingletonBeans.put(key, api);
                }
            }
        }
        return api;
    }

    public <T> Collection<Class<? extends T>> getApiImpls(Class<T> requiredType) {
        List<Class<? extends T>> apiImpls = new LinkedList<>();
        Collection<Class<? extends T>> impls;
        Impl impl;
        String name;
        Boolean enabled;
        for (ApiImpls imp : mApiImpls) {
            impls = imp.getApiImpls(requiredType);
            if (impls != null) {
                for (Class<? extends T> cls : impls) {
                    impl = cls.getAnnotation(Impl.class);
                    if (impl != null) {
                        name = impl.name();
                        if (!name.isEmpty()) {
                            enabled = isImplEnabled(name);
                            if (enabled != null && !enabled) {
                                continue;
                            }
                        }
                    }
                    apiImpls.add(cls);
                }
            }
        }
        return apiImpls;
    }

    @SuppressWarnings("WeakerAccess")
    protected String convertKey(String name, Class<?> type) {
        String key = type.getSimpleName();
        if (name.isEmpty() || key.equalsIgnoreCase(name)) {
            return key;
        }
        return key + "#" + name;
    }

    @Override
    public <T> Class<? extends T> getApiImpl(String name, Class<T> api) {
        return findApiImpl(name, api, true);
    }

    @Override
    public <T> T getApi(String name, Class<T> requiredType) {
        return getApi(name, requiredType, true);
    }

    @SuppressWarnings("unchecked")
    protected <T> T getApi(String name, Class<T> requiredType, boolean useRegex) {
        if (name == null) {
            name = "";
        }
        String key = convertKey(name, requiredType);
        synchronized (mSingletonBeans) {
            if (mSingletonBeans.containsKey(key)) {
                return (T) mSingletonBeans.get(key);
            }
            Class<? extends T> impl = findApiImpl(name, requiredType, useRegex);
            if (impl == null) {
                if (!Modifier.isAbstract(requiredType.getModifiers())) {
                    impl = requiredType;
                }
            }
            T api = createApi(name, impl);
            mSingletonBeans.put(key, api);
            return api;
        }
    }

    @Override
    public <T> T hasApi(Class<T> requiredType) {
        T api = hasApi("", requiredType);
        if (api != null) {
            return api;
        }
        api = hasApi(requiredType.getSimpleName(), requiredType);
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
    public <T> T hasApi(String name, Class<T> requiredType) {
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
            impl = findApiImpl(null, api, false);
        } else {
            impl = api;
        }
        return createApi(null, impl);
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
        if (isDebugEnabled()) {
            if (mExecutorService instanceof ThreadPoolExecutor) {
                ThreadPoolExecutor executor = (ThreadPoolExecutor) mExecutorService;
                long active = executor.getActiveCount();
                long completed = executor.getCompletedTaskCount();
                long total = executor.getTaskCount();
                long largestPoolSize = executor.getLargestPoolSize();
                long queued = total - active - completed;
                debug(Thread.currentThread().getStackTrace()[3], "ExecutorService " + "TaskCount: " + total + " ActiveCount: " + active
                        + ", CompletedTaskCount: " + completed + ", QueuedCount: " + queued + ", LargestPoolSize: " + largestPoolSize
                );
            }
        }
    }

    @Override
    public void execute(Runnable command, long priority) {
        execute(command);
    }

    @Override
    public void requestPermissions(String... permissions) {
        // TODO 请求权限操作
    }


    /**
     * Start Service for background work.
     */
    public void startWorkService() {
        //TODO 启动一个工作server 可以启动一个保活守护server
    }

    @SuppressWarnings("WeakerAccess")
    protected <T> Class<? extends T> findApiImpl(String name, Class<T> requiredType, boolean useRegex) {
        Collection<Class<? extends T>> imps = getApiImpls(requiredType);
        Class<? extends T> impl = findApiImpl(name, imps, useRegex);
        if (impl != null) {
            return impl;
        }
        if (name != null) {
            return null;
        }
        impl = findApiImpl(requiredType.getSimpleName(), imps, false);
        if (impl != null) {
            return impl;
        }
        if (imps.size() > 0) {
            return imps.iterator().next();
        }
        return null;
    }

    @SuppressWarnings("WeakerAccess")
    protected <T> Class<? extends T> findApiImpl(String name, Collection<Class<? extends T>> imps, boolean useRegex) {
        if (name == null) {
            name = "";
        }
        if (name.isEmpty()) {
            useRegex = false;
        }
        Class<? extends T> impRegex = null;
        Impl api;
        String pattern;
        for (Class<? extends T> imp : imps) {
            api = getAnnotation(imp, Impl.class);
            if (api == null) {
                continue;
            }
            if (name.equals(api.name())) {
                return imp;
            }

            if (useRegex && impRegex == null) {
                pattern = api.name();
                if (!pattern.isEmpty() && Pattern.matches(pattern, name)) {
                    impRegex = imp;
                }
            }
        }
        return impRegex;
    }

    /**
     * @param name api impl name
     * @param impl api impl class
     * @param <T>  T
     * @return object of T
     */
    @SuppressWarnings("WeakerAccess")
    @Nullable
    protected <T> T createApi(String name, Class<? extends T> impl) {
        if (impl == null) {
            return null;
        }
        T obj = null;
        try {
            obj = impl.newInstance();
        } catch (Exception ex) {
            reportException(TAG, ex);
        }
        if (obj instanceof ApiImplContextAware) {
            ((ApiImplContextAware) obj).setApiImplContext(this);
        }
        return obj;
    }

    @SuppressWarnings({"WeakerAccess", "unchecked"})
    public <A extends Annotation> A getAnnotation(Class<?> cls, Class<A> annotationClass) {
        A a = cls.getAnnotation(annotationClass);
        if (a != null) {
            return a;
        }
        String annotationClassname = annotationClass.getName();
        for (Annotation annotation : cls.getAnnotations()) {
            if (annotationClassname.equals(annotation.getClass().getName())) {
                a = (A) annotation;
                return a;
            }
        }
        return null;
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
