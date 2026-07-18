package com.translator.ast;

import java.util.List;

/**
 * 枚举声明节点，表示枚举类型的定义
 * <p>
 * 转换为 Java 时对应 enum 类型的定义
 */
public class EnumDeclaration implements AstNode {
    private final Identifier name;
    private final List<Identifier> values;

    public EnumDeclaration(Identifier name, List<Identifier> values) {
        this.name = name;
        this.values = values;
    }

    public Identifier getName() {
        return name;
    }

    public List<Identifier> getValues() {
        return values;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitEnumDeclaration(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("enum " + name + " {");
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(values.get(i));
        }
        sb.append("}");
        return sb.toString();
    }
}
