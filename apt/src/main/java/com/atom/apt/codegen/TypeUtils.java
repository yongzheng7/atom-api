package com.atom.apt.codegen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

/**
 * Created by HYW on 2017/5/25.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class TypeUtils {

    public static final String DEFAULT_ANNOTATION_PARAMETER_NAME = "value";
    private static final Map<String, String> PRIMITIVES = new HashMap<>();

    static {
        PRIMITIVES.put("char", "Character");

        PRIMITIVES.put("byte", "Byte");
        PRIMITIVES.put("short", "Short");
        PRIMITIVES.put("int", "Integer");
        PRIMITIVES.put("long", "Long");

        PRIMITIVES.put("boolean", "Boolean");

        PRIMITIVES.put("float", "Float");
        PRIMITIVES.put("double", "Double");
    }

    private TypeUtils() {
    }

    public static String toTypeString(TypeMirror type) {
        if (type.getKind().isPrimitive()) {
            return PRIMITIVES.get(type.toString());
        }
        return type.toString();
    }

    public static TypeElement getSuperclassTypeElement(TypeElement element) {
        final TypeMirror superClass = element.getSuperclass();
        //superclass of Object is of NoType which returns some other kind
        if (superClass.getKind() == TypeKind.DECLARED) {
            //F..king Ch...t Have those people used their horrible APIs even once?
            final Element superClassElement = ((DeclaredType) superClass).asElement();
            return (TypeElement) superClassElement;
        } else {
            return null;
        }
    }

    public static String extractClosestRealTypeAsString(TypeMirror type, Context context) {
        if (type instanceof TypeVariable) {
            final TypeMirror compositeUpperBound = ((TypeVariable) type).getUpperBound();
            return extractClosestRealTypeAsString(compositeUpperBound, context);
        } else {
            return context.getTypeUtils().erasure(type).toString();
        }
    }

    public static boolean containsAnnotation(Element element, String... annotations) {
        assert element != null;
        assert annotations != null;

        List<String> annotationClassNames = new ArrayList<>();
        Collections.addAll(annotationClassNames, annotations);

        List<? extends AnnotationMirror> annotationMirrors = element.getAnnotationMirrors();
        for (AnnotationMirror mirror : annotationMirrors) {
            if (annotationClassNames.contains(mirror.getAnnotationType().toString())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns {@code true} if the provided annotation type is of the same type as the provided class, {@code false} otherwise.
     * This method uses the string class names for comparison. See also
     * <a href="http://www.retep.org/2009/02/getting-class-values-from-annotations.html">getting-class-values-from-annotations</a>.
     *
     * @param annotationMirror The annotation mirror
     * @param fqcn             the fully qualified class name to check against
     * @return {@code true} if the provided annotation type is of the same type as the provided class, {@code false} otherwise.
     */
    public static boolean isAnnotationMirrorOfType(AnnotationMirror annotationMirror, String fqcn) {
        assert annotationMirror != null;
        assert fqcn != null;
        String annotationClassName = annotationMirror.getAnnotationType().toString();

        return annotationClassName.equals(fqcn);
    }

    /**
     * Checks whether the {@code Element} hosts the annotation with the given fully qualified class name.
     *
     * @param element the element to check for the hosted annotation
     * @param fqcn    the fully qualified class name of the annotation to check for
     * @return the annotation mirror for the specified annotation class from the {@code Element} or {@code null} in case
     * the {@code TypeElement} does not host the specified annotation.
     */
    public static AnnotationMirror getAnnotationMirror(Element element, String fqcn) {
        assert element != null;
        assert fqcn != null;

        AnnotationMirror mirror = null;
        for (AnnotationMirror am : element.getAnnotationMirrors()) {
            if (isAnnotationMirrorOfType(am, fqcn)) {
                mirror = am;
                break;
            }
        }
        return mirror;
    }

    public static Object getAnnotationValue(AnnotationMirror annotationMirror, String parameterValue) {
        assert annotationMirror != null;
        assert parameterValue != null;

        Object returnValue = null;
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotationMirror.getElementValues().entrySet()) {
            if (parameterValue.equals(entry.getKey().getSimpleName().toString())) {
                returnValue = entry.getValue().getValue();
                break;
            }
        }
        return returnValue;
    }

    public static TypeMirror getCollectionElementType(DeclaredType t, String fqNameOfReturnedType, String explicitTargetEntityName, Context context) {
        TypeMirror collectionElementType;
        if (explicitTargetEntityName != null) {
            Elements elements = context.getElementUtils();
            TypeElement element = elements.getTypeElement(explicitTargetEntityName);
            collectionElementType = element.asType();
        } else {
            List<? extends TypeMirror> typeArguments = t.getTypeArguments();
            if (typeArguments.size() == 0) {
                throw new RuntimeException("Unable to determine collection type");
            } else if (Map.class.getCanonicalName().equals(fqNameOfReturnedType)) {
                collectionElementType = t.getTypeArguments().get(1);
            } else {
                collectionElementType = t.getTypeArguments().get(0);
            }
        }
        return collectionElementType;
    }

    public static String getKeyType(DeclaredType t, Context context) {
        List<? extends TypeMirror> typeArguments = t.getTypeArguments();
        if (typeArguments.size() == 0) {
            context.logMessage(Diagnostic.Kind.ERROR, "Unable to determine type argument for " + t);
        }
        return extractClosestRealTypeAsString(typeArguments.get(0), context);
    }

    public static boolean hasPublicEmptyDefaultConstructor(TypeElement classElement) {
        for (Element enclosed : classElement.getEnclosedElements()) {
            if (enclosed.getKind() == ElementKind.CONSTRUCTOR) { // 判断是构造函数
                ExecutableElement constructorElement = (ExecutableElement) enclosed; // 强转
                if (constructorElement.getParameters().size() == 0  // 构造函数的 形参 = 0
                        && constructorElement.getModifiers().contains(Modifier.PUBLIC)// 构造函数的 public 修饰
                ) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isAssignable(Context context, TypeElement classElement, TypeMirror superClassTypeMirror) {
        if (classElement.getQualifiedName().toString().equals(superClassTypeMirror.toString())) {
            return true;
        }
        while (true) {
            List<? extends TypeMirror> interfaces = classElement.getInterfaces(); // 当前的类的实现的接口集合
            // context.logMessage(Diagnostic.Kind.NOTE, "The class " + classElement.getQualifiedName().toString());
            if (interfaces.contains(superClassTypeMirror)) { // 当前类实现该接口
                return true;
            }
            for (TypeMirror typeMirror : interfaces) { // 遍历所有接口
                // 将自身实现的接口转为TypeElement 在此进行判断
                if (isAssignable(context, (TypeElement) context.getTypeUtils().asElement(typeMirror), superClassTypeMirror)) {
                    return true;
                }
            }
            // 获取父类
            TypeMirror superClassType = classElement.getSuperclass();
            if (superClassType.getKind() == TypeKind.NONE) { // 父类为空
                return false;
            }
            // 将父类进行替换为当前的类 class 再次进行判断 直到判断有实现关系 即可
            classElement = (TypeElement) context.getTypeUtils().asElement(superClassType);
        }
    }

    public static boolean isAssignable(Context context, TypeElement classElement, TypeElement superClassElement) {
        String currClassQualifiedName = classElement.getQualifiedName().toString();
        String superClassQualifiedName = superClassElement.getQualifiedName().toString();
        context.logMessage(Diagnostic.Kind.NOTE, "isAssignable curr = " + currClassQualifiedName + "   super = "+superClassQualifiedName);
        if (currClassQualifiedName.equals(superClassQualifiedName)) {
            return true;
        }
        if (ElementKind.INTERFACE.equals(superClassElement.getKind())) {
            return isAssignable(context, classElement, superClassElement.asType());
        } else {
            // check subclass
            TypeElement currentClass = classElement;
            String superClassName = superClassElement.getQualifiedName().toString();
             context.logMessage(Diagnostic.Kind.NOTE, "The class " + classElement.getQualifiedName().toString());
            while (true) {
                TypeMirror superClassType = currentClass.getSuperclass();
                 context.logMessage(Diagnostic.Kind.NOTE, "The class " + superClassType.toString());
                if (superClassType.getKind() == TypeKind.NONE) {
                    return false;
                }
                if (superClassType.toString().equals(superClassName)) {
                    return true;
                }
                currentClass = (TypeElement) context.getTypeUtils().asElement(superClassType);
            }
        }
    }
}
