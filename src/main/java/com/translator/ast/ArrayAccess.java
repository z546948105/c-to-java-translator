package com.translator.ast;

/**
 * 数组访问节点，表示数组元素的访问操作
 * <p>
 * 例如：arr[index]
 */
public class ArrayAccess implements AstNode {
    private final AstNode array;
    private final AstNode index;

    public ArrayAccess(AstNode array, AstNode index) {
        this.array = array;
        this.index = index;
    }

    public AstNode getArray() {
        return array;
    }

    public AstNode getIndex() {
        return index;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitArrayAccess(this);
    }

    @Override
    public String toString() {
        return array + "[" + index + "]";
    }
}