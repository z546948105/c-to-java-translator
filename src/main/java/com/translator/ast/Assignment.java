package com.translator.ast;

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