package com.atom.apt.codegen;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;

import javax.annotation.Generated;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * Created by HYW on 2017/5/25.
 */
@SuppressWarnings({"WeakerAccess"})
public final class Context {
    @SuppressWarnings("SimpleDateFormat")
    public static final ThreadLocal<SimpleDateFormat> SIMPLE_DATE_FORMAT =
            ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));

    private final ProcessingEnvironment pe;
    private final boolean logDebug;
    private final Map<String, String> options;
    // keep track of all classes for which model have been generated
    private final Collection<String> generatedModelClasses = new HashSet<>();

    public Context(ProcessingEnvironment env) {
        this.pe = env;
        this.options = env.getOptions();
        String tmp = options.get(ApiAnnotationProcessor.DEBUG_OPTION);
        logDebug = Boolean.parseBoolean(tmp);
        logMessage(Diagnostic.Kind.NOTE, "ApiAnnotation init Context start \n");
        for (Map.Entry<String, String> entry : options.entrySet()) {
            logMessage(Diagnostic.Kind.NOTE, "\nApiAnnotation init Context option: " + entry.getKey() + " -> " + entry.getValue());
        }
        logMessage(Diagnostic.Kind.NOTE, "\nApiAnnotation init Context end");
    }

    public ProcessingEnvironment getProcessingEnvironment() {
        return pe;
    }

    public Elements getElementUtils() {
        return pe.getElementUtils();
    }

    public Types getTypeUtils() {
        return pe.getTypeUtils();
    }

    public TypeElement getTypeElementForFullyQualifiedName(String fqcn) {
        return getElementUtils().getTypeElement(fqcn);
    }

    void markGenerated(String name) {
        generatedModelClasses.add(name);
    }

    boolean isAlreadyGenerated(String name) {
        return generatedModelClasses.contains(name);
    }


    public String writeGeneratedAnnotation(ImportContext importContext) {
        return "@" +
                importContext.importType(Generated.class.getName()) +
                "(value = \"" +
                ApiAnnotationProcessor.class.getName() +
                "\", date = \"" +
                Context.SIMPLE_DATE_FORMAT.get().format(new Date()) +
                "\")";
    }

    public String writeSuppressWarnings() {
        return "@SuppressWarnings(\"all\")";
    }


    public void logMessage(Diagnostic.Kind type, String message) {
        if (!logDebug ) {
            return;
        }
        pe.getMessager().printMessage(type, message);
    }

    public void logMessage(Diagnostic.Kind type, String message, Element element) {
        if (!logDebug ) {
            return;
        }
        pe.getMessager().printMessage(type, message, element);
    }

    @Override
    public String toString() {
        return "Context" + ", logDebug=" + logDebug + '}';
    }
}
