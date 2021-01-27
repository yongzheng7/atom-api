package com.atom.api;

import java.util.concurrent.Future;

public interface IOThreadHandler {

    void execute(Runnable command);

    Future<?> submit(Runnable task);
}
