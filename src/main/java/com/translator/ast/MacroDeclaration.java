package com.translator.ast;

import java.util.List;

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
