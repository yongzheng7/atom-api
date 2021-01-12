package com.atom.apt.codegen;

import com.atom.annotation.Impl;

import org.apache.commons.lang3.StringUtils;

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
        ApiAnnotationProcessor.DEBUG_OPTION,
        ApiAnnotationProcessor.BUNDLE_CLASSNAME
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
        context.logger().info(getClass().getSimpleName() + " init atom\n");
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        context.logger().info(getClass().getSimpleName() + " getSupportedSourceVersion \n");
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        context.logger().info(getClass().getSimpleName() + " getSupportedAnnotationTypes \n");
        Set<String> set = new HashSet<>();
        set.add(Impl.class.getCanonicalName());
        return Collections.unmodifiableSet(set);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        context.logger().info(getClass().getSimpleName() + " process [" + processSize.addAndGet(1) + ']' + " \n");
        if (roundEnv.processingOver() || annotations.size() == 0) {
            return ALLOW_OTHER_PROCESSORS_TO_CLAIM_ANNOTATIONS;
        }
        final Set<MetaApi> apiImpls = new HashSet<>();
        for (Element element : roundEnv.getElementsAnnotatedWith(Impl.class)) {
            MetaApi metaApi = MetaApi.isValidApiAnnotatedClass(context, element);
            if (metaApi != null) {
                context.logger().info(" process [" + processSize.addAndGet(1) + ']' + " \n");
                apiImpls.add(metaApi);
            }
        }
        processApis(roundEnv, apiImpls);
        return ALLOW_OTHER_PROCESSORS_TO_CLAIM_ANNOTATIONS;
    }

    private static final String TAG = "Atom";

    private void processApis(RoundEnvironment roundEnv, Set<MetaApi> apies) {
        context.logger().info("ApiAnnotationProcessor processApis\"");
        String bundleClassname = context.getProcessingEnvironment().getOptions().get(ApiAnnotationProcessor.BUNDLE_CLASSNAME);
        if (StringUtils.isNotEmpty(bundleClassname)) {
            bundleClassname = bundleClassname.replaceAll("[^0-9a-zA-Z_]+", "");
            bundleClassname = TAG + upperFirstLetter(bundleClassname);
            context.logger().info("The user has configuration the module name, it was [" + bundleClassname + "]");
        } else {
            throw new RuntimeException("ARouter::Compiler >>> No module name, for more information, look at gradle log.");
        }
        MetaApis metaApis = new MetaApis(context, bundleClassname);
        metaApis.writeFile(apies);

    }


    public static String upperFirstLetter(final String s) {
        if (StringUtils.isEmpty(s) || !Character.isLowerCase(s.charAt(0))) {
            return s;
        }
        return (char) (s.charAt(0) - 32) + s.substring(1);
    }
}
