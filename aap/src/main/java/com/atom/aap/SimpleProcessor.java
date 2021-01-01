package com.atom.aap;


import com.google.auto.service.AutoService;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Generated;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

@AutoService(Processor.class)
public class SimpleProcessor extends AbstractProcessor {

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        types.add(Generated.class.getCanonicalName());
        return types;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    private static final String KEY_MODULE_NAME = "aapClassname";
    private Filer mFiler;
    private Types mTypes;
    private Elements mElements;
    private Logger mLogger;
    private String moduleName = null;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mFiler = processingEnv.getFiler();
        mTypes = processingEnv.getTypeUtils();
        mElements = processingEnv.getElementUtils();
        mLogger = new Logger(processingEnv.getMessager());
        Map<String, String> options = processingEnv.getOptions();
        if (MapUtils.isNotEmpty(options)) {
            moduleName = options.get(KEY_MODULE_NAME);
        }
        if (StringUtils.isNotEmpty(moduleName)) {
            //moduleName = moduleName.replaceAll("[^0-9a-zA-Z_]+", "");
            mLogger.info("The user has configuration the module name, \n it was [" + moduleName + "]");
        } else {
            mLogger.info("These no module name, at 'build.gradle', like :\n" +
                    "javaCompileOptions {\n" +
                    "    annotationProcessorOptions {\n" +
                    "        arguments = [ moduleName : project.getName() ]\n" +
                    "    }\n" +
                    "}\n");
            moduleName = "app";
//            throw new RuntimeException("XPage::Compiler >>> No module name, for more information, look at gradle log.");
        }
        mLogger.info(">>> PageConfigProcessor init. <<<");
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (CollectionUtils.isNotEmpty(set)) {
            Set<? extends Element> pageElements = roundEnvironment.getElementsAnnotatedWith(Generated.class);
            try {
                mLogger.info(">>> Found Pages, start... <<<");
                for (Element aa :pageElements) {
                    mLogger.info(">>> Found Pages, "+aa.toString()+" <<<");
                }
                mLogger.info(">>> Found Pages, end  ... <<<");
            } catch (Exception e) {
                mLogger.error(e);
            }
            return true;
        }
        return false;
    }
}
