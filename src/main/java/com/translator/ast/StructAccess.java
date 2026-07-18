package com.translator.ast;

/**
 * 结构体成员访问节点，表示访问结构体或类的成员
 * <p>
 * 例如：struct.field
 */
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