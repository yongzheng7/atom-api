package com.atom.api;

import java.util.Collection;


public interface ApiBundle {

    <T> Collection<Class<? extends T>> getApiImpls(Class<T> api);

}
