package com.translator.ast;

import java.util.List;

/**
 * 代码块节点，表示一组语句的集合
 * <p>
 * 对应 C 语言中的 {} 包裹的语句块
 */
public class Block implements AstNode {
    private final List<AstNode> statements;

    public Block(List<AstNode> statements) {
        this.statements = statements;
    }

    public List<AstNode> getStatements() {
        return statements;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitBlock(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{");
        for (AstNode stmt : statements) {
            sb.append("\n    ").append(stmt);
        }
        sb.append("\n}");
        return sb.toString();
    }
}