package com.translator.ast;

public class ForStatement implements AstNode {
    private final AstNode init;
    private final AstNode condition;
    private final AstNode update;
    private final Block body;

    public ForStatement(AstNode init, AstNode condition, AstNode update, Block body) {
        this.init = init;
        this.condition = condition;
        this.update = update;
        this.body = body;
    }

    public AstNode getInit() {
        return init;
    }

    public AstNode getCondition() {
        return condition;
    }

    public AstNode getUpdate() {
        return update;
    }

    public Block getBody() {
        return body;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitForStatement(this);
    }

    @Override
    public String toString() {
        return "for (" + init + "; " + condition + "; " + update + ") " + body;
    }
}