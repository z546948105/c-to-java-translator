package com.translator.ast;

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