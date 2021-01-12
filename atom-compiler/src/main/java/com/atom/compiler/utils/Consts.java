package com.atom.compiler.utils;

/**
 * Some consts used in processors
 */
public final class Consts {
    public static final String TAG = "Atom";
    public static final String DEBUG_OPTION = "debug";
    public static final String BUNDLE_CLASSNAME = "bundleClassname";


    public static String upperFirstLetter(final String s) {
        if (org.apache.commons.lang3.StringUtils.isEmpty(s) || !Character.isLowerCase(s.charAt(0))) {
            return s;
        }
        return (char) (s.charAt(0) - 32) + s.substring(1);
    }
}