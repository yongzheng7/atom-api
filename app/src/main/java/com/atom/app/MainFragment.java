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

import com.atom.api.app.Hello;
import com.atom.app.base.AbstractFragment;

import java.util.Collection;
import java.util.Objects;

@Impl(api = AbstractFragment.class)
public class MainFragment extends AbstractFragment {

    public MainFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.activity_fragment, container, false);
        initView(inflate);
        return inflate;
    }

    void initView(View layout) {
        TextView viewById = layout.findViewById(R.id.hello);
        viewById.setOnClickListener(v -> new AlertDialog.Builder(getActivity())
                .setTitle("Atom Api")
                .setMessage("Api框架： \n1：实现通过Api接口获取实现的类 \n2：实现根据名字，正则配合版本动态获取类 \n3：实现配置实现类的开关")
                .setPositiveButton("关闭", (dialog, which) -> dialog.dismiss()).create().show());

        layout.findViewById(R.id.test_class_api_list).setOnClickListener(v -> {
            Collection<Class<? extends Hello>> apiImpls = apiImplContext().getApiImpls(Hello.class);
            Log.e(getTag(), "实现 Hello interface 的子类 ----[" + apiImpls.size() + "] \n");
            for (Class<? extends Hello> clazz : apiImpls
            ) {
                Log.e(getTag(), Objects.requireNonNull(clazz.getCanonicalName()));
            }
            Log.e(getTag(), "------------------------------- \n");
        });

        layout.findViewById(R.id.test_class_api_name).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(getTag(), "实现 Hello interface 的子类 ----start name = null version = -1 \n");
                final String name = "waswaswasz";
                Class<? extends Hello> apiImplByName = apiImplContext().getApiImplByName(Hello.class, name, 5);
                if (apiImplByName == null) {
                    Log.e(getTag(), " Hello interface impl class name = "+name+" is null");
                } else {
                    Log.e(getTag(), Objects.requireNonNull(Objects.requireNonNull(apiImplByName).getCanonicalName()));
                }

                Log.e(getTag(), "------------------------------- \n");
            }
        });
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

    }
}
