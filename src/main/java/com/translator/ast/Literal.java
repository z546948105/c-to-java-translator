package com.translator.ast;

/**
 * 字面量节点，表示常量值
 * <p>
 * 支持的类型：INTEGER（整数）、FLOAT（浮点数）、STRING（字符串）、CHARACTER（字符）、NULL（空指针）
 */
public class Literal implements AstNode {
    private final String value;
    private final LiteralType type;

    public enum LiteralType {
        INTEGER,
        FLOAT,
        STRING,
        CHARACTER,
        NULL
    }

    public Literal(String value, LiteralType type) {
        this.value = value;
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public LiteralType getType() {
        return type;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitLiteral(this);
    }

    @Override
    public String toString() {
        if (type == LiteralType.STRING) {
            return "\"" + value + "\"";
        }
        return value;
    }
}