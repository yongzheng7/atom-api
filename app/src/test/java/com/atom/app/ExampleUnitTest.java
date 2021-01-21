package com.atom.app;

import org.junit.Test;

import java.util.regex.Pattern;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    @Test
    public void addition_isCorrect() {
        boolean asd = Pattern.matches("hello(.*)", "hello123123123");
        System.out.println(asd);

        String Str = new String("www.runoob.com");

        System.out.print("返回值 :" );

        System.out.println(Pattern.matches("(.*)runoob(.*)", Str));

        System.out.print("返回值 :" );
        System.out.println(Pattern.matches("(.*)google(.*)", Str));

        System.out.print("返回值 :" );
        System.out.println(Pattern.matches("www(.*)", Str));
    }
}