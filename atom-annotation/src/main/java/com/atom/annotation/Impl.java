package com.atom.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Impl {

    /**
     * (Optional) The API interface class. Defaults to a Object.class ignore class check.
     * <p>
     * The Class with @Impl must be public class and  must not ba a abstract class and must provide an public empty default constructor
     *
     * @return interface class
     */
    Class<?> api() default Object.class;

    /**
     * (Optional) The identification of the Implement.
     *
     * @return The identification of the Implement
     */
    String name() default "";

    /**
     * (Optional) The version of the Implement.
     *
     * @return The version of the Implement
     */
    int version() default 0;
}
