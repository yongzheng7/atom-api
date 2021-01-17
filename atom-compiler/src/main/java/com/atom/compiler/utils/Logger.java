package com.atom.compiler.utils;

import org.apache.commons.lang3.StringUtils;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

/**
 * 日志记录
 */
public class Logger {
    private Messager msg;
    private final boolean debug;

    public Logger(Messager messager, boolean debug) {
        msg = messager;
        this.debug = debug;
    }


    public void info(String info) {
        if (StringUtils.isNotEmpty(info)&&debug) {
            msg.printMessage(Diagnostic.Kind.NOTE,  info+"\n");
        }
    }

    public void error(String error) {
        if (StringUtils.isNotEmpty(error)&&debug) {
            msg.printMessage(Diagnostic.Kind.ERROR,  "An exception is encountered, [" + error + "]"+"\n");
        }
    }

    public void error(String error, Element var3) {
        if (StringUtils.isNotEmpty(error) && var3 != null&&debug) {
            msg.printMessage(Diagnostic.Kind.ERROR,  "An exception is encountered, [" + error + "]"+"\n", var3);
        }
    }

    public void error(Throwable error) {
        if (null != error&&debug) {
            msg.printMessage(Diagnostic.Kind.ERROR,  "An exception is encountered, [" + error.getMessage() + "]" + "\n" + formatStackTrace(error.getStackTrace()));
        }
    }

    public void warning(CharSequence warning) {
        if (StringUtils.isNotEmpty(warning)&&debug) {
            msg.printMessage(Diagnostic.Kind.WARNING,  warning);
        }
    }

    private String formatStackTrace(StackTraceElement[] stackTrace) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : stackTrace) {
            sb.append("    at ").append(element.toString());
            sb.append("\n");
        }
        return sb.toString();
    }
}
