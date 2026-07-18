package com.translator.ast;

import java.util.List;

/**
 * case 语句节点，表示 switch 语句中的一个分支
 */
public class CaseStatement implements AstNode {
    private final AstNode value;
    private final List<AstNode> statements;

    public CaseStatement(AstNode value, List<AstNode> statements) {
        this.value = value;
        this.statements = statements;
    }

    public AstNode getValue() {
        return value;
    }

    public List<AstNode> getStatements() {
        return statements;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitCaseStatement(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("case " + value + ":");
        for (AstNode stmt : statements) {
            sb.append("\n        ").append(stmt);
        }
        return sb.toString();
    }
}