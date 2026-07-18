package com.translator.codegen;

import com.translator.ast.*;

public class CodeGenerator implements AstVisitor<String> {
    private static final String INDENT = "    ";
    private int indentLevel = 0;

    @Override
    public String visitProgram(Program node) {
        StringBuilder sb = new StringBuilder();
        for (AstNode decl : node.getDeclarations()) {
            sb.append(decl.accept(this)).append("\n\n");
        }
        return sb.toString().trim();
    }

    @Override
    public String visitType(Type node) {
        return node.getName();
    }

    @Override
    public String visitVariableDeclaration(VariableDeclaration node) {
        StringBuilder sb = new StringBuilder();
        String typeName = node.getType().accept(this);
        sb.append(typeName)
          .append(" ")
          .append(node.getName().accept(this));
        if (node.getInitializer() != null) {
            String init = node.getInitializer().accept(this);
            if (typeName.contains("[]") && !init.contains("{")) {
                String elementType = typeName.substring(0, typeName.indexOf("["));
                sb.append(" = new ").append(elementType).append("[]{").append(init).append("}");
            } else {
                sb.append(" = ").append(init);
            }
        }
        sb.append(";");
        return sb.toString();
    }

    @Override
    public String visitFunctionDeclaration(FunctionDeclaration node) {
        StringBuilder sb = new StringBuilder();
        
        String returnType = node.getReturnType().accept(this);
        String name = node.getName().accept(this);
        
        if (returnType.equals("public class")) {
            sb.append(returnType).append(" ").append(name).append(" {\n");
            indentLevel++;
            for (AstNode stmt : node.getBody().getStatements()) {
                sb.append(getIndent()).append(stmt.accept(this)).append("\n");
            }
            indentLevel--;
            sb.append("}");
        } else {
            sb.append(returnType).append(" ").append(name).append("(");
            for (int i = 0; i < node.getParameters().size(); i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                VariableDeclaration param = node.getParameters().get(i);
                sb.append(param.getType().accept(this))
                  .append(" ")
                  .append(param.getName().accept(this));
            }
            sb.append(")");
            if (node.getBody() == null) {
                sb.append(";");
            } else {
                sb.append(" {\n");
                indentLevel++;
                for (AstNode stmt : node.getBody().getStatements()) {
                    sb.append(getIndent()).append(stmt.accept(this)).append("\n");
                }
                indentLevel--;
                sb.append(getIndent()).append("}");
            }
        }
        
        return sb.toString();
    }

    @Override
    public String visitStructDeclaration(StructDeclaration node) {
        StringBuilder sb = new StringBuilder("public class ").append(node.getName().accept(this)).append(" {\n");
        indentLevel++;
        for (VariableDeclaration field : node.getFields()) {
            sb.append(getIndent()).append(field.accept(this)).append("\n");
        }
        indentLevel--;
        sb.append("}");
        return sb.toString();
    }

    @Override
    public String visitTypedefDeclaration(TypedefDeclaration node) {
        if (node.getOriginalType() instanceof StructDeclaration) {
            return ((StructDeclaration) node.getOriginalType()).accept(this);
        }
        if (node.getOriginalType() instanceof EnumDeclaration) {
            return ((EnumDeclaration) node.getOriginalType()).accept(this);
        }
        return "// typedef " + node.getAlias().accept(this) + " -> " + node.getOriginalType().accept(this);
    }

    @Override
    public String visitBlock(Block node) {
        StringBuilder sb = new StringBuilder("{");
        if (!node.getStatements().isEmpty()) {
            sb.append("\n");
            indentLevel++;
            for (AstNode stmt : node.getStatements()) {
                sb.append(getIndent()).append(stmt.accept(this)).append("\n");
            }
            indentLevel--;
            sb.append(getIndent());
        }
        sb.append("}");
        return sb.toString();
    }

    @Override
    public String visitIfStatement(IfStatement node) {
        StringBuilder sb = new StringBuilder();
        sb.append("if (").append(node.getCondition().accept(this)).append(") ")
          .append(node.getThenBlock().accept(this));
        if (node.getElseBlock() != null) {
            sb.append(" else ").append(node.getElseBlock().accept(this));
        }
        return sb.toString();
    }

    @Override
    public String visitWhileStatement(WhileStatement node) {
        return "while (" + node.getCondition().accept(this) + ") " + node.getBody().accept(this);
    }

