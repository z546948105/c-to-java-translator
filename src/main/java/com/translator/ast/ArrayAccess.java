package com.translator.ast;

public class ArrayAccess implements AstNode {
    private final AstNode array;
    private final AstNode index;

    public ArrayAccess(AstNode array, AstNode index) {
        this.array = array;
        this.index = index;
    }

    public AstNode getArray() {
        return array;
    }

    public AstNode getIndex() {
        return index;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitArrayAccess(this);
    }

    @Override
    public String toString() {
        return array + "[" + index + "]";
    }
}