package com.translator.ast;

/**
 * 标识符节点，表示变量名、函数名、类型名等
 * <p>
 * 在代码生成阶段，标识符直接转换为对应的 Java 标识符
 */
public class Identifier implements AstNode {
    private final String name;

    public Identifier(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitIdentifier(this);
    }

    @Override
    public String toString() {
        return name;
    }
}