package com.atom.api;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collection;
import java.util.concurrent.RejectedExecutionException;

/**
 * Created by HYW on 2017/11/9.
 * <pre>
 * Get the singleton ApiImplContext object.
 *
 * param context context
 * return object of ApiImplContext, else null.
 *
 * public static ApiImplContext app(Context context) {
 *   if (context instanceof ApiImplContextApplication) {
 *     return ((ApiImplContextApplication) context).getApiImplContext();
 *   }
 *   context = context.getApplicationContext();
 *   if (context instanceof ApiImplContextApplication) {
 *     return  ((ApiImplContextApplication) context).getApiImplContext();
 *   }
 *   return null;
 * }
 * </pre>
 */

public interface ApiImplContext {

    /**
     * Get Api Implements class.
     *
     * @param requiredType Api class
     * @param <T>          Application Programming Interface
     * @return Api Implements class
     */
    <T> Collection<Class<? extends T>> getApiImpls(Class<T> requiredType);

    /**
     * Get Api Implements class, by name.
     *
     * @param name         Api Implements name.
     * @param requiredType Api class
     * @param <T>          Application Programming Interface
     * @return Api Implements class or null, if has more than one implements return the first one.
     */
    <T> Class<? extends T> getApiImpl(String name, Class<T> requiredType);

    /**
     * Get or Create Api Implements default object.
     *
     * @param requiredType Api class
     * @param <T>          Application Programming Interface
     * @return Api Implements default object or null
     */
    <T> T getApi(Class<T> requiredType);

    /**
     * Get or Create Api Implements object.
     *
     * @param <T>          Application Programming Interface
     * @param name         Api object name.
     * @param requiredType Api class
     * @return Api Implements object or null
     */
    <T> T getApi(String name, Class<T> requiredType);

    /**
     * Get Api exist implements default object, it not create new one.
     *
     * @param requiredType Api class
     * @param <T>          Application Programming Interface
     * @return Api Implements default object or null
     */
    <T> T hasApi(Class<T> requiredType);

    /**
     * Get Api exist implements object, it not create new one.
     *
     * @param <T>          Application Programming Interface
     * @param name         Api object name.
     * @param requiredType Api class
     * @return Api Implements object or null
     */
    <T> T hasApi(String name, Class<T> requiredType);

    /**
     * Get Api implements by create new object.
     *
     * @param requiredType Api class or Api Implements class
     * @param <T>          Application Programming Interface
     * @return Api Implements new object or null
     */
    <T> T newApi(Class<T> requiredType);

    /**
     * Get the Application Context that this object runs in.
     *
     * @return Context of Application.
     */
    Context getAppContext();

    /**
     * Get the string value associated with a particular resource id.
     *
     * @param id The desired resource identifier
     * @return String The string data associated with the resource
     */
    String getString(int id);

    /**
     * Get the bitmap value associated with a particular resource id.
     *
     * @param id The desired resource identifier
     * @return Bitmap The bitmap data associated with the resource
     */
    Bitmap getBitmap(int id);

    /**
     * Get the bitmap value associated with a particular assets path.
     *
     * @param path The desired resource assets path
     * @return Bitmap The bitmap data associated with the resource
     */
    Bitmap decodeAssets(String path);

    /**
     * Cache a data
     *
     * @param data data
     * @return key of data
     */
    @NonNull
    String cachePut(@NonNull Object data);

    /**
     * Get cached data
     *
     * @param key key of cached data
     * @return cached data
     */
    @Nullable
    Object cacheGet(@NonNull String key);

    /**
     * Remove cached data
     *
     * @param key key of cached data
     * @return cached data
     */
    @Nullable
    Object cacheRemove(@NonNull String key);

    /**
     * Enable or disable implements by name
     *
     * @param name   name  of implements
     * @param enable true enable, false disable, null default
     */
    void enableImpl(@NonNull String name, Boolean enable);

    /**
     * Return the implements is enabled or not
     *
     * @param name name of implements
     * @return true or false or null
     */
    @Nullable
    Boolean isImplEnabled(@NonNull String name);

    /**
     * Is run debug mode.
     *
     * @return true if run debug mode, else false.
     */
    boolean isDebugEnabled();

    /**
     * print msg to logcat
     *
     * @param msg message
     */
    void debug(Object msg);

    /**
     * print msg to logcat
     *
     * @param stack StackTraceElement
     * @param msg   message
     */
    void debug(StackTraceElement stack, Object msg);

    /**
     * Enable disable debug mode.
     *
     * @param enable true enable debug mode, false disable debug mode.
     */
    void enableDebug(boolean enable);

    /**
     * Report Exception.
     *
     * @param tag       tag of exception.
     * @param throwable exception to report.
     */
    void reportException(String tag, Throwable throwable);

    /**
     * Get Main Fragment Activity Class
     *
     * @return class
     */
    Class<?> getFragmentActivityClass();

    /**
     * Executes the given command at some time in the future.  The command  may execute in a new thread,
     * in a pooled thread, or in the calling thread, at the discretion of the {@code Executor} implementation.
     *
     * @param command the runnable task
     * @throws RejectedExecutionException if this task cannot be accepted for execution
     * @throws NullPointerException       if command is null
     */
    void execute(Runnable command);

    /**
     * Executes the given command at some time in the future.  The command  may execute in a new thread,
     * in a pooled thread, or in the calling thread, at the discretion of the {@code Executor} implementation.
     *
     * @param command  the runnable task
     * @param priority the runnable task's priority, use timestamp (in milliseconds), The smaller the value, the higher the priority
     * @throws RejectedExecutionException if this task cannot be accepted for execution
     * @throws NullPointerException       if command is null
     */
    void execute(Runnable command, long priority);

    /**
     * Causes the Runnable r to be added to the message queue. The runnable will be run on the application main thread.
     *
     * @param command The Runnable that will be executed.
     * @return Returns true if the Runnable was successfully placed in to the message queue.  Returns false on failure.
     * usually because the looper processing the message queue is exiting.
     */
    boolean post(Runnable command);

    /**
     * Causes the Runnable r to be added to the message queue, to be run after the specified amount of time elapses.
     * The runnable will be run on the application main thread.
     *
     * <b>The time-base is {@link android.os.SystemClock#uptimeMillis}.</b>
     * Time spent in deep sleep will add an additional delay to execution.
     *
     * @param command     The Runnable that will be executed.
     * @param delayMillis The delay (in milliseconds) until the Runnable will be executed.
     * @return Returns true if the Runnable was successfully placed in to the message queue.
     * Returns false on failure, usually because the looper processing the message queue is exiting.
     * Note that a result of true does not mean the Runnable will be processed -- if the looper is quit before the delivery time of the message
     * occurs then the message will be dropped.
     */
    boolean postDelayed(Runnable command, long delayMillis);

    /**
     * @param permissions request permissions
     */
    void requestPermissions(String... permissions);

    /**
     * Start Work Service
     */
    void startWorkService();
}
