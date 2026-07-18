package com.translator.ast;

public class DoWhileStatement implements AstNode {
    private final Block body;
    private final AstNode condition;

    public DoWhileStatement(Block body, AstNode condition) {
        this.body = body;
        this.condition = condition;
    }

    public Block getBody() {
        return body;
    }

    public AstNode getCondition() {
        return condition;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitDoWhileStatement(this);
    }

    @Override
    public String toString() {
        return "do " + body + " while (" + condition + ");";
    }
}