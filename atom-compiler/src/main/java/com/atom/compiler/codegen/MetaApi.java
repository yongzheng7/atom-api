package com.atom.compiler.codegen;

import com.atom.annotation.Impl;
import com.atom.compiler.utils.TypeUtils;

import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;

/**
 * Created by HYW on 2017/5/26.
 */
@SuppressWarnings({"WeakerAccess"})
public class MetaApi {

    private final Context mContext;
    private final TypeElement implTypeElement;
    private final String implQualifiedName;
    private final TypeElement apiTypeElement;
    private final String apiQualifiedName;
    private final String implAnnotationName ;
    private final long implAnnotationVersion ;

    private MetaApi(Context context, TypeElement classElement) {
        Impl annotation = classElement.getAnnotation(Impl.class);
        String apiClassQualifiedName;
        TypeElement apiClassTypeElement;
        try {
            Class<?> clazz = annotation.api();
            apiClassQualifiedName = clazz.getCanonicalName();
            apiClassTypeElement = context.getElementUtils().getTypeElement(apiClassQualifiedName);
        } catch (MirroredTypeException mte) {
            DeclaredType classTypeMirror = (DeclaredType) mte.getTypeMirror();
            TypeElement classTypeElement = (TypeElement) classTypeMirror.asElement();
            apiClassTypeElement = classTypeElement;
            apiClassQualifiedName = classTypeElement.getQualifiedName().toString();
        }
        this.mContext = context;
        this.implTypeElement = classElement;
        this.implQualifiedName = classElement.getQualifiedName().toString();
        this.apiTypeElement = apiClassTypeElement; // 对应的接口的element对象
        this.apiQualifiedName = apiClassQualifiedName; // 实现的接口的名字

        this.implAnnotationName = annotation.name();
        this.implAnnotationVersion = annotation.version();

    }

    public static MetaApi isValidApiAnnotatedClass(Context context, Element element) {
        TypeElement classElement = (TypeElement) element;
        Set<Modifier> modifierSet = classElement.getModifiers();
        if (!modifierSet.contains(Modifier.PUBLIC)) {
            context.logger().error(
                    "The class " + classElement.getQualifiedName().toString()
                            + " is not public.", classElement
            );
            return null;
        }
        if (modifierSet.contains(Modifier.ABSTRACT)) {
            context.logger().error("The class " + classElement.getQualifiedName().toString()
                            + " is abstract. You can't annotate abstract classes with @" + Impl.class.getSimpleName(),
                    classElement);
            return null;
        }
        if (!TypeUtils.hasPublicEmptyDefaultConstructor(classElement)) {
            context.logger().error("The class " + classElement.getQualifiedName().toString()
                            + " must provide an public empty default constructor",
                    classElement);
            return null;
        }
        MetaApi metaApi = new MetaApi(context, classElement);
        if (Object.class.getCanonicalName().equals(metaApi.getApiQualifiedName())) {
            return metaApi;
        }
        TypeElement superClassElement = metaApi.apiTypeElement;

        if (!TypeUtils.isAssignable(context, classElement, superClassElement)) {
            String superClassName = superClassElement.getQualifiedName().toString();
            if (ElementKind.INTERFACE.equals(superClassElement.getKind())) {
                context.logger().error("The class " + classElement.getQualifiedName().toString()
                                + " annotated with @" + Impl.class.getSimpleName()
                                + " must implement the interface " + superClassName,
                        classElement);
            } else {
                context.logger().error("The class " + classElement.getQualifiedName().toString()
                                + " annotated with @" + Impl.class.getSimpleName()
                                + " must inherit from " + superClassName,
                        classElement);
            }
            return null;
        }
        return metaApi;
    }

    public Context getContext() {
        return mContext;
    }

    public String getApiQualifiedName() {
        return apiQualifiedName;
    }

    public String getImplQualifiedName() {
        return implQualifiedName;
    }

    public TypeElement getApiTypeElement() {
        return apiTypeElement;
    }

    public TypeElement getImplTypeElement() {
        return implTypeElement;
    }

    public boolean isApiImpl(String apiQualifiedName) {
        return this.apiQualifiedName.equals(apiQualifiedName);
    }

    public String getImplAnnotationName() {
        return implAnnotationName;
    }

    public long getImplAnnotationVersion() {
        return implAnnotationVersion;
    }
}
