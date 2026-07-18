package com.translator.ast;

/**
 * AST（抽象语法树）节点的通用接口
 * <p>
 * 所有 AST 节点都必须实现此接口，用于支持访问者模式
 * <p>
 * 访问者模式允许在不修改节点类的情况下对 AST 进行不同的操作
 * （如代码生成、类型检查、优化等）
 */
public interface AstNode {
    <T> T accept(AstVisitor<T> visitor);
}