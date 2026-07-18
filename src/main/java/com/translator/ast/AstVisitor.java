package com.translator.ast;

public interface AstVisitor<T> {
    T visitProgram(Program node);
    T visitType(Type node);
    T visitVariableDeclaration(VariableDeclaration node);
    T visitFunctionDeclaration(FunctionDeclaration node);
    T visitStructDeclaration(StructDeclaration node);
    T visitTypedefDeclaration(TypedefDeclaration node);
    T visitBlock(Block node);
    T visitIfStatement(IfStatement node);
    T visitWhileStatement(WhileStatement node);
    T visitForStatement(ForStatement node);
    T visitDoWhileStatement(DoWhileStatement node);
    T visitSwitchStatement(SwitchStatement node);
    T visitCaseStatement(CaseStatement node);
    T visitDefaultStatement(DefaultStatement node);
    T visitBreakStatement(BreakStatement node);
    T visitContinueStatement(ContinueStatement node);
    T visitReturnStatement(ReturnStatement node);
    T visitExpressionStatement(ExpressionStatement node);
    T visitAssignment(Assignment node);
    T visitBinaryExpression(BinaryExpression node);
    T visitUnaryExpression(UnaryExpression node);
    T visitIdentifier(Identifier node);
    T visitLiteral(Literal node);
    T visitArrayAccess(ArrayAccess node);
    T visitFunctionCall(FunctionCall node);
    T visitStructAccess(StructAccess node);
    T visitTypeCastExpression(TypeCastExpression node);
    T visitTernaryExpression(TernaryExpression node);
    T visitArrayInitializer(ArrayInitializer node);
    T visitEnumDeclaration(EnumDeclaration node);
    T visitComment(Comment node);
    T visitUnsupportedCode(UnsupportedCode node);
}