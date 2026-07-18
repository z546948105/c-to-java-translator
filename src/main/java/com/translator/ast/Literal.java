package com.translator.ast;

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