package com.translator.ast;

/**
 * 一元表达式节点，表示带有一个操作数的表达式
 * <p>
 * 支持的运算符：++ -- + - ! ~ *（解引用） &（取地址）
 * <p>
 * postfix: 表示是否为后缀形式（如 i++ 而非 ++i）
 */
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