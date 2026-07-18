package com.translator.ast;

public class UnaryExpression implements AstNode {
    private final String operator;
    private final AstNode operand;
    private final boolean postfix;

    public UnaryExpression(String operator, AstNode operand) {
        this(operator, operand, false);
    }

    public UnaryExpression(String operator, AstNode operand, boolean postfix) {
        this.operator = operator;
        this.operand = operand;
        this.postfix = postfix;
    }

    public String getOperator() {
        return operator;
    }

    public AstNode getOperand() {
        return operand;
    }

    public boolean isPostfix() {
        return postfix;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitUnaryExpression(this);
    }

    @Override
    public String toString() {
        if (postfix) {
            return operand + operator;
        }
        return operator + operand;
    }
}