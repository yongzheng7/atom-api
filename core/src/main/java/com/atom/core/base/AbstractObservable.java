package com.atom.core.base;

import com.atom.api.core.IObservable;
import com.atom.api.core.IObserver;

import java.util.Collection;

public class AbstractObservable<T> implements IObservable<T> {

    private final Observable<T> observable;

    public AbstractObservable() {
        this(new Observable<T>());
    }

    public AbstractObservable(Observable<T> observable) {
        this.observable = observable;
    }

    @Override
    public void addObserver(IObserver<T> observer) {
        observable.addObserver(observer);
    }

    @Override
    public Boolean removeObserver(IObserver<T> observer) {
        return observable.removeObserver(observer);
    }

    @Override
    public void clearObserver() {
        observable.clearObserver();
    }

    @Override
    public Boolean containsObserver(IObserver<T> observer) {
        return observable.containsObserver(observer);
    }

    @Override
    public Collection<IObserver<T>> getObservers() {
        return observable.getObservers();
    }

    @Override
    public void notify(T t) {
        observable.notify(t);
    }
}
