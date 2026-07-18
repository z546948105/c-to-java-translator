package com.translator.ast;

/**
 * 表达式语句节点，表示以分号结尾的表达式
 * <p>
 * 例如：x = 5; 或 func();
 */
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