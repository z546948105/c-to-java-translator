package com.translator.ast;

import java.util.List;

/**
 * 函数指针类型节点，表示 C 语言中的函数指针类型
 * <p>
 * 例如：int (*func)(int) 表示返回 int、接受一个 int 参数的函数指针
 * int (*funcs[5])(int) 表示函数指针数组
 * <p>
 * 返回类型和参数类型列表用于映射到 Java 函数式接口
 */
public class FunctionPointerType extends Type {
    private final Type returnType;
    private final List<Type> parameterTypes;
    private final boolean isArray;
    private final Integer arraySize;

    public FunctionPointerType(Type returnType, List<Type> parameterTypes) {
        this(returnType, parameterTypes, false, null);
    }

    public FunctionPointerType(Type returnType, List<Type> parameterTypes, boolean isArray, Integer arraySize) {
        super("function_pointer", 0, isArray, arraySize);
        this.returnType = returnType;
        this.parameterTypes = parameterTypes;
        this.isArray = isArray;
        this.arraySize = arraySize;
    }

    public Type getReturnType() {
        return returnType;
    }

    public List<Type> getParameterTypes() {
        return parameterTypes;
    }

    public int getParameterCount() {
        return parameterTypes != null ? parameterTypes.size() : 0;
    }

    public boolean isArray() {
        return isArray;
    }

    public Integer getArraySize() {
        return arraySize;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitFunctionPointerType(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(returnType).append(" (*)(");
        if (parameterTypes != null && !parameterTypes.isEmpty()) {
            for (int i = 0; i < parameterTypes.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(parameterTypes.get(i));
            }
        }
        sb.append(")");
        if (isArray) {
            sb.append("[");
            if (arraySize != null) {
                sb.append(arraySize);
            }
            sb.append("]");
        }
        return sb.toString();
    }
}
