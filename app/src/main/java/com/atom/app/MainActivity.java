package com.atom.app;


import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.atom.annotation.Impl;
import com.atom.api.app.Hello;
import com.atom.app.base.AbstractActivity;
import com.atom.runtime.AtomApi;

@Impl(api = Activity.class)
public class MainActivity extends AbstractActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        View viewById = findViewById(getFrameLayout());
        viewById.setOnClickListener(view -> {
            AtomApi atomApi = apiImplContext();
            Hello api = atomApi.getApi(Hello.class);
            if (api != null) {
                api.hello();
            }
            Toast.makeText(this, "mainActivity", Toast.LENGTH_SHORT).show();
            loadFragment(new MainFragment() , true);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadFragment(new MainFragment() , true);
    }

    @Override
    protected int getFrameLayout() {
        return R.id.widget_frame;
    }
}