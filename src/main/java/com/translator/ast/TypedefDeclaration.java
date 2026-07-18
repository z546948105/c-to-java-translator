package com.translator.ast;

public class TypedefDeclaration implements AstNode {
    private final AstNode originalType;
    private final Identifier alias;

    public TypedefDeclaration(AstNode originalType, Identifier alias) {
        this.originalType = originalType;
        this.alias = alias;
    }

    public AstNode getOriginalType() {
        return originalType;
    }

    public Identifier getAlias() {
        return alias;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitTypedefDeclaration(this);
    }

    @Override
    public String toString() {
        return "typedef " + originalType + " " + alias + ";";
    }
}