package com.atom.app;

import android.util.Log;

import com.atom.annotation.Impl;
import com.atom.api.app.Hello;


@Impl(api = Hello.class , name = "waswaswas" , version = 1)
public class HelloWorld implements Hello{
    String filed = "asdasd";
    String name = "asdasd";

    @Override
    public void hello() {
        Log.e("loadPackages" ,"HelloWorld >"+filed + name) ;
    }
}
