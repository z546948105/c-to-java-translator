package com.translator.ast;

public class TypeCastExpression implements AstNode {
    private final Type type;
    private final AstNode expression;

    public TypeCastExpression(Type type, AstNode expression) {
        this.type = type;
        this.expression = expression;
    }

    public Type getType() {
        return type;
    }

    public AstNode getExpression() {
        return expression;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitTypeCastExpression(this);
    }
}