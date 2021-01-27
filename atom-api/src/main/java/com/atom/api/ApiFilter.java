package com.atom.api;

import com.atom.annotation.bean.ApiImpls;

public interface ApiFilter<T> {
    boolean accept(Class<? extends T> clazz, ApiImpls.NameVersion param);
}
