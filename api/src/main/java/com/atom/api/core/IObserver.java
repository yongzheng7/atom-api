package com.atom.api.core;

public interface IObserver<T> {
    void run(T t);
}
