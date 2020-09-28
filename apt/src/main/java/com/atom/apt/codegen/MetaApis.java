package com.atom.apt.codegen;

import com.atom.apt.annotation.ApiImpls;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.FilerException;
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

    private String classSimpleName() {
        return ImportContext.unqualify(mQualifiedName);
    }

    private String generateImports() {
        return mImportContext.generateImports();
    }

    private String importType(String fqcn) {
        return mImportContext.importType(fqcn);
    }

    public void writeFile(Set<MetaApi> apies) {
        try {
            String metaModelPackage = ImportContext.qualifier(mQualifiedName);
            // need to generate the body first, since this will also update the required imports which need to
            // be written out first

            String body = generateBody(apies).toString();
            String fullyQualifiedClassName = getFullyQualifiedClassName(metaModelPackage);
            FileObject fo = mContext.getProcessingEnvironment().getFiler().createSourceFile(fullyQualifiedClassName);
            OutputStream os = fo.openOutputStream();
            PrintWriter pw = new PrintWriter(os);

            if (!metaModelPackage.isEmpty()) {
                pw.println("package " + metaModelPackage + ";");
                pw.println();
            }
            pw.println(generateImports());
            pw.println(body);
            pw.flush();
            pw.close();
            mContext.logMessage(Diagnostic.Kind.NOTE, fullyQualifiedClassName);
        } catch (FilerException filerEx) {
            mContext.logMessage(Diagnostic.Kind.ERROR, "Problem with Filer: " + filerEx.getMessage());
        } catch (IOException ioEx) {
            mContext.logMessage(Diagnostic.Kind.ERROR, "Problem opening file to write MetaModel for " + mQualifiedName + ioEx.getMessage());
        }
    }


    private StringBuffer generateBody(Set<MetaApi> apiImpls) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(sw);
            if (mContext.addGeneratedAnnotation()) {
                pw.println(mContext.writeGeneratedAnnotation(mImportContext));
            }
            if (mContext.isAddSuppressWarningsAnnotation()) {
                pw.println(mContext.writeSuppressWarnings());
            }
            printClassDeclaration(pw);
            pw.println();
            pw.println("     @SuppressWarnings(\"unchecked\")");
            pw.println("     public " + classSimpleName() + "() {");
            pw.println();

            for (MetaApi metaApi : apiImpls) {
                mApis.add(metaApi.getApiQualifiedName());
            }
            for (String api : mApis) {
                // String classname = importType(api);
                String implnames = getImplNames(api, apiImpls);
                if ("".equals(implnames)) {
                    continue;
                }
                pw.println("        // Add " + api);
                pw.println("        add(" + api + ".class, " + implnames + ");");
            }
            pw.println("    }");
            pw.println();

            pw.println("}");
            return sw.getBuffer();
        } finally {
            if (pw != null) {
                pw.close();
            }
        }
    }

    private void printClassDeclaration(PrintWriter pw) {
        pw.print("public class " + classSimpleName());
        pw.print(" extends " + ApiImpls.class.getCanonicalName());
        pw.println(" {");
    }

    private String getFullyQualifiedClassName(String metaModelPackage) {
        String fullyQualifiedClassName = "";
        if (!metaModelPackage.isEmpty()) {
            fullyQualifiedClassName = fullyQualifiedClassName + metaModelPackage + ".";
        }
        fullyQualifiedClassName = fullyQualifiedClassName + classSimpleName();
        return fullyQualifiedClassName;
    }


    private String getImplNames(String api, Collection<MetaApi> impls) {
        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        for (MetaApi metaApi : impls) {
            if (!metaApi.isApiImpl(api)) {
                continue;
            }
            if (!isFirst) {
                sb.append(", ");
            } else {
                isFirst = false;
            }
            sb.append(importType(metaApi.getImplQualifiedName()));
            sb.append(".class");
        }
        return sb.toString();
    }
}
