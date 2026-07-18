package com.translator.ast;

/**
 * 类型转换表达式节点，表示显式类型转换
 * <p>
 * 例如：(int) value
 */
public class TypeCastExpression implements AstNode {
    private final Type type;
    private final AstNode expression;

    public TypeCastExpression(Type type, AstNode expression) {
        this.type = type;
        this.expression = expression;
    }

    public Type getType() {
        return type;
    }

    public AstNode getExpression() {
        return expression;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitTypeCastExpression(this);
    }
}