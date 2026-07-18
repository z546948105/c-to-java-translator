package com.translator.ast;

/**
 * continue 语句节点，表示跳过循环的剩余部分，进入下一次迭代
 */
public class ContinueStatement implements AstNode {
    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitContinueStatement(this);
    }

    @Override
    public String toString() {
        return "continue;";
    }
}