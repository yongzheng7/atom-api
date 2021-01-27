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



@Impl(api = AbstractFragment.class, name = "main/menu/impl")
public class TestAtomImplFragment extends AbstractFragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_test_impl, container, false);
        initView(inflate);
        return inflate;
    }

    void initView(View layout) {
        final TextView print = layout.findViewById(R.id.print_view);
        /**
         * 进行根据api获取所有模块实现该api的class
         */
        layout.findViewById(R.id.test_class_impl_api).setOnClickListener(v -> {
            Hello impl = apiImplContext().getImpl(Hello.class);
            StringBuilder builder = new StringBuilder('\n');
            builder.append("implClass / name / version").append('\n');
            Impl annotation = impl.getClass().getAnnotation(Impl.class);
            if (annotation != null)
                builder.append(impl.getClass().getCanonicalName())
                        .append(" / ")
                        .append(annotation.name())
                        .append(" / ")
                        .append(annotation.version())
                        .append('\n');
            String s = builder.toString();
            Log.e("TestAtomImplFragment", s);
            print.setText(s);
        });
        /**
         * 进行根据api 以及 name 精准获取 class 或者根据 正则表达式 模糊定位
         */
        layout.findViewById(R.id.test_class_impl_api_version).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // version 为 >= 0 则精准获取
                //Hello impl = apiImplContext().getImpl(Hello.class , 5) ;
                // version < 0 则进行版本排序 , 比如 hello 实现的版本有
                // 0 1 2 3 4 4 5 6 6 8 一共有10个版本 那么-1 获取到的是版本8 , -2 获取的 6 ,  -3 也是6 以此类推 -10 获取的为0 -11 判断后和-10相同
                Hello impl = apiImplContext().getImpl(Hello.class, -111);
                StringBuilder builder = new StringBuilder('\n');
                builder.append("implClass / name / version").append('\n');
                Impl annotation = impl.getClass().getAnnotation(Impl.class);
                if (annotation != null)
                    builder.append(impl.getClass().getCanonicalName())
                            .append(" / ")
                            .append(annotation.name())
                            .append(" / ")
                            .append(annotation.version())
                            .append('\n');
                String s = builder.toString();
                Log.e("TestAtomImplFragment", s);
                print.setText(s);
            }
        });
        /**
         * 进行根据api 以及 name 精准获取 class 或者根据 正则表达式 模糊定位
         */
        layout.findViewById(R.id.test_class_impl_all).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Hello impl;
                //impl = apiImplContext().getImpl(Hello.class, "hello2", -1, false);
                //impl = apiImplContext().getImpl(Hello.class, "hello2", -2, false);
                impl = apiImplContext().getImpl(Hello.class, "hello2", 3, false);
                //impl = apiImplContext().getImpl(Hello.class, "hello2", 3, false);
                StringBuilder builder = new StringBuilder('\n');
                builder.append("implClass / name / version").append('\n');
                if(impl == null){
                    builder.append("参数错误,请重新设置") ;
                }else{
                    Impl annotation = impl.getClass().getAnnotation(Impl.class);
                    if (annotation != null)
                        builder.append(impl.getClass().getCanonicalName())
                                .append(" / ")
                                .append(annotation.name())
                                .append(" / ")
                                .append(annotation.version())
                                .append('\n');

                }
                String s = builder.toString();
                Log.e("TestAtomImplFragment", s);
                print.setText(s);
            }
        });
    }
}
