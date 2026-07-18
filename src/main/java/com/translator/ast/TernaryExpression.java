package com.translator.ast;

/**
 * 三元表达式节点，表示条件表达式（三目运算符）
 * <p>
 * 例如：condition ? trueExpr : falseExpr
 */
public class TernaryExpression implements AstNode {
    private final AstNode condition;
    private final AstNode trueExpression;
    private final AstNode falseExpression;

    public TernaryExpression(AstNode condition, AstNode trueExpression, AstNode falseExpression) {
        this.condition = condition;
        this.trueExpression = trueExpression;
        this.falseExpression = falseExpression;
    }

    public AstNode getCondition() {
        return condition;
    }

    public AstNode getTrueExpression() {
        return trueExpression;
    }

    public AstNode getFalseExpression() {
        return falseExpression;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitTernaryExpression(this);
    }

    @Override
    public String toString() {
        return condition + " ? " + trueExpression + " : " + falseExpression;
    }
}