    @Override
    public String visitForStatement(ForStatement node) {
        StringBuilder sb = new StringBuilder("for (");
        if (node.getInit() != null) {
            if (node.getInit() instanceof VariableDeclaration) {
                VariableDeclaration vd = (VariableDeclaration) node.getInit();
                sb.append(vd.getType().accept(this))
                  .append(" ")
                  .append(vd.getName().accept(this));
                if (vd.getInitializer() != null) {
                    sb.append(" = ").append(vd.getInitializer().accept(this));
                }
            } else {
                sb.append(node.getInit().accept(this));
            }
        }
        sb.append("; ");
        if (node.getCondition() != null) {
            sb.append(node.getCondition().accept(this));
        }
        sb.append("; ");
        if (node.getUpdate() != null) {
            sb.append(node.getUpdate().accept(this));
        }
        sb.append(") ").append(node.getBody().accept(this));
        return sb.toString();
    }

    @Override
    public String visitDoWhileStatement(DoWhileStatement node) {
        return "do " + node.getBody().accept(this) + " while (" + node.getCondition().accept(this) + ");";
    }

    @Override
    public String visitSwitchStatement(SwitchStatement node) {
        StringBuilder sb = new StringBuilder("switch (").append(node.getExpression().accept(this)).append(") {\n");
        indentLevel++;
        for (AstNode caseStmt : node.getCases()) {
            sb.append(getIndent()).append(caseStmt.accept(this)).append("\n");
        }
        indentLevel--;
        sb.append("}");
        return sb.toString();
    }

    @Override
    public String visitCaseStatement(CaseStatement node) {
        StringBuilder sb = new StringBuilder("case ").append(node.getValue().accept(this)).append(":");
        if (!node.getStatements().isEmpty()) {
            sb.append("\n");
            indentLevel++;
            for (AstNode stmt : node.getStatements()) {
                sb.append(getIndent()).append(stmt.accept(this)).append("\n");
            }
            indentLevel--;
            // 检查最后一个语句是否已经是 break
            AstNode lastStmt = node.getStatements().get(node.getStatements().size() - 1);
            if (!(lastStmt instanceof BreakStatement)) {
                sb.append(getIndent()).append("break;");
            }
        }
        return sb.toString();
    }

    @Override
    public String visitDefaultStatement(DefaultStatement node) {
        StringBuilder sb = new StringBuilder("default:");
        if (!node.getStatements().isEmpty()) {
            sb.append("\n");
            indentLevel++;
            for (AstNode stmt : node.getStatements()) {
                sb.append(getIndent()).append(stmt.accept(this)).append("\n");
            }
            indentLevel--;
            // 检查最后一个语句是否已经是 break
            AstNode lastStmt = node.getStatements().get(node.getStatements().size() - 1);
            if (!(lastStmt instanceof BreakStatement)) {
                sb.append(getIndent()).append("break;");
            }
        }
        return sb.toString();
    }

    @Override
    public String visitBreakStatement(BreakStatement node) {
        return "break;";
    }

    @Override
    public String visitContinueStatement(ContinueStatement node) {
        return "continue;";
    }

    @Override
    public String visitReturnStatement(ReturnStatement node) {
        StringBuilder sb = new StringBuilder("return");
        if (node.getExpression() != null) {
            sb.append(" ").append(node.getExpression().accept(this));
        }
        sb.append(";");
        return sb.toString();
    }

    @Override
    public String visitExpressionStatement(ExpressionStatement node) {
        return node.getExpression().accept(this) + ";";
    }

    @Override
    public String visitAssignment(Assignment node) {
        return node.getLeft().accept(this) + " " + node.getOperator() + " " + node.getRight().accept(this);
    }

    @Override
    public String visitBinaryExpression(BinaryExpression node) {
        String operator = node.getOperator();
        // 处理字符串比较：AstTransformer 将字符串的 == 和 != 转换为 equals 和 notequals
        if (operator.equals("equals")) {
            return node.getLeft().accept(this) + ".equals(" + node.getRight().accept(this) + ")";
        }
        if (operator.equals("notequals")) {
            return "!" + node.getLeft().accept(this) + ".equals(" + node.getRight().accept(this) + ")";
        }
        return "(" + node.getLeft().accept(this) + " " + operator + " " + node.getRight().accept(this) + ")";
    }

    @Override
    public String visitUnaryExpression(UnaryExpression node) {
        if (node.isPostfix()) {
            return node.getOperand().accept(this) + node.getOperator();
        }
        String operator = node.getOperator();
        String operand = node.getOperand().accept(this);
        // 指针解引用 *ptr → ptr[0]
        if (operator.equals("*")) {
            return operand + "[0]";
        }
        // 取地址 &a → 保留原样，由 AstTransformer 处理
        if (operator.equals("&")) {
            return operand;
        }
        return operator + operand;
    }

