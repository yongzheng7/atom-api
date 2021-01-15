package com.atom.compiler.utils;

import java.text.SimpleDateFormat;

/**
 * Some consts used in processors
 */
public final class Consts {
    public static final String TAG = "Atom";
    public static final String PROXY = "Proxy";
    public static final String DEBUG_OPTION = "debug";
    public static final String BUNDLE_CLASSNAME = "bundleClassname";
    public static final SimpleDateFormat DATEFORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    public static final String APTPATH = "com.atom.apt.proxy";

    public static String upperFirstLetter(final String s) {
        if (org.apache.commons.lang3.StringUtils.isEmpty(s) || !Character.isLowerCase(s.charAt(0))) {
            return s;
        }
        return (char) (s.charAt(0) - 32) + s.substring(1);
    }
}