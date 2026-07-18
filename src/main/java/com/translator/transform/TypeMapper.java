package com.translator.transform;

import com.translator.ast.Type;

public class TypeMapper {
    public static String mapPrimitiveType(String cType) {
        if (cType.startsWith("struct ")) {
            return cType.substring("struct ".length());
        }
        if (cType.startsWith("enum ")) {
            return cType.substring("enum ".length());
        }
        if (cType.startsWith("union ")) {
            return cType.substring("union ".length());
        }
        if (cType.startsWith("const ")) {
            return mapPrimitiveType(cType.substring("const ".length()));
        }
        switch (cType) {
            case "int": return "int";
            case "long": return "long";
            case "short": return "short";
            case "char": return "char";
            case "float": return "float";
            case "double": return "double";
            case "void": return "void";
            case "bool": return "boolean";
            case "size_t": return "long";
            case "unsigned int": return "int";
            case "unsigned long": return "long";
            case "long long": return "long";
            case "unsigned long long": return "long";
            default: return cType;
        }
    }

    public static String mapType(Type type) {
        String baseType = mapPrimitiveType(type.getName());
        
        if (type.isArray()) {
            String elementType = baseType;
            if (type.isPointer()) {
                if (baseType.equals("void")) {
                    elementType = "Object";
                } else if (baseType.equals("char")) {
                    elementType = "String";
                } else if (type.getPointerLevel() == 1) {
                    elementType = baseType + "[]";
                } else {
                    elementType = "Object";
                }
            }
            if (type.getArraySize() != null) {
                return elementType + "[" + type.getArraySize() + "]";
            } else {
                return elementType + "[]";
            }
        }
        
        if (type.isPointer()) {
            if (baseType.equals("void")) {
                return "Object";
            }
            if (baseType.equals("char")) {
                return "String";
            }
            if (type.getPointerLevel() == 1) {
                return baseType + "[]";
            } else {
                return "Object";
            }
        }
        
        return baseType;
    }
}