package com.translator.ast;

/**
 * break 语句节点，表示跳出循环或 switch
 */
public class BreakStatement implements AstNode {
    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitBreakStatement(this);
    }

    @Override
    public String toString() {
        return "break;";
    }
}