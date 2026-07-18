package com.translator.ast;

import java.util.List;

/**
 * 数组初始化节点，表示数组的初始化列表
 * <p>
 * 例如：{1, 2, 3}
 */
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