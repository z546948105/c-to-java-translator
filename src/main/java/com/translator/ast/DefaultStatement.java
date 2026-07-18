package com.translator.ast;

import java.util.List;

public class DefaultStatement implements AstNode {
    private final List<AstNode> statements;

    public DefaultStatement(List<AstNode> statements) {
        this.statements = statements;
    }

    public List<AstNode> getStatements() {
        return statements;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitDefaultStatement(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("default:");
        for (AstNode stmt : statements) {
            sb.append("\n        ").append(stmt);
        }
        return sb.toString();
    }
}