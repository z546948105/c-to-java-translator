package com.translator.ast;

/**
 * return 语句节点，表示函数返回语句
 * <p>
 * 支持带返回值和不带返回值两种形式
 */
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