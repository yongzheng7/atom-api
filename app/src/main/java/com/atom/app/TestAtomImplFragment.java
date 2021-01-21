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

@Impl(api = AbstractFragment.class, name = "main/menu/impl")
public class TestAtomImplFragment extends AbstractFragment {

}
