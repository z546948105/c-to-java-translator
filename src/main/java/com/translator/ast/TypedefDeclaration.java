package com.translator.ast;

/**
 * typedef 声明节点，表示类型别名定义
 * <p>
 * 将一种类型映射到另一个名称
 */
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