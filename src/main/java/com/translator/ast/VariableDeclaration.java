package com.translator.ast;

/**
 * 变量声明节点，表示变量的声明和可选的初始化
 * <p>
 * 支持：类型、名称、初始化表达式（可选）
 */
public class VariableDeclaration implements AstNode {
    private final Type type;
    private final Identifier name;
    private final AstNode initializer;

    public VariableDeclaration(Type type, Identifier name) {
        this(type, name, null);
    }

    public VariableDeclaration(Type type, Identifier name, AstNode initializer) {
        this.type = type;
        this.name = name;
        this.initializer = initializer;
    }

    public Type getType() {
        return type;
    }

    public Identifier getName() {
        return name;
    }

    public AstNode getInitializer() {
        return initializer;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitVariableDeclaration(this);
    }

    @Override
    public String toString() {
        if (initializer != null) {
            return type + " " + name + " = " + initializer + ";";
        }
        return type + " " + name + ";";
    }
}