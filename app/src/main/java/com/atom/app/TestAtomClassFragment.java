package com.atom.app;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.atom.annotation.Impl;
import com.atom.annotation.bean.ApiImpls;
import com.atom.api.app.Hello;
import com.atom.app.base.AbstractFragment;
import com.atom.core.AtomApi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Impl(api = AbstractFragment.class, name = "main/menu/api")
public class TestAtomClassFragment extends AbstractFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_test_api, container, false);
        initView(inflate);
        return inflate;
    }

    void initView(View layout) {
        final TextView print = layout.findViewById(R.id.print_view);
        /**
         * 进行根据api获取所有模块实现该api的class
         */
        layout.findViewById(R.id.test_class_api_list).setOnClickListener(v -> {
            Collection<Class<? extends Hello>> apiImpls = apiImplContext().getApis(Hello.class);
            StringBuilder builder = new StringBuilder('\n');
            builder.append("implClass / name / version").append('\n');
            for (Class<? extends Hello> item : apiImpls
            ) {
                Impl annotation = item.getAnnotation(Impl.class);
                if (annotation == null) continue;
                builder.append(item.getCanonicalName())
                        .append(" / ")
                        .append(annotation.name())
                        .append(" / ")
                        .append(annotation.version())
                        .append('\n');
            }
            String s = builder.toString();
            Log.e("TestAtomClassFragment", s);
            print.setText(s);
        });
        /**
         * 进行根据api 以及 name 精准获取 class 或者根据 正则表达式 模糊定位
         */
        layout.findViewById(R.id.test_class_api_name).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 精确查找
                //Collection<Class<? extends Hello>> hello2 = apiImplContext().getApis(Hello.class, "hello*", true);
                // 正则查找
                Collection<Class<? extends Hello>> hello2 = apiImplContext().getApis(Hello.class, "hello(.*)", true);
                StringBuilder builder = new StringBuilder('\n');
                builder.append("implClass / name / version").append('\n');
                for (Class<? extends Hello> item : hello2
                ) {
                    Impl annotation = item.getAnnotation(Impl.class);
                    if (annotation == null) continue;
                    builder.append(item.getCanonicalName())
                            .append(" / ")
                            .append(annotation.name())
                            .append(" / ")
                            .append(annotation.version())
                            .append('\n');
                }
                String s = builder.toString();
                Log.e("TestAtomClassFragment", s);
                print.setText(s);
            }
        });


        /**
         * 进行根据api 以及 name 精准获取 class 或者根据 正则表达式 模糊定位
         */
        layout.findViewById(R.id.test_class_api_filter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Collection<Class<? extends Hello>> hello2 = apiImplContext().getApis(Hello.class, new AtomApi.ApiFilter<Hello>() {
                    @Override
                    public boolean accept(Class<? extends Hello> clazz, ApiImpls.NameVersion param) {
                        if(param.getVersion() > 2){
                            return true ;
                        }
                        return false;
                    }
                });
                StringBuilder builder = new StringBuilder('\n');
                builder.append("implClass / name / version").append('\n');
                for (Class<? extends Hello> item : hello2
                ) {
                    Impl annotation = item.getAnnotation(Impl.class);
                    if (annotation == null) continue;
                    builder.append(item.getCanonicalName())
                            .append(" / ")
                            .append(annotation.name())
                            .append(" / ")
                            .append(annotation.version())
                            .append('\n');
                }
                String s = builder.toString();
                Log.e("TestAtomClassFragment", s);
                print.setText(s);
            }
        });
    }
}
