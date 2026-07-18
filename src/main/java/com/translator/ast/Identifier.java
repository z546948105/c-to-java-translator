package com.translator.ast;

public class Identifier implements AstNode {
    private final String name;

    public Identifier(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitIdentifier(this);
    }

    @Override
    public String toString() {
        return name;
    }
}