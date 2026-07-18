package com.translator.ast;

/**
 * 赋值表达式节点，表示变量赋值操作
 * <p>
 * 支持简单赋值（=）和复合赋值（+= -= *= /= %= 等）
 */
public class Assignment implements AstNode {
    private final AstNode left;
    private final String operator;
    private final AstNode right;

    public Assignment(AstNode left, AstNode right) {
        this(left, "=", right);
    }

    public Assignment(AstNode left, String operator, AstNode right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    public AstNode getLeft() {
        return left;
    }

    public String getOperator() {
        return operator;
    }

    public AstNode getRight() {
        return right;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitAssignment(this);
    }

    @Override
    public String toString() {
        return left + " " + operator + " " + right;
    }
}