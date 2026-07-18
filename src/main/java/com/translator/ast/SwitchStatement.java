package com.translator.ast;

import java.util.List;

public class SwitchStatement implements AstNode {
    private final AstNode expression;
    private final List<AstNode> cases;

    public SwitchStatement(AstNode expression, List<AstNode> cases) {
        this.expression = expression;
        this.cases = cases;
    }

    public AstNode getExpression() {
        return expression;
    }

    public List<AstNode> getCases() {
        return cases;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitSwitchStatement(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("switch (" + expression + ") {");
        for (AstNode caseStmt : cases) {
            sb.append("\n    ").append(caseStmt);
        }
        sb.append("\n}");
        return sb.toString();
    }
}