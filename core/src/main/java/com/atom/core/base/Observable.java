package com.atom.core.base;

import com.atom.api.core.IObservable;
import com.atom.api.core.IObserver;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Observable<T, O extends IObserver<T>> implements IObservable<T, O> {

    private final List<O> observers;

    public Observable(List<O> observers) {
        this.observers = observers;
    }

    public Observable() {
        this(new LinkedList<O>());
    }

    @Override
    public void setProxy(IObservable<T, O> observable) {
        observable.setProxy(this);
    }

    @Override
    public void addObserver(O observer) {
        synchronized (observers) {
            observers.add(observer);
        }
    }

    @Override
    public Boolean removeObserver(O observer) {
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
    public Boolean containsObserver(O observer) {
        synchronized (observers) {
            return observers.contains(observer);
        }
    }

    @Override
    public Collection<O> getObservers() {
        synchronized (observers) {
            return Collections.unmodifiableList(observers);
        }
    }

    @Override
    public void notify(T t) {
        synchronized (observers) {
            for (O observer : observers) {
                observer.run(t);
            }
        }
    }
}
