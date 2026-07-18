package com.translator.ast;

public class BreakStatement implements AstNode {
    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitBreakStatement(this);
    }

    @Override
    public String toString() {
        return "break;";
    }
}