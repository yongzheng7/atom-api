package com.atom.apt.codegen;

import com.atom.apt.annotation.Impl;

import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.tools.Diagnostic;

/**
 * Created by HYW on 2017/5/26.
 */
@SuppressWarnings({"WeakerAccess"})
public class MetaApi {

    private final TypeElement implTypeElement;
    private final Context mContext;
    private final String implQualifiedName;
    private final TypeElement apiTypeElement;
    private final String apiQualifiedName;

    private MetaApi(Context context, String apiQualifiedName, String implQualifiedName) {
        this.mContext = context;
        this.apiTypeElement = null;
        this.apiQualifiedName = apiQualifiedName;
        this.implTypeElement = null;
        this.implQualifiedName = implQualifiedName;
    }

    private MetaApi(Context context, TypeElement classElement) {
        Impl annotation = classElement.getAnnotation(Impl.class); // 获取类class 的注解
        String apiClassQualifiedName;
        TypeElement apiClassTypeElement;
        try {
            Class<?> clazz = annotation.api(); // 获取实现的接口 api
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
        this.apiQualifiedName = apiClassQualifiedName; // 实现的接口的名字
        this.apiTypeElement = apiClassTypeElement; // 对应的element对象
    }

    public static MetaApi isValidApiAnnotatedClass(Context context, String apiQualifiedName, String implQualifiedName) {
        return new MetaApi(context, apiQualifiedName, implQualifiedName);
    }

    public static MetaApi isValidApiAnnotatedClass(Context context, Element element) {
        // 强转未 Type
        TypeElement classElement = (TypeElement) element;
        // 获取修饰符
        Set<Modifier> modifierSet = classElement.getModifiers();
        // 如果该类时 不未 Public的 则意味着访问权限可能有限
        if (!modifierSet.contains(Modifier.PUBLIC)) {
            context.logMessage(Diagnostic.Kind.ERROR,
                    "The class " + classElement.getQualifiedName().toString()
                            + " is not public.",
                    classElement);
            return null;
        }
        // 如果该类时 不未 是抽象类 则不可以进行注解
        if (modifierSet.contains(Modifier.ABSTRACT)) {
            context.logMessage(Diagnostic.Kind.ERROR, "The class " + classElement.getQualifiedName().toString()
                            + " is abstract. You can't annotate abstract classes with @" + Impl.class.getSimpleName(),
                    classElement);
            return null;
        }
        // 判断是否有一个 public 修饰 空的构造函数
        if (!TypeUtils.hasPublicEmptyDefaultConstructor(classElement)) {
            context.logMessage(Diagnostic.Kind.ERROR, "The class " + classElement.getQualifiedName().toString()
                            + " must provide an public empty default constructor",
                    classElement);
            return null;
        }
        MetaApi metaApi = new MetaApi(context, classElement);
        if (Object.class.getCanonicalName().equals(metaApi.getApiQualifiedName())) {
            return metaApi;
        }
        TypeElement superClassElement = metaApi.apiTypeElement; // 获取到实现的api接口

        if (!TypeUtils.isAssignable(context, classElement, superClassElement)) {
            // 无实现关系
            String superClassName = superClassElement.getQualifiedName().toString();
            if (ElementKind.INTERFACE.equals(superClassElement.getKind())) { // 如果是接口 必须要实现
                context.logMessage(Diagnostic.Kind.ERROR, "The class " + classElement.getQualifiedName().toString()
                                + " annotated with @" + Impl.class.getSimpleName()
                                + " must implement the interface " + superClassName,
                        classElement);
            } else {
                context.logMessage(Diagnostic.Kind.ERROR, "The class " + classElement.getQualifiedName().toString()
                                + " annotated with @" + Impl.class.getSimpleName()
                                + " must inherit from " + superClassName,
                        classElement);
            }
            return null;
        }
        return metaApi;
    }

    /**
     * Get at {@link Impl#api()} qualified name
     *
     * @return qualified name
     */
    public String getApiQualifiedName() {
        return apiQualifiedName;
    }

    public String getImplQualifiedName() {
        return implQualifiedName;
    }

    public boolean isApiImpl(String apiQualifiedName) {
        return this.apiQualifiedName.equals(apiQualifiedName);
    }

    public boolean isApiImpl(TypeElement api) {
        if (implTypeElement == null || apiTypeElement == null) {
            return apiQualifiedName.equals(api.getQualifiedName().toString());
        }
        TypeElement classElement = implTypeElement;
        if (!ElementKind.CLASS.equals(classElement.getKind())) {
            return false;
        }
        if (!Object.class.getCanonicalName().equals(getApiQualifiedName())) {
            classElement = apiTypeElement;
        }
        return TypeUtils.isAssignable(mContext, classElement, api);
    }
}
