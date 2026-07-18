package com.translator.ast;

public class StructAccess implements AstNode {
    private final AstNode struct;
    private final Identifier field;

    public StructAccess(AstNode struct, Identifier field) {
        this.struct = struct;
        this.field = field;
    }

    public AstNode getStruct() {
        return struct;
    }

    public Identifier getField() {
        return field;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitStructAccess(this);
    }

    @Override
    public String toString() {
        return struct + "." + field;
    }
}