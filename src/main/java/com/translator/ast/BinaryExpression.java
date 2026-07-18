package com.translator.ast;

/**
 * 二元表达式节点，表示带有两个操作数的表达式
 * <p>
 * 支持的运算符：算术（+ - * / %）、比较（== != < > <= >=）、逻辑（&& ||）、位运算等
 */
public class BinaryExpression implements AstNode {
    private final AstNode left;
    private final String operator;
    private final AstNode right;

    public BinaryExpression(AstNode left, String operator, AstNode right) {
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
        return visitor.visitBinaryExpression(this);
    }

    @Override
    public String toString() {
        return "(" + left + " " + operator + " " + right + ")";
    }
}