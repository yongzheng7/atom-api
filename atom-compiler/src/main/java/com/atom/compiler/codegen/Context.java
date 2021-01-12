package com.atom.compiler.codegen;


import com.atom.compiler.utils.Consts;
import com.atom.compiler.utils.Logger;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

@SuppressWarnings({"WeakerAccess"})
public final class Context {

    private final static SimpleDateFormat DATEFORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    private final static String aptPath = "com.atom.apt";

    private final Collection<String> generatedModelClasses = new HashSet<>();
    private final Collection<String> apiClass = new HashSet<>();

    private final ProcessingEnvironment processingEnv;
    private final Filer mFiler;
    private final Logger logger;
    private final Types typeUtils;
    private final Elements elementUtils;

    private String moduleName;
    private boolean debug;

    public Context(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
        this.mFiler = processingEnv.getFiler();
        this.typeUtils = processingEnv.getTypeUtils();
        this.elementUtils = processingEnv.getElementUtils();
        // Attempt to get user configuration [moduleName]
        Map<String, String> options = processingEnv.getOptions();
        if (MapUtils.isNotEmpty(options)) {
            moduleName = options.get(Consts.BUNDLE_CLASSNAME);
            debug = options.get(Consts.DEBUG_OPTION).equalsIgnoreCase("true");
        }
        logger = new Logger(processingEnv.getMessager(), debug);
        if (StringUtils.isNotEmpty(moduleName)) {
            moduleName = moduleName.replaceAll("[^0-9a-zA-Z_]+", "");
            moduleName = Consts.TAG + Consts.upperFirstLetter(moduleName);
            logger.info("The user has configuration the module name, it was [" + moduleName + "]");
        } else {
            logger.error("Module name is Empty !!!");
            throw new RuntimeException("Atom::Compiler >>> No module name, for more information, look at gradle log.");
        }
    }

    public ProcessingEnvironment getProcessingEnvironment() {
        return processingEnv;
    }

    public Elements getElementUtils() {
        return elementUtils;
    }

    public Filer getFiler() {
        return mFiler;
    }

    public Types getTypeUtils() {
        return typeUtils;
    }

    public TypeElement getTypeElementForFullyQualifiedName(String fqcn) {
        return getElementUtils().getTypeElement(fqcn);
    }

    public void markGenerated(String name) {
        generatedModelClasses.add(name);
    }

    public void putApi(String name) {
        apiClass.add(name);
    }

    public boolean isApi(String name) {
        return apiClass.contains(name);
    }

    public Logger logger() {
        return logger;
    }

    public String packet() {
        return aptPath;
    }

    public SimpleDateFormat dateformat() {
        return DATEFORMAT;
    }

    public String getModuleName() {
        return moduleName;
    }

    @Override
    public String toString() {
        return "Context" + ", logDebug=" + debug + '}';
    }
}
