package com.translator.transform;

import com.translator.ast.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AstTransformer implements AstVisitor<AstNode> {
    private String className = "TranslatedCode";
    private Set<String> stringVariables = new HashSet<>();
    private java.util.Map<String, String> pointerMappings = new java.util.HashMap<>();

    public AstTransformer() {
    }

    public AstTransformer(String className) {
        this.className = className;
    }

    @Override
    public AstNode visitProgram(Program node) {
        List<AstNode> javaDeclarations = new ArrayList<>();
        List<AstNode> methods = new ArrayList<>();
        List<AstNode> fields = new ArrayList<>();
        List<AstNode> standaloneStatements = new ArrayList<>();

        for (AstNode decl : node.getDeclarations()) {
            AstNode transformed = decl.accept(this);
            if (transformed == null) {
                continue;
            }
            // 过滤掉没有 body 的函数声明（函数原型）
            if (transformed instanceof FunctionDeclaration) {
                FunctionDeclaration fd = (FunctionDeclaration) transformed;
                if (fd.getBody() != null) {
                    methods.add(transformed);
                }
            } else if (transformed instanceof StructDeclaration || transformed instanceof EnumDeclaration) {
                javaDeclarations.add(transformed);
            } else if (transformed instanceof VariableDeclaration) {
                fields.add(transformed);
            } else {
                standaloneStatements.add(transformed);
            }
        }

        if (!methods.isEmpty() || !standaloneStatements.isEmpty()) {
            List<VariableDeclaration> params = new ArrayList<>();
            Block classBody = new Block(new ArrayList<>());

            for (AstNode field : fields) {
                classBody.getStatements().add(field);
            }
            
            if (!standaloneStatements.isEmpty()) {
                List<VariableDeclaration> mainParams = new ArrayList<>();
                mainParams.add(new VariableDeclaration(new Type("String[]"), new Identifier("args")));
                Block mainBody = new Block(standaloneStatements);
                FunctionDeclaration mainMethod = new FunctionDeclaration(
                        new Type("public static void"),
                        new Identifier("main"),
                        mainParams,
                        mainBody
                );
                classBody.getStatements().add(mainMethod);
            }
            
            for (AstNode method : methods) {
                classBody.getStatements().add(method);
            }

            javaDeclarations.add(new FunctionDeclaration(
                    new Type("public class"),
                    new Identifier(className),
                    params,
                    classBody
            ));
        }

        return new Program(javaDeclarations);
    }

    @Override
    public AstNode visitType(Type node) {
        String javaTypeName = TypeMapper.mapType(node);
        return new Type(javaTypeName, node.getPointerLevel(), node.isArray(), node.getArraySize());
    }

    @Override
    public AstNode visitVariableDeclaration(VariableDeclaration node) {
        Type javaType = (Type) node.getType().accept(this);
        AstNode javaInitializer = node.getInitializer() != null ? node.getInitializer().accept(this) : null;
        
        if (javaType.getName().contains("[]") && javaInitializer instanceof UnaryExpression) {
            UnaryExpression init = (UnaryExpression) javaInitializer;
            if (init.getOperator().equals("&")) {
                javaInitializer = init.getOperand();
                String ptrName = node.getName().getName();
                String targetName = ((Identifier) init.getOperand()).getName();
                pointerMappings.put(ptrName, targetName);
                return new Comment("// " + ptrName + " -> " + targetName + " (pointer mapping)");
            }
        }
        
        if (javaType.getName().equals("String")) {
            stringVariables.add(node.getName().getName());
        }
        return new VariableDeclaration(javaType, node.getName(), javaInitializer);
    }

    @Override
    public AstNode visitFunctionDeclaration(FunctionDeclaration node) {
        Type javaReturnType = (Type) node.getReturnType().accept(this);

        String funcName = node.getName().getName();
        if (funcName.equals("main")) {
            javaReturnType = new Type("public static void");
        } else {
            javaReturnType = new Type("public " + javaReturnType.getName());
        }

        List<VariableDeclaration> javaParams = new ArrayList<>();
        for (VariableDeclaration param : node.getParameters()) {
            javaParams.add((VariableDeclaration) param.accept(this));
        }

        if (funcName.equals("main") && javaParams.isEmpty()) {
            javaParams.add(new VariableDeclaration(new Type("String[]"), new Identifier("args")));
        }

        Block javaBody = node.getBody() != null ? (Block) node.getBody().accept(this) : null;
        return new FunctionDeclaration(javaReturnType, node.getName(), javaParams, javaBody);
    }

    @Override
    public AstNode visitStructDeclaration(StructDeclaration node) {
        List<AstNode> javaFields = new ArrayList<>();
        for (VariableDeclaration field : node.getFields()) {
            VariableDeclaration javaField = (VariableDeclaration) field.accept(this);
            javaField = new VariableDeclaration(
                    new Type("private " + javaField.getType().getName()),
                    javaField.getName(),
                    javaField.getInitializer()
            );
            javaFields.add(javaField);
        }

        List<VariableDeclaration> params = new ArrayList<>();
        Block classBody = new Block(javaFields);

        return new FunctionDeclaration(
                new Type("public class"),
                node.getName(),
                params,
                classBody
        );
    }

    @Override
    public AstNode visitTypedefDeclaration(TypedefDeclaration node) {
        if (node.getOriginalType() instanceof StructDeclaration) {
            return node.getOriginalType().accept(this);
        }
        if (node.getOriginalType() instanceof EnumDeclaration) {
            return node.getOriginalType().accept(this);
        }
        return null;
    }

    @Override
    public AstNode visitBlock(Block node) {
        List<AstNode> javaStatements = new ArrayList<>();
        for (AstNode stmt : node.getStatements()) {
            AstNode javaStmt = stmt.accept(this);
            if (javaStmt != null) {
                javaStatements.add(javaStmt);
            }
        }
        return new Block(javaStatements);
    }

    @Override
    public AstNode visitIfStatement(IfStatement node) {
        AstNode javaCondition = node.getCondition().accept(this);
        Block javaThen = (Block) node.getThenBlock().accept(this);
        Block javaElse = node.getElseBlock() != null ? (Block) node.getElseBlock().accept(this) : null;
        return new IfStatement(javaCondition, javaThen, javaElse);
    }

    @Override
    public AstNode visitWhileStatement(WhileStatement node) {
        AstNode javaCondition = node.getCondition().accept(this);
        Block javaBody = (Block) node.getBody().accept(this);
        return new WhileStatement(javaCondition, javaBody);
    }

    @Override
    public AstNode visitForStatement(ForStatement node) {
        AstNode javaInit = node.getInit() != null ? node.getInit().accept(this) : null;
        AstNode javaCondition = node.getCondition() != null ? node.getCondition().accept(this) : null;
        AstNode javaUpdate = node.getUpdate() != null ? node.getUpdate().accept(this) : null;
        Block javaBody = (Block) node.getBody().accept(this);
        return new ForStatement(javaInit, javaCondition, javaUpdate, javaBody);
    }

    @Override
    public AstNode visitDoWhileStatement(DoWhileStatement node) {
        Block javaBody = (Block) node.getBody().accept(this);
        AstNode javaCondition = node.getCondition().accept(this);
        return new DoWhileStatement(javaBody, javaCondition);
    }

    @Override
    public AstNode visitSwitchStatement(SwitchStatement node) {
        AstNode javaExpr = node.getExpression().accept(this);
        List<AstNode> javaCases = new ArrayList<>();
        for (AstNode caseStmt : node.getCases()) {
            javaCases.add(caseStmt.accept(this));
        }
        return new SwitchStatement(javaExpr, javaCases);
    }

    @Override
    public AstNode visitCaseStatement(CaseStatement node) {
        AstNode javaValue = node.getValue().accept(this);
        List<AstNode> javaStatements = new ArrayList<>();
        for (AstNode stmt : node.getStatements()) {
            javaStatements.add(stmt.accept(this));
        }
        return new CaseStatement(javaValue, javaStatements);
    }

    @Override
    public AstNode visitDefaultStatement(DefaultStatement node) {
        List<AstNode> javaStatements = new ArrayList<>();
        for (AstNode stmt : node.getStatements()) {
            javaStatements.add(stmt.accept(this));
        }
        return new DefaultStatement(javaStatements);
    }

    @Override
    public AstNode visitBreakStatement(BreakStatement node) {
        return node;
    }

    @Override
    public AstNode visitContinueStatement(ContinueStatement node) {
        return node;
    }

    @Override
    public AstNode visitReturnStatement(ReturnStatement node) {
        AstNode javaExpr = node.getExpression() != null ? node.getExpression().accept(this) : null;
        if (javaExpr instanceof Literal) {
            Literal lit = (Literal) javaExpr;
            if (lit.getValue().equals("0")) {
                return null;
            }
        }
        return new ReturnStatement(javaExpr);
    }

    @Override
    public AstNode visitExpressionStatement(ExpressionStatement node) {
        AstNode javaExpr = node.getExpression().accept(this);
        return new ExpressionStatement(javaExpr);
    }

    @Override
    public AstNode visitAssignment(Assignment node) {
        AstNode javaLeft = node.getLeft().accept(this);
        AstNode javaRight = node.getRight().accept(this);
        return new Assignment(javaLeft, node.getOperator(), javaRight);
    }

    @Override
    public AstNode visitBinaryExpression(BinaryExpression node) {
        AstNode javaLeft = node.getLeft().accept(this);
        AstNode javaRight = node.getRight().accept(this);
        String operator = node.getOperator();
        // 对于 == 和 != 运算符，若操作数为字符串，则使用特殊操作符以便 CodeGenerator 生成 equals 调用
        if (operator.equals("==") || operator.equals("!=")) {
            if (isStringOperand(javaLeft) || isStringOperand(javaRight)) {
                if (operator.equals("==")) {
                    return new BinaryExpression(javaLeft, "equals", javaRight);
                } else {
                    return new BinaryExpression(javaLeft, "notequals", javaRight);
                }
            }
        }
        return new BinaryExpression(javaLeft, operator, javaRight);
    }

    /**
     * 判断操作数是否为字符串（字符串字面量或字符串变量）
     */
    private boolean isStringOperand(AstNode node) {
        if (node instanceof Literal) {
            return ((Literal) node).getType() == Literal.LiteralType.STRING;
        }
        if (node instanceof Identifier) {
            return stringVariables.contains(((Identifier) node).getName());
        }
        return false;
    }

    @Override
    public AstNode visitUnaryExpression(UnaryExpression node) {
        AstNode javaOperand = node.getOperand().accept(this);
        
        if (node.getOperator().equals("*") && javaOperand instanceof Identifier) {
            String ptrName = ((Identifier) javaOperand).getName();
            if (pointerMappings.containsKey(ptrName)) {
                return new Identifier(pointerMappings.get(ptrName));
            }
        }
        
        return new UnaryExpression(node.getOperator(), javaOperand, node.isPostfix());
    }

    @Override
    public AstNode visitIdentifier(Identifier node) {
        return node;
    }

    @Override
    public AstNode visitComment(Comment node) {
        return node;
    }

    @Override
    public AstNode visitLiteral(Literal node) {
        return node;
    }

    @Override
    public AstNode visitArrayAccess(ArrayAccess node) {
        AstNode javaArray = node.getArray().accept(this);
        AstNode javaIndex = node.getIndex().accept(this);
        return new ArrayAccess(javaArray, javaIndex);
    }

    @Override
    public AstNode visitFunctionCall(FunctionCall node) {
        if (StdlibMapper.isStdlibFunction(node.getName().getName())) {
            FunctionCall mapped = StdlibMapper.mapFunctionCall(node);
            List<AstNode> javaArgs = new ArrayList<>();
            for (AstNode arg : mapped.getArguments()) {
                AstNode mappedArg = mapPointerReferences(arg);
                javaArgs.add(mappedArg.accept(this));
            }
            return new FunctionCall(mapped.getName(), javaArgs);
        }

        List<AstNode> javaArgs = new ArrayList<>();
        for (AstNode arg : node.getArguments()) {
            AstNode mappedArg = mapPointerReferences(arg);
            javaArgs.add(mappedArg.accept(this));
        }
        return new FunctionCall(node.getName(), javaArgs);
    }
    
    private AstNode mapPointerReferences(AstNode node) {
        if (node instanceof Identifier) {
            String name = ((Identifier) node).getName();
            if (pointerMappings.containsKey(name)) {
                return new Identifier(pointerMappings.get(name));
            }
        }
        return node;
    }

    @Override
    public AstNode visitStructAccess(StructAccess node) {
        AstNode javaStruct = node.getStruct().accept(this);
        return new StructAccess(javaStruct, node.getField());
    }

    @Override
    public AstNode visitTypeCastExpression(TypeCastExpression node) {
        return node.getExpression().accept(this);
    }

    @Override
    public AstNode visitTernaryExpression(TernaryExpression node) {
        AstNode javaCondition = node.getCondition().accept(this);
        AstNode javaTrueExpr = node.getTrueExpression().accept(this);
        AstNode javaFalseExpr = node.getFalseExpression().accept(this);
        return new TernaryExpression(javaCondition, javaTrueExpr, javaFalseExpr);
    }
    
    @Override
    public AstNode visitArrayInitializer(ArrayInitializer node) {
        List<AstNode> javaElements = new ArrayList<>();
        for (AstNode element : node.getElements()) {
            javaElements.add(element.accept(this));
        }
        return new ArrayInitializer(javaElements);
    }

    @Override
    public AstNode visitEnumDeclaration(EnumDeclaration node) {
        List<Identifier> javaValues = new ArrayList<>();
        for (Identifier value : node.getValues()) {
            javaValues.add((Identifier) value.accept(this));
        }
        Identifier name = node.getName() != null ? (Identifier) node.getName().accept(this) : null;
        return new EnumDeclaration(name, javaValues);
    }
}