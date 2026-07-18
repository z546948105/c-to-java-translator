package com.translator.ast;

import java.util.List;

/**
 * 宏定义节点，表示 #define 宏声明
 * <p>
 * 支持对象宏（无参数）和函数宏（有参数）
 * <p>
 * 函数宏转换为 Java 静态方法
 */
public class MacroDeclaration implements AstNode {
    private final Identifier name;
    private final List<Identifier> parameters;
    private final AstNode body;

    public MacroDeclaration(Identifier name, List<Identifier> parameters, AstNode body) {
        this.name = name;
        this.parameters = parameters;
        this.body = body;
    }

    public Identifier getName() {
        return name;
    }

    public List<Identifier> getParameters() {
        return parameters;
    }

    public AstNode getBody() {
        return body;
    }

    public boolean isFunctionMacro() {
        return parameters != null && !parameters.isEmpty();
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitMacroDeclaration(this);
    }
}
