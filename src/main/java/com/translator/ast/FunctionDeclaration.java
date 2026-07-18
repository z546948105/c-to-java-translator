package com.translator.ast;

import java.util.List;

/**
 * 函数声明节点，表示函数的定义或原型
 * <p>
 * 包含返回类型、函数名、参数列表和函数体
 * <p>
 * 如果 body 为 null，则表示函数原型（仅声明）
 */
public class FunctionDeclaration implements AstNode {
    private final Type returnType;
    private final Identifier name;
    private final List<VariableDeclaration> parameters;
    private final Block body;

    public FunctionDeclaration(Type returnType, Identifier name, List<VariableDeclaration> parameters, Block body) {
        this.returnType = returnType;
        this.name = name;
        this.parameters = parameters;
        this.body = body;
    }

    public Type getReturnType() {
        return returnType;
    }

    public Identifier getName() {
        return name;
    }

    public List<VariableDeclaration> getParameters() {
        return parameters;
    }

    public Block getBody() {
        return body;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitFunctionDeclaration(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(returnType + " " + name + "(");
        for (int i = 0; i < parameters.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(parameters.get(i).getType()).append(" ").append(parameters.get(i).getName());
        }
        sb.append(") ").append(body);
        return sb.toString();
    }
}