package com.translator.ast;

public class Comment implements AstNode {
    private final String text;
    
    public Comment(String text) {
        this.text = text;
    }
    
    public String getText() {
        return text;
    }
    
    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitComment(this);
    }
}
