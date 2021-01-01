package com.atom.apt.codegen;

import com.atom.apt.annotation.Impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

/**
 * Created by HYW on 2017/5/25.
 * <p>
 * Processor Impls annotation make a class
 * <pre>
 * example:
 *
 * interface A {}
 *
 * &#64;Impl(api=A.class)
 * class AImp1 implement A {}
 *
 * &#64;Impl(api=A.class)
 * class AImp2 implement A {}
 *
 * &#64;Impls(apis={A.class, B.class}, name="AutoGenAsImpls")
 * class As {
 *      private final  AbstractApiImpls apisImp = new AutoGenAsA();
 * }
 *
 * ApiAnnotationProcessor generated
 * class AutoGenAsImpls extern AbstractApiImpls {
 *     public AutoGenAsImpls() {}
 *          add(A.class, AImp1.class, AImp2.class);
 *          add(B.class, BImp1.class, BImp2.class, BImp3.class);
 *      }
 * }
 *
 * </pre>
 */
@SupportedOptions({
        ApiAnnotationProcessor.DEBUG_OPTION
})
public class ApiAnnotationProcessor extends AbstractProcessor {

    static final String DEBUG_OPTION = "debug";

    static final String BUNDLE_CLASSNAME = "bundleClassname";

    private static final Boolean ALLOW_OTHER_PROCESSORS_TO_CLAIM_ANNOTATIONS = Boolean.FALSE;
    private Context context;
    private final AtomicLong processSize = new AtomicLong(0);

    public ApiAnnotationProcessor() {

    }

    @Override
    public void init(ProcessingEnvironment env) {
        super.init(env);
        context = new Context(env);
        context.logMessage(Diagnostic.Kind.NOTE, getClass().getSimpleName() + " init \n");
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        context.logMessage(Diagnostic.Kind.NOTE, getClass().getSimpleName() + " getSupportedSourceVersion \n");
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        context.logMessage(Diagnostic.Kind.NOTE, getClass().getSimpleName() + " getSupportedAnnotationTypes \n");
        Set<String> set = new HashSet<>();
        set.add(Impl.class.getCanonicalName());
        return Collections.unmodifiableSet(set);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        context.logMessage(Diagnostic.Kind.NOTE, getClass().getSimpleName() + " process [" +processSize.addAndGet(1)+']'+" \n");
        if (roundEnv.processingOver() || annotations.size() == 0) {
            return ALLOW_OTHER_PROCESSORS_TO_CLAIM_ANNOTATIONS;
        }
        final Set<MetaApi> apiImpls = new HashSet<>();
        for (Element element : roundEnv.getElementsAnnotatedWith(Impl.class)) {
            MetaApi metaApi = MetaApi.isValidApiAnnotatedClass(context, element);
            if (metaApi != null) {
                context.logMessage(Diagnostic.Kind.NOTE, " process [" +processSize.addAndGet(1)+']'+" \n");
                apiImpls.add(metaApi);
            }
        }
        processApis(roundEnv, apiImpls);
        return ALLOW_OTHER_PROCESSORS_TO_CLAIM_ANNOTATIONS;
    }

    private void processApis(RoundEnvironment roundEnv, Set<MetaApi> apies) {
        context.logMessage(Diagnostic.Kind.NOTE, "processApis 1  ");
        String bundleClassname = context.getProcessingEnvironment().getOptions().get(ApiAnnotationProcessor.BUNDLE_CLASSNAME);
        if (bundleClassname == null || bundleClassname.isEmpty()) {
            return;
        }
        if (context.isAlreadyGenerated(bundleClassname)) {
            return;
        }
        MetaApis metaApis = new MetaApis(context, bundleClassname);
        // auto generate java files
        context.logMessage(Diagnostic.Kind.NOTE, "Writing bundle \"" + bundleClassname);
        metaApis.writeFile(apies);
        context.markGenerated(bundleClassname);
    }

}
