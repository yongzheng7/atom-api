package com.atom.data;

import androidx.annotation.Keep;

import com.atom.api.ApiBundle;
import com.atom.apt.annotation.Impl;
import com.atom.apt.data.ApiImplBundles;

@Keep
@Impl(api = ApiBundle.class)
public class ApiBundleImpls extends ApiImplBundles implements ApiBundle {

}