    @Override
    public String visitIdentifier(Identifier node) {
        return node.getName();
    }

    @Override
    public String visitLiteral(Literal node) {
        switch (node.getType()) {
            case STRING:
                return "\"" + escapeString(node.getValue()) + "\"";
            case CHARACTER:
                return "'" + escapeChar(node.getValue()) + "'";
            default:
                return node.getValue();
        }
    }

    @Override
    public String visitArrayAccess(ArrayAccess node) {
        return node.getArray().accept(this) + "[" + node.getIndex().accept(this) + "]";
    }

    @Override
    public String visitFunctionCall(FunctionCall node) {
        StringBuilder sb = new StringBuilder();
        String name = node.getName().accept(this);
        
        if (name.equals("sizeof")) {
            String arg = node.getArguments().get(0).accept(this);
            if (arg.contains("[")) {
                return "1";
            }
            return arg + ".length";
        }
        
        if (name.equals("printf")) {
            StringBuilder formatSb = new StringBuilder("System.out.printf(");
            if (!node.getArguments().isEmpty()) {
                String formatStr = node.getArguments().get(0).accept(this);
                formatStr = formatStr.replace("%p", "%s");
                formatSb.append(formatStr);
                for (int i = 1; i < node.getArguments().size(); i++) {
                    formatSb.append(", ");
                    String arg = node.getArguments().get(i).accept(this);
                    if (arg.contains("[]")) {
                        formatSb.append("\"[array]\"");
                    } else {
                        formatSb.append(arg);
                    }
                }
            }
            formatSb.append(")");
            return formatSb.toString();
        }
        
        if (name.contains(".")) {
            sb.append(name);
        } else {
            sb.append(name);
        }
        
        sb.append("(");
        for (int i = 0; i < node.getArguments().size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(node.getArguments().get(i).accept(this));
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public String visitStructAccess(StructAccess node) {
        return node.getStruct().accept(this) + "." + node.getField().accept(this);
    }

    @Override
    public String visitTypeCastExpression(TypeCastExpression node) {
        return node.getExpression().accept(this);
    }

    @Override
    public String visitTernaryExpression(TernaryExpression node) {
        return node.getCondition().accept(this) + " ? " + 
               node.getTrueExpression().accept(this) + " : " + 
               node.getFalseExpression().accept(this);
    }
    
    @Override
    public String visitArrayInitializer(ArrayInitializer node) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        boolean first = true;
        for (AstNode element : node.getElements()) {
            if (!first) {
                sb.append(", ");
            }
            sb.append(element.accept(this));
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }

    @Override
    public String visitEnumDeclaration(EnumDeclaration node) {
        StringBuilder sb = new StringBuilder("public enum ");
        sb.append(node.getName() != null ? node.getName().getName() : "AnonymousEnum");
        sb.append(" {\n");
        indentLevel++;
        for (int i = 0; i < node.getValues().size(); i++) {
            sb.append(getIndent()).append(node.getValues().get(i).getName());
            if (i < node.getValues().size() - 1) {
                sb.append(",");
            }
            sb.append("\n");
        }
        indentLevel--;
        sb.append("}");
        return sb.toString();
    }

    private String getIndent() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < indentLevel; i++) {
            sb.append(INDENT);
        }
        return sb.toString();
    }

    private String escapeString(String value) {
        return value.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\t", "\\t")
                    .replace("\r", "\\r")
                    .replace("%p", "%s");
    }

    private String escapeChar(String value) {
        if (value.length() == 1) {
            char c = value.charAt(0);
            switch (c) {
                case '\n': return "\\n";
                case '\t': return "\\t";
                case '\r': return "\\r";
                case '\'': return "\\'";
                case '\\': return "\\\\";
                default: return value;
            }
        }
        return value;
    }

    @Override
    public String visitComment(Comment node) {
        return node.getText();
    }

    @Override
    public String visitUnsupportedCode(UnsupportedCode node) {
        StringBuilder sb = new StringBuilder();
        sb.append("// [UNSUPPORTED] Line ").append(node.getLine()).append("\n");
        sb.append("// Reason: ").append(node.getReason()).append("\n");
        sb.append("// Original: ").append(node.getOriginalCode());
        return sb.toString();
    }
}