package com.atom.apt.codegen;

import com.atom.apt.annotation.ApiImpls;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.Generated;
import javax.annotation.processing.FilerException;
import javax.lang.model.element.Modifier;
import javax.tools.Diagnostic;
import javax.tools.FileObject;

/**
 * Created by HYW on 2017/5/25.
 */
@SuppressWarnings({"WeakerAccess"})
public class MetaApis {

    private final Context mContext;
    private final ImportContext mImportContext;
    private final String mQualifiedName;
    private final Set<String> mApis;

    public MetaApis(Context context, String bundleClassname) {
        this.mContext = context;
        this.mQualifiedName = bundleClassname;
        this.mApis = new HashSet<>();
        this.mImportContext = new ImportContext(ImportContext.qualifier(bundleClassname));
    }

    private String importType(String fqcn) {
        return mImportContext.importType(fqcn);
    }

    public void writeFile(Set<MetaApi> apies) {
        String metaModelPackage = ImportContext.qualifier(mQualifiedName);
        String metaModelName = ImportContext.unqualify(mQualifiedName);
        // need to generate the body first, since this will also update the required imports which need to
        // be written out first
        ClassName pageConfigClassName = ClassName.get(metaModelPackage, metaModelName);
        mContext.logger().warning("warning >>> " + pageConfigClassName.canonicalName());

        TypeSpec.Builder pageConfigBuilder = TypeSpec.classBuilder(pageConfigClassName)
                .superclass(ClassName.get(ApiImpls.class));

        String format = Context.SIMPLE_DATE_FORMAT.get().format(new Date());
        // 代码创建 文档
        CodeBlock javaDoc = CodeBlock.builder()
                .add("<p>这是ApiAnnotationProcessor自动生成的类，用以自动进行页面的注册。</p>\n")
                .add("\n")
                .add("@date ").add(format)
                .add("\n")
                .build();

        AnnotationSpec GeneratedAnnountation = AnnotationSpec.builder(Generated.class)
                .addMember("value", "$S", ApiAnnotationProcessor.class.getName())
                .addMember("date", "$S", format)
                .build();

        AnnotationSpec suppressWarningsAnnountation = AnnotationSpec.builder(SuppressWarnings.class)
                .addMember("value", "$S" , "all")
                .build();

        MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC);

        Iterator<MetaApi> iterator = apies.iterator();
        while (iterator.hasNext()) {
            MetaApi next = iterator.next();
            if (next.getApiQualifiedName() != null) {
                // String classname = importType(api);
                List<ClassName> implNames = getImplNames(next, apies);
                if (implNames.isEmpty()) {
                    continue;
                } else {
                    int size = implNames.size();
                    StringBuilder stringBuilder = new StringBuilder();
                    //add(com.atom.api.ApiBundle.class , ApiBundleImpls.class)
                    stringBuilder.append("add(") ;
                    for (; size != 0; size--) {
                        stringBuilder.append("$L.class") ;
                        if(size!= 1){
                            stringBuilder.append(",");
                        }
                    }
                    stringBuilder.append(")") ;
                    String s = stringBuilder.toString();
                    mContext.logger().warning("warning >>> " +s);
                    ClassName[] classNames = implNames.toArray(new ClassName[]{});
                    constructorBuilder.addStatement(s ,classNames );
                }

            }
        }
        MethodSpec constructor = constructorBuilder.build();

        // 建造class者 组合完成
        pageConfigBuilder
                .addJavadoc(javaDoc)
                .addAnnotation(GeneratedAnnountation)
                .addAnnotation(suppressWarningsAnnountation)
                .addModifiers(Modifier.PUBLIC)
                .addMethod(constructor);
        // 写入某个包中
        JavaFile.Builder builder = JavaFile.builder(metaModelPackage, pageConfigBuilder.build());
        JavaFile build = builder.build();
        try {
            build.writeTo(mContext.getFiler());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<ClassName> getImplNames(MetaApi api, Collection<MetaApi> impls) {
        final List<ClassName> list = new ArrayList<>();
        for (MetaApi metaApi : impls) {
            if (!metaApi.isApiImpl(api.getApiQualifiedName())) {
                continue;
            }
            list.add(ClassName.get(metaApi.getImplTypeElement()));
        }
        if (!list.isEmpty()) {
            list.add(0, ClassName.get(api.getApiTypeElement()));
        }
        return list;
    }
}
