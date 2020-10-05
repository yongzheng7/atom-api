package com.atom.core.base;

import androidx.lifecycle.Observer;

import com.atom.api.ApiImplContext;
import com.atom.api.ApiImplContextAware;
import com.atom.api.core.IObservable;
import com.atom.api.core.IObserver;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;


public class ObservableManager implements ApiImplContextAware {

    private final Map<Class<?>, IObservable<?, ?>> observableMap = new HashMap<>();

    private ApiImplContext context;

    @Override
    public void setApiImplContext(ApiImplContext context) {
        this.context = context;
    }

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
}
