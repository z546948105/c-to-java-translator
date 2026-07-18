package com.translator.ast;

public class ReturnStatement implements AstNode {
    private final AstNode expression;

    public ReturnStatement() {
        this(null);
    }

    public ReturnStatement(AstNode expression) {
        this.expression = expression;
    }

    public AstNode getExpression() {
        return expression;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitReturnStatement(this);
    }

    @Override
    public String toString() {
        if (expression != null) {
            return "return " + expression + ";";
        }
        return "return;";
    }
}