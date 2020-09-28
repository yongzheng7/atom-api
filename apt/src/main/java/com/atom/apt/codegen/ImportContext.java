package com.atom.apt.codegen;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by HYW on 2017/5/25.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class ImportContext {

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
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

    private final Set<String> imports = new TreeSet<>();
    private final Set<String> staticImports = new TreeSet<>();
    private final Map<String, String> simpleNames = new HashMap<>();
    private final String basePackage;

    public ImportContext(String basePackage) {
        this.basePackage = basePackage;
    }

    public static String unqualify(String qualifiedName) {
        int loc = qualifiedName.lastIndexOf('.');
        return (loc < 0) ? qualifiedName : qualifiedName.substring(qualifiedName.lastIndexOf('.') + 1);
    }


    public static String qualifier(String qualifiedName) {
        int loc = qualifiedName.lastIndexOf(".");
        return (loc < 0) ? "" : qualifiedName.substring(0, loc);
    }

    /**
     * <pre>
     * Add fqcn to the import list. Returns fqcn as needed in source code.
     * Attempts to handle fqcn with array and generics references.
     * e.g.
     * java.util.Collection&lt;org.marvel.Hulk&gt; imports java.util.Collection and returns Collection
     * org.marvel.Hulk[] imports org.marvel.Hulk and returns Hulk
     * </pre>
     *
     * @param fqcn Fully qualified class name
     * @return import string
     */
    public String importType(String fqcn) {
        String result = fqcn;

        //if(fqcn==null) return "/** (null) **/";

        String additionalTypePart = null;
        if (fqcn.indexOf('<') >= 0) {
            additionalTypePart = result.substring(fqcn.indexOf('<'));
            result = result.substring(0, fqcn.indexOf('<'));
            fqcn = result;
        } else if (fqcn.indexOf('[') >= 0) {
            additionalTypePart = result.substring(fqcn.indexOf('['));
            result = result.substring(0, fqcn.indexOf('['));
            fqcn = result;
        }

        String pureFqcn = fqcn.replace('$', '.');

        boolean canBeSimple;

        String simpleName = unqualify(fqcn);
        if (simpleNames.containsKey(simpleName)) {
            String existingFqcn = simpleNames.get(simpleName);
            canBeSimple = existingFqcn.equals(pureFqcn);
        } else {
            canBeSimple = true;
            simpleNames.put(simpleName, pureFqcn);
            imports.add(pureFqcn);
        }

        if (inSamePackage(fqcn) || (imports.contains(pureFqcn) && canBeSimple)) {
            result = unqualify(result);
        } else if (inJavaLang(fqcn)) {
            result = result.substring("java.lang.".length());
        }

        if (additionalTypePart != null) {
            result = result + additionalTypePart;
        }

        result = result.replace('$', '.');
        return result;
    }

    public String staticImport(String fqcn, String member) {
        String local = fqcn + "." + member;
        imports.add(local);
        staticImports.add(local);

        if (member.equals("*")) {
            return "";
        } else {
            return member;
        }
    }

    private boolean inDefaultPackage(String className) {
        return !className.contains(".");
    }

    private boolean isPrimitive(String className) {
        return PRIMITIVES.containsKey(className);
    }

    private boolean inSamePackage(String className) {
        String other = qualifier(className);
        return other.equals(basePackage);
    }

    private boolean inJavaLang(String className) {
        return "java.lang".equals(qualifier(className));
    }

    public String generateImports() {
        StringBuilder builder = new StringBuilder();

        for (String next : imports) {
            // don't add automatically "imported" stuff
            if (!isAutoImported(next)) {
                if (staticImports.contains(next)) {
                    builder.append("import static ").append(next).append(";").append(LINE_SEPARATOR);
                } else {
                    builder.append("import ").append(next).append(";").append(LINE_SEPARATOR);
                }
            }
        }

        if (builder.indexOf("$") >= 0) {
            return builder.toString();
        }
        return builder.toString();
    }

    private boolean isAutoImported(String next) {
        return isPrimitive(next) || inDefaultPackage(next) || inJavaLang(next) || inSamePackage(next);
    }
}
