package com.atom.app;

import android.util.Log;

import com.atom.api.app.Hello;
import com.atom.apt.annotation.Impl;

@Impl(api = Hello.class)
public class HelloWorld2 implements Hello{
    String filed = "asdasd";
    String name = "asdasd";

    @Override
    public void hello() {
        Log.e("loadPackages" ,"HelloWorld >"+filed + name) ;
    }
}
