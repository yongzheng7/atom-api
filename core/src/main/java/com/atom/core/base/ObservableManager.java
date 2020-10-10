package com.atom.core.base;

import com.atom.api.core.IObserver;

public interface ObservableManager {

    <T, O extends IObserver<T>> void proxyObservable(Class<T> key, ProxyObservable<T, O> observable);

    <T, O extends IObserver<T>> void registerObserver(Class<T> key, O observer);

    <T, O extends IObserver<T>> void unregisterObserver(Class<T> key, O observer);
}
