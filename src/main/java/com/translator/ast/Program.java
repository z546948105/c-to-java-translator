package com.translator.ast;

import java.util.List;

/**
 * 程序根节点，代表整个 C 源文件
 * <p>
 * 包含所有顶层声明（函数、变量、结构体、枚举、宏定义等）
 */
public class Program implements AstNode {
    private final List<AstNode> declarations;

    public Program(List<AstNode> declarations) {
        this.declarations = declarations;
    }

    public List<AstNode> getDeclarations() {
        return declarations;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitProgram(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (AstNode decl : declarations) {
            sb.append(decl).append("\n");
        }
        return sb.toString();
    }
}