package com.atom.compiler.processor;

import com.atom.annotation.Impl;
import com.atom.compiler.codegen.MetaApi;
import com.atom.compiler.codegen.MetaApis;
import com.google.auto.service.AutoService;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

@AutoService(Processor.class)
public class ApiImplsProcessor extends BaseProcessor {

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        context.logger().info(">>> ApiImplsProcessor init. \n<<<");
    }


    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> supportedAnnotationTypes = super.getSupportedAnnotationTypes();
        supportedAnnotationTypes.add(Impl.class.getCanonicalName());
        return supportedAnnotationTypes;
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        context.logger().info(">>> ApiImplsProcessor process. \n<<<");
        if (roundEnvironment.processingOver() || set.size() == 0) {
            return false;
        }
        final Set<MetaApi> apiImpls = new HashSet<>();
        for (Element element : roundEnvironment.getElementsAnnotatedWith(Impl.class)) {
            MetaApi metaApi = MetaApi.isValidApiAnnotatedClass(context, element);
            if (metaApi != null) {
                apiImpls.add(metaApi);
            }
        }
        new MetaApis(context, context.getModuleName()).writeFile(apiImpls);
        return false;
    }
}
