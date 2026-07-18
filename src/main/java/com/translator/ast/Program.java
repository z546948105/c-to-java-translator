package com.translator.ast;

import java.util.List;

public class Program implements AstNode {
    private final List<AstNode> declarations;

    public Program(List<AstNode> declarations) {
        this.declarations = declarations;
    }

    public List<AstNode> getDeclarations() {
        return declarations;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitProgram(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (AstNode decl : declarations) {
            sb.append(decl).append("\n");
        }
        return sb.toString();
    }
}