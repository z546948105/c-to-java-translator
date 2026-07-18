package com.translator.ast;

public class ContinueStatement implements AstNode {
    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitContinueStatement(this);
    }

    @Override
    public String toString() {
        return "continue;";
    }
}