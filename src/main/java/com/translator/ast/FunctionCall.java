package com.translator.ast;

import java.util.List;

/**
 * 函数调用节点，表示函数或方法的调用
 * <p>
 * 包含函数名和参数列表
 */
public class FunctionCall implements AstNode {
    private final Identifier name;
    private final List<AstNode> arguments;

    public FunctionCall(Identifier name, List<AstNode> arguments) {
        this.name = name;
        this.arguments = arguments;
    }

    public Identifier getName() {
        return name;
    }

    public List<AstNode> getArguments() {
        return arguments;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitFunctionCall(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(name + "(");
        for (int i = 0; i < arguments.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(arguments.get(i));
        }
        sb.append(")");
        return sb.toString();
    }
}