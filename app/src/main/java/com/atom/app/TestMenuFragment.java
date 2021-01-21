package com.atom.app;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.atom.annotation.Impl;

import com.atom.app.base.AbstractFragment;


@Impl(api = AbstractFragment.class, name = "main/menu")
public class TestMenuFragment extends AbstractFragment {

    public TestMenuFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_main_menu, container, false);
        initView(inflate);
        return inflate;
    }

    void initView(View layout) {
        layout.findViewById(R.id.hello).setOnClickListener(v -> new AlertDialog.Builder(getActivity())
                .setTitle("Atom Api")
                .setMessage("Api框架： \n1：实现通过Api接口获取实现的类 \n2：实现根据名字，正则配合版本动态获取类 \n3：实现配置实现类的开关")
                .setPositiveButton("关闭", (dialog, which) -> dialog.dismiss()).create().show());
        layout.findViewById(R.id.test_api).setOnClickListener(v -> {
            AbstractFragment apiByName = apiImplContext().getImpl(AbstractFragment.class, "main/menu/api" , 0 , false);
            if(apiByName != null){
                loadFragment(apiByName , true);
            }
        });
        layout.findViewById(R.id.test_impl).setOnClickListener(v -> {
            AbstractFragment apiByName = apiImplContext().getImpl(AbstractFragment.class, "main/menu/impl", 0 , false);
            if(apiByName != null){
                loadFragment(apiByName , true);
            }
        });
        layout.findViewById(R.id.test_cache).setOnClickListener(v -> {
            AbstractFragment apiByName = apiImplContext().getImpl(AbstractFragment.class, "main/menu/cache", 0 , false);
            if(apiByName != null){
                loadFragment(apiByName , true);
            }
        });
        layout.findViewById(R.id.test_other).setOnClickListener(v -> {
            AbstractFragment apiByName = apiImplContext().getImpl(AbstractFragment.class, "main/menu/other", 0 , false);
            if(apiByName != null){
                loadFragment(apiByName , true);
            }
        });
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

    }
}
