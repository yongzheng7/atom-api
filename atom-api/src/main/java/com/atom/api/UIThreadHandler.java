package com.atom.api;

public interface UIThreadHandler {

    boolean post(Runnable action);

    boolean postDelayed(Runnable action, long delayMillis);
}
