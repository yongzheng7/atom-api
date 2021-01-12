package com.atom.app;


import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.atom.annotation.Impl;
import com.atom.api.ApiImplContext;
import com.atom.api.app.Hello;
import com.atom.core.base.ObservableManager;
import com.atom.core.ui.AbstractActivity;

@Impl(api = Activity.class)
public class MainActivity extends AbstractActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        View viewById = findViewById(getFrameLayout());
        viewById.setOnClickListener(view -> apiImplContext().post(() -> {
            ApiImplContext apiImplContext = apiImplContext();
            Hello api = apiImplContext.getApi(Hello.class);
            if (api != null) {
                api.hello();
            }
            ObservableManager api1 = apiImplContext.getApi(ObservableManager.class);
            loadFragment(new MainFragment(), true);
            Toast.makeText(this, "mainActivity", Toast.LENGTH_SHORT).show();
        }));

    }

    @Override
    protected int getFrameLayout() {
        return R.id.widget_frame;
    }
}