package com.atom.api;

public interface ExceptionHandler {
    void report(String tag, Throwable throwable);
}
