package com.atom.core.base;

import com.atom.api.core.IObservable;
import com.atom.api.core.IObserver;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class SimpleObservable<T> implements IObservable<T, IObserver<T>> {

    private final List<IObserver<T>> observers;

    public SimpleObservable(List<IObserver<T>> observers) {
        this.observers = observers;
    }

    public SimpleObservable() {
        this(new LinkedList<IObserver<T>>());
    }

    @Override
    public void setProxy(IObservable<T, IObserver<T>> observable) {
        observable.setProxy(this);
    }

    @Override
    public void addObserver(IObserver<T> observer) {
        synchronized (observers) {
            observers.add(observer);
        }
    }

    @Override
    public Boolean removeObserver(IObserver<T> observer) {
        synchronized (observers) {
            return observers.remove(observer);
        }
    }

    @Override
    public void clearObserver() {
        synchronized (observers) {
            observers.clear();
        }
    }

    @Override
    public Boolean containsObserver(IObserver<T> observer) {
        synchronized (observers) {
            return observers.contains(observer);
        }
    }

    @Override
    public Collection<IObserver<T>> getObservers() {
        synchronized (observers) {
            return Collections.unmodifiableList(observers);
        }
    }

    @Override
    public void notify(T t) {
        synchronized (observers) {
            for (IObserver<T> observer : observers) {
                observer.run(t);
            }
        }
    }
}
