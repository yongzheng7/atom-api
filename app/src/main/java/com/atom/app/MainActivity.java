package com.atom.app;


import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.atom.api.ApiImplContext;
import com.atom.api.app.Hello;
import com.atom.core.ui.AbstractActivity;

public class MainActivity extends AbstractActivity {

    @Override
    protected String getAppPackageName() {
        return "com.atom.app";
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onclickTest(View view) {
        Log.e("ObservableManager" , "onclickTest") ;
        apiImplContext().post(() -> {
            ApiImplContext apiImplContext = apiImplContext();
            Hello api = apiImplContext.getApi(Hello.class);
            if(api != null){
                Log.e("ObservableManager" , "onclickTest 1") ;
                api.hello();
            }
//            ObservableManager api1 = apiImplContext.getApi(ObservableManager.class);
//            Log.e("ObservableManager" , api1 == null ? " null " : "api != null") ;
        });
    }
}