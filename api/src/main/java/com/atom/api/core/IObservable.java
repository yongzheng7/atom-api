package com.atom.api.core;

import java.util.Collection;

public interface IObservable<T> {

    void addObserver(IObserver<T> observer);

    Boolean removeObserver(IObserver<T> observer);

    void clearObserver();

    Boolean containsObserver(IObserver<T> observer) ;

    Collection<IObserver<T>> getObservers() ;

    void notify(T t);
}
