package com.atom.core.base;


import com.atom.api.ApiImplContext;
import com.atom.api.ApiImplContextAware;
import com.atom.api.app.Hello;
import com.atom.api.core.IObservable;
import com.atom.api.core.IObserver;
import com.atom.apt.annotation.Impl;

import java.util.HashMap;
import java.util.Map;

@Impl(api = ObservableManager.class)
public class ObservableManagerImpl implements ApiImplContextAware , ObservableManager {

    private final Map<Class<?>, IObservable<?, ?>> observableMap = new HashMap<>();

    private ApiImplContext context;

    @Override
    public void setApiImplContext(ApiImplContext context) {
        this.context = context;
    }

    @Override
    public <T, O extends IObserver<T>> void registerObserver(Class<T> key, O observer) {
        synchronized (observableMap) {
            IObservable<T, O> iObservableFinal;
            IObservable<?, ?> iObservable = observableMap.get(key);
            if (iObservable == null) {
                iObservableFinal = new Observable<>();
                observableMap.put(key, iObservableFinal);
            } else {
                iObservableFinal = (IObservable<T, O>) iObservable;
            }
            iObservableFinal.addObserver(observer);
        }
    }
    @Override
    public <T, O extends IObserver<T>> void unregisterObserver(Class<T> key, O observer) {
        synchronized (observableMap) {
            IObservable<T, O> iObservableFinal;
            IObservable<?, ?> iObservable = observableMap.get(key);
            if (iObservable != null) {
                iObservableFinal = (IObservable<T, O>) iObservable;
                iObservableFinal.removeObserver(observer);
            }
        }
    }
    @Override
    public <T, O extends IObserver<T>> void proxyObservable(Class<T> key, ProxyObservable<T, O> observable) {
        synchronized (observableMap) {
            IObservable<?, ?> iObservable = observableMap.get(key);
            if (iObservable != null) {
                ((IObservable<T, O>) iObservable).setProxy(observable);
            }
        }
    }
}
