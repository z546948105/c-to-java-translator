package com.translator.ast;

public class ExpressionStatement implements AstNode {
    private final AstNode expression;

    public ExpressionStatement(AstNode expression) {
        this.expression = expression;
    }

    public AstNode getExpression() {
        return expression;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitExpressionStatement(this);
    }

    @Override
    public String toString() {
        return expression + ";";
    }
}