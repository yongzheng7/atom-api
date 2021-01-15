package com.atom.data;

import android.util.Log;

import com.atom.annotation.Impl;
import com.atom.api.app.Hello;

@Impl(api = Hello.class, name = "HelloWorld4" , version = 2)
public class HelloWorld4 implements Hello{
    String filed = "asdasd";
    String name = "asdasd";

    @Override
    public void hello() {
        Log.e("loadPackages" ,"HelloWorld >"+filed + name) ;
    }
}
