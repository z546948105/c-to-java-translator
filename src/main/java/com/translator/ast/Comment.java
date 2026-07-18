package com.translator.ast;

/**
 * 注释节点，表示代码中的注释
 */
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
