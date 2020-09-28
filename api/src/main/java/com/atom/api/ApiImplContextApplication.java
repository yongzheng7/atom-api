package com.atom.api;


import androidx.annotation.NonNull;

public interface ApiImplContextApplication {

    /**
     * Get the api implemnet context.
     *
     * @return context api implemnet context
     */
    @NonNull
    ApiImplContext getApiImplContext();
}
