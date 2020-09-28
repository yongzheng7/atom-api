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
    private boolean addGeneratedAnnotation = true;
    private boolean addGenerationDate = true;
    private boolean addSuppressWarningsAnnotation = true;

    public Context(ProcessingEnvironment env) {
        this.pe = env;
        this.options = env.getOptions();
        String tmp = options.get(ApiAnnotationProcessor.DEBUG_OPTION);
        logDebug = Boolean.parseBoolean(tmp);

        tmp = options.get(ApiAnnotationProcessor.ADD_GENERATED_ANNOTATION);
        if (tmp != null && !tmp.isEmpty()) {
            setAddGeneratedAnnotation(Boolean.parseBoolean(tmp));
        }
        tmp = options.get(ApiAnnotationProcessor.ADD_GENERATION_DATE);
        if (tmp != null && !tmp.isEmpty()) {
            setAddGenerationDate(Boolean.parseBoolean(tmp));
        }
        tmp = options.get(ApiAnnotationProcessor.ADD_SUPPRESS_WARNINGS_ANNOTATION);
        if (tmp != null && !tmp.isEmpty()) {
            setAddSuppressWarningsAnnotation(Boolean.parseBoolean(tmp));
        }
        logMessage(Diagnostic.Kind.NOTE, "ApiAnnotation init Context start");
        for (Map.Entry<String, String> entry : options.entrySet()) {
            logMessage(Diagnostic.Kind.NOTE, "ApiAnnotation init Context option: " + entry.getKey() + " -> " + entry.getValue());
        }
        logMessage(Diagnostic.Kind.NOTE, "ApiAnnotation init Context end");
    }

    public ProcessingEnvironment getProcessingEnvironment() {
        return pe;
    }

    public boolean addGeneratedAnnotation() {
        return addGeneratedAnnotation;
    }

    public void setAddGeneratedAnnotation(boolean addGeneratedAnnotation) {
        this.addGeneratedAnnotation = addGeneratedAnnotation;
    }

    public boolean addGeneratedDate() {
        return addGenerationDate;
    }

    public void setAddGenerationDate(boolean addGenerationDate) {
        this.addGenerationDate = addGenerationDate;
    }

    public boolean isAddSuppressWarningsAnnotation() {
        return addSuppressWarningsAnnotation;
    }

    public void setAddSuppressWarningsAnnotation(boolean addSuppressWarningsAnnotation) {
        this.addSuppressWarningsAnnotation = addSuppressWarningsAnnotation;
    }

    public Elements getElementUtils() {
        return pe.getElementUtils();
    }

    public Types getTypeUtils() {
        return pe.getTypeUtils();
    }

    public TypeElement getTypeElementForFullyQualifiedName(String fqcn) {
        Elements elementUtils = pe.getElementUtils();
        return elementUtils.getTypeElement(fqcn);
    }

    void markGenerated(String name) {
        generatedModelClasses.add(name);
    }

    boolean isAlreadyGenerated(String name) {
        return generatedModelClasses.contains(name);
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

    public String writeGeneratedAnnotation(ImportContext importContext) {
        StringBuilder generatedAnnotation = new StringBuilder();
        generatedAnnotation.append("@")
                .append(importContext.importType(Generated.class.getName()))
                .append("(value = \"")
                .append(ApiAnnotationProcessor.class.getName());
        if (addGeneratedDate()) {
            generatedAnnotation.append("\", date = \"")
                    .append(Context.SIMPLE_DATE_FORMAT.get().format(new Date()))
                    .append("\")");
        } else {
            generatedAnnotation.append("\")");
        }
        return generatedAnnotation.toString();
    }

    public String writeSuppressWarnings() {
        return "@SuppressWarnings(\"all\")";
    }

    @Override
    public String toString() {
        return "Context" + ", logDebug=" + logDebug + '}';
    }
}
