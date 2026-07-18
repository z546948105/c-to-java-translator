package com.translator.ast;

public class WhileStatement implements AstNode {
    private final AstNode condition;
    private final Block body;

    public WhileStatement(AstNode condition, Block body) {
        this.condition = condition;
        this.body = body;
    }

    public AstNode getCondition() {
        return condition;
    }

    public Block getBody() {
        return body;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitWhileStatement(this);
    }

    @Override
    public String toString() {
        return "while (" + condition + ") " + body;
    }
}