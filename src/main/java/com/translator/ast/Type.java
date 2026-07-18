package com.translator.ast;

/**
 * 类型节点，表示 C 语言中的数据类型
 * <p>
 * 支持：基础类型（int, float 等）、指针类型、数组类型
 * <p>
 * pointerLevel: 指针级别（0=非指针, 1=一级指针, 2=二级指针...）
 * isArray: 是否为数组类型
 * arraySize: 数组大小（可为 null，表示未知大小）
 */
public class Type implements AstNode {
    private final String name;
    private final int pointerLevel;
    private final boolean isArray;
    private final Integer arraySize;

    public Type(String name) {
        this(name, 0, false, null);
    }

    public Type(String name, int pointerLevel) {
        this(name, pointerLevel, false, null);
    }

    public Type(String name, boolean isArray, Integer arraySize) {
        this(name, 0, isArray, arraySize);
    }

    public Type(String name, int pointerLevel, boolean isArray, Integer arraySize) {
        this.name = name;
        this.pointerLevel = pointerLevel;
        this.isArray = isArray;
        this.arraySize = arraySize;
    }

    public String getName() {
        return name;
    }

    public int getPointerLevel() {
        return pointerLevel;
    }

    public boolean isArray() {
        return isArray;
    }

    public Integer getArraySize() {
        return arraySize;
    }

    public boolean isPointer() {
        return pointerLevel > 0;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitType(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(name);
        for (int i = 0; i < pointerLevel; i++) {
            sb.append("*");
        }
        if (isArray) {
            sb.append("[]");
            if (arraySize != null) {
                sb.setLength(sb.length() - 2);
                sb.append("[").append(arraySize).append("]");
            }
        }
        return sb.toString();
    }
}