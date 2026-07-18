package com.translator.ast;

import java.util.List;

public class ArrayInitializer implements AstNode {
    private List<AstNode> elements;
    
    public ArrayInitializer(List<AstNode> elements) {
        this.elements = elements;
    }
    
    public List<AstNode> getElements() {
        return elements;
    }
    
    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitArrayInitializer(this);
    }
}