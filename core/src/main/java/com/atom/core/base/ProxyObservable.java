package com.atom.core.base;

import com.atom.api.core.IObservable;
import com.atom.api.core.IObserver;

import java.util.Collection;

public class ProxyObservable<T, O extends IObserver<T>> implements IObservable<T, O> {

    private IObservable<T, O> observable;

    @Override
    public void setProxy(IObservable<T, O> observable) {
        this.observable = observable;
    }

    @Override
    public void addObserver(O observer) {
        observable.addObserver(observer);
    }

    @Override
    public Boolean removeObserver(O observer) {
        return observable.removeObserver(observer);
    }

    @Override
    public void clearObserver() {
        observable.clearObserver();
    }

    @Override
    public Boolean containsObserver(O observer) {
        return observable.containsObserver(observer);
    }

    @Override
    public Collection<O> getObservers() {
        return observable.getObservers();
    }

    @Override
    public void notify(T t) {
        observable.notify(t);
    }
}
