package com.atom.app;


import android.app.Activity;
import android.os.Bundle;

import com.atom.annotation.Impl;
import com.atom.app.base.AbstractActivity;
import com.atom.app.base.AbstractFragment;

@Impl(api = Activity.class)
public class MainActivity extends AbstractActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();
        AbstractFragment apiByName = apiImplContext().getImpl(AbstractFragment.class, "main/menu/api" , 0 , false);
        loadFragment(apiByName , true);
    }

    @Override
    protected int getFrameLayout() {
        return R.id.widget_frame;
    }
}