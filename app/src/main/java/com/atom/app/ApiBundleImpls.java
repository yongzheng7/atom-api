package com.atom.app;

import androidx.annotation.Keep;

import com.atom.api.ApiBundle;
import com.atom.apt.annotation.Impl;
import com.atom.apt.app.ApiImplBundles;


@Keep
@Impl(api = ApiBundle.class)
public class ApiBundleImpls extends ApiImplBundles implements ApiBundle {

}
