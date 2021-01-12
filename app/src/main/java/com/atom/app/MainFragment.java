package com.atom.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.atom.annotation.Impl;
import com.atom.api.ApiImplContext;
import com.atom.api.app.Hello;
import com.atom.core.ui.AbstractFragment;
@Impl(api = AbstractFragment.class)
public class MainFragment extends AbstractFragment {

    public MainFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.activity_fragment, container, false);
        TextView viewById = inflate.findViewById(R.id.testText2);
        if(viewById != null){
            viewById.setOnClickListener(view -> apiImplContext().post(() -> {
                ApiImplContext apiImplContext = apiImplContext();
                Hello api = apiImplContext.getApi(Hello.class);
                if (api != null) {
                    api.hello();
                }
                Toast.makeText(getContext(), "mainFragment", Toast.LENGTH_SHORT).show();
            }));


        }
        return inflate;
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

    }
}
