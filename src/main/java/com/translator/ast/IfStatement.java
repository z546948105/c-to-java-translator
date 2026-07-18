package com.translator.ast;

/**
 * if 语句节点，表示条件分支语句
 * <p>
 * 支持带 else 分支和不带 else 分支两种形式
 */
public class IfStatement implements AstNode {
    private final AstNode condition;
    private final Block thenBlock;
    private final Block elseBlock;

    public IfStatement(AstNode condition, Block thenBlock) {
        this(condition, thenBlock, null);
    }

    public IfStatement(AstNode condition, Block thenBlock, Block elseBlock) {
        this.condition = condition;
        this.thenBlock = thenBlock;
        this.elseBlock = elseBlock;
    }

    public AstNode getCondition() {
        return condition;
    }

    public Block getThenBlock() {
        return thenBlock;
    }

    public Block getElseBlock() {
        return elseBlock;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitIfStatement(this);
    }

    @Override
    public String toString() {
        if (elseBlock != null) {
            return "if (" + condition + ") " + thenBlock + " else " + elseBlock;
        }
        return "if (" + condition + ") " + thenBlock;
    }
}