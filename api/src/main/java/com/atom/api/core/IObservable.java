package com.atom.api.core;

import java.util.Collection;

public interface IObservable<T, O> {

    void setProxy(IObservable<T, O> observable);

    void addObserver(O observer);

    Boolean removeObserver(O observer);

    void clearObserver();

    Boolean containsObserver(O observer);

    Collection<O> getObservers();

    void notify(T t);
}
