package com.atom.core;

import androidx.annotation.Keep;

import com.atom.api.ApiBundle;
import com.atom.apt.annotation.Impl;
import com.atom.apt.core.ApiImplBundles;

@Keep
@Impl(api = ApiBundle.class)
public class ApiBundleImpls extends ApiImplBundles implements ApiBundle {

}
