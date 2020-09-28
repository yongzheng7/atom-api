package com.atom.api.core;

public interface IObservable<T> {

    void addObserver(IObserver<T> observer);

    Boolean removeObserver(IObserver<T> observer);

    void clearObserver();

    void notify(T t);
}
