package com.translator.transform;

import com.translator.ast.Type;

/**
 * 类型映射器（TypeMapper）
 * <p>
 * 将 C 语言类型映射为 Java 语言类型
 * <p>
 * 映射规则：
 * - 基础类型：int → int, float → float, char → char
 * - 指针类型：int* → int[], int** → int[][], char* → String, void* → Object
 * - 数组类型：保留数组维度
 * - 结构体/枚举：去掉前缀
 */
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
            StringBuilder result = new StringBuilder(baseType);
            for (int i = 0; i < type.getPointerLevel(); i++) {
                result.append("[]");
            }
            return result.toString();
        }
        
        return baseType;
    }
}