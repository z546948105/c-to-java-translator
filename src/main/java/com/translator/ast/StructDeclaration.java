package com.translator.ast;

import java.util.List;

/**
 * 结构体声明节点，表示结构体类型的定义
 * <p>
 * 转换为 Java 时对应类的定义
 */
public class StructDeclaration implements AstNode {
    private final Identifier name;
    private final List<VariableDeclaration> fields;

    public StructDeclaration(Identifier name, List<VariableDeclaration> fields) {
        this.name = name;
        this.fields = fields;
    }

    public Identifier getName() {
        return name;
    }

    public List<VariableDeclaration> getFields() {
        return fields;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitStructDeclaration(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("struct " + name + " {");
        for (VariableDeclaration field : fields) {
            sb.append("\n    ").append(field.getType()).append(" ").append(field.getName()).append(";");
        }
        sb.append("\n};");
        return sb.toString();
    }
}