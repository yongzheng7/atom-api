package com.atom.compiler.processor;

import com.atom.compiler.codegen.Context;
import com.atom.compiler.utils.Consts;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.SourceVersion;
public abstract class BaseProcessor extends AbstractProcessor {


    protected Context context;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        context = new Context(processingEnv) ;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }


    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return new HashSet<>();
    }

    @Override
    public Set<String> getSupportedOptions() {
        HashSet<String> options = new HashSet<>();
        options.add(Consts.DEBUG_OPTION);
        options.add(Consts.BUNDLE_CLASSNAME);
        return options;
    }
}
