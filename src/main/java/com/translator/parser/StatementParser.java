package com.translator.parser;

import com.translator.ast.*;
import com.translator.token.Token;
import com.translator.token.TokenType;

import java.util.ArrayList;
import java.util.List;

class StatementParser {
    private final ParserBase base;
    private final TypeParser typeParser;
    private DeclarationParser declarationParser;
    private final ExpressionParser expressionParser;

    StatementParser(ParserBase base, TypeParser typeParser, DeclarationParser declarationParser, ExpressionParser expressionParser) {
        this.base = base;
        this.typeParser = typeParser;
        this.declarationParser = declarationParser;
        this.expressionParser = expressionParser;
    }

    void setDeclarationParser(DeclarationParser declarationParser) {
        this.declarationParser = declarationParser;
    }

    AstNode parseStatement() {
        if (base.match(TokenType.IF)) {
            return parseIfStatement();
        } else if (base.match(TokenType.WHILE)) {
            return parseWhileStatement();
        } else if (base.match(TokenType.FOR)) {
            return parseForStatement();
        } else if (base.match(TokenType.DO)) {
            return parseDoWhileStatement();
        } else if (base.match(TokenType.SWITCH)) {
            return parseSwitchStatement();
        } else if (base.match(TokenType.BREAK)) {
            base.eat(TokenType.BREAK);
            base.eat(TokenType.SEMICOLON);
            return new BreakStatement();
        } else if (base.match(TokenType.CONTINUE)) {
            base.eat(TokenType.CONTINUE);
            base.eat(TokenType.SEMICOLON);
            return new ContinueStatement();
        } else if (base.match(TokenType.RETURN)) {
            return parseReturnStatement();
        } else if (base.match(TokenType.LBRACE)) {
            return parseBlock();
        } else if (base.match(TokenType.STRUCT)) {
            return parseStructInStatement();
        } else if (base.matchAny(TokenType.INT, TokenType.LONG, TokenType.SHORT, TokenType.CHAR, TokenType.FLOAT, TokenType.DOUBLE, TokenType.VOID, TokenType.BOOL, TokenType.SIZE_T, TokenType.UNSIGNED, TokenType.CONST)) {
            return parseTypedStatement();
        } else if (base.currentToken.getType() == TokenType.IDENTIFIER) {
            return parseIdentifierStatement();
        } else {
            AstNode expr = expressionParser.parseExpression();
            base.eat(TokenType.SEMICOLON);
            return new ExpressionStatement(expr);
        }
    }

    private AstNode parseStructInStatement() {
        base.eat(TokenType.STRUCT);
        if (base.match(TokenType.LBRACE)) {
            return declarationParser.parseStructDeclaration(null);
        } else if (base.match(TokenType.IDENTIFIER)) {
            Identifier structName = typeParser.parseIdentifier();
            Type type = new Type("struct " + structName.getName(), 0, false, null);
            Identifier name = typeParser.parseIdentifier();
            return typeParser.parseVariableDeclaration(type, name);
        } else {
            throw new RuntimeException("Unexpected token " + base.currentToken.getType() + " after struct at line " + base.currentToken.getLine());
        }
    }

    private AstNode parseTypedStatement() {
        Type type = typeParser.parseType();
        
        if (base.match(TokenType.LPAREN)) {
            base.eat(TokenType.LPAREN);
            if (base.match(TokenType.MUL)) {
                return parseFunctionPointerStatement(type);
            }
            base.eat(TokenType.RPAREN);
        }
        
        Identifier name = typeParser.parseIdentifier();
        if (base.match(TokenType.LPAREN)) {
            return declarationParser.parseFunctionDeclaration(type, name);
        } else {
            List<VariableDeclaration> vars = new ArrayList<>();
            vars.add(typeParser.parseVariableDeclarationHelper(type, name));
            while (base.match(TokenType.COMMA)) {
                base.eat(TokenType.COMMA);
                Identifier nextName = typeParser.parseIdentifier();
                vars.add(typeParser.parseVariableDeclarationHelper(type, nextName));
            }
            base.eat(TokenType.SEMICOLON);
            if (vars.size() == 1) {
                return vars.get(0);
            } else {
                List<AstNode> statements = new ArrayList<>();
                statements.addAll(vars);
                return new Block(statements);
            }
        }
    }

    private AstNode parseFunctionPointerStatement(Type type) {
        base.eat(TokenType.MUL);
        Identifier ptrName = typeParser.parseIdentifier();
        
        boolean isArray = false;
        Integer arraySize = null;
        if (base.match(TokenType.LBRACKET)) {
            base.eat(TokenType.LBRACKET);
            isArray = true;
            if (base.match(TokenType.NUMBER)) {
                arraySize = Integer.parseInt(base.currentToken.getValue());
                base.eat(TokenType.NUMBER);
            }
            base.eat(TokenType.RBRACKET);
        }
        
        base.eat(TokenType.RPAREN);
        
        base.eat(TokenType.LPAREN);
        List<Type> paramTypes = new ArrayList<>();
        if (!base.match(TokenType.RPAREN)) {
            paramTypes.add(typeParser.parseType());
            while (base.match(TokenType.COMMA)) {
                base.eat(TokenType.COMMA);
                paramTypes.add(typeParser.parseType());
            }
        }
        base.eat(TokenType.RPAREN);
        
        FunctionPointerType fpType = new FunctionPointerType(type, paramTypes, isArray, arraySize);
        
        AstNode initializer = null;
        if (base.match(TokenType.ASSIGN)) {
            base.eat(TokenType.ASSIGN);
            initializer = expressionParser.parseExpression();
        }
        base.eat(TokenType.SEMICOLON);
        return new VariableDeclaration(fpType, ptrName, initializer);
    }

    private AstNode parseIdentifierStatement() {
        Token typeToken = base.currentToken;
        base.eat(TokenType.IDENTIFIER);
        
        if (base.match(TokenType.MUL)) {
            base.eat(TokenType.MUL);
            Type type = new Type(typeToken.getValue(), 1, false, null);
            Identifier name = typeParser.parseIdentifier();
            AstNode initializer = null;
            if (base.match(TokenType.ASSIGN)) {
                base.eat(TokenType.ASSIGN);
                initializer = expressionParser.parseExpression();
            }
            base.eat(TokenType.SEMICOLON);
            return new VariableDeclaration(type, name, initializer);
        } else if (base.currentToken.getType() == TokenType.IDENTIFIER) {
            int pointerLevel = 0;
            while (base.match(TokenType.MUL)) {
                base.eat(TokenType.MUL);
                pointerLevel++;
            }
            Type type = new Type(typeToken.getValue(), pointerLevel, false, null);
            Identifier name = typeParser.parseIdentifier();
            AstNode initializer = null;
            if (base.match(TokenType.ASSIGN)) {
                base.eat(TokenType.ASSIGN);
                initializer = expressionParser.parseExpression();
            }
            base.eat(TokenType.SEMICOLON);
            return new VariableDeclaration(type, name, initializer);
        } else {
            AstNode left = new Identifier(typeToken.getValue());
            AstNode postfix = expressionParser.parsePostfixExpression(left);
            AstNode expr = expressionParser.parseAssignmentWithLeft(postfix);
            base.eat(TokenType.SEMICOLON);
            return new ExpressionStatement(expr);
        }
    }

    IfStatement parseIfStatement() {
        base.eat(TokenType.IF);
        base.eat(TokenType.LPAREN);
        AstNode condition = expressionParser.parseExpression();
        base.eat(TokenType.RPAREN);
        Block thenBlock = parseStatementAsBlock();
        Block elseBlock = null;
        if (base.match(TokenType.ELSE)) {
            base.eat(TokenType.ELSE);
            elseBlock = parseStatementAsBlock();
        }
        return new IfStatement(condition, thenBlock, elseBlock);
    }

    WhileStatement parseWhileStatement() {
        base.eat(TokenType.WHILE);
        base.eat(TokenType.LPAREN);
        AstNode condition = expressionParser.parseExpression();
        base.eat(TokenType.RPAREN);
        Block body = parseStatementAsBlock();
        return new WhileStatement(condition, body);
    }

    ForStatement parseForStatement() {
        base.eat(TokenType.FOR);
        base.eat(TokenType.LPAREN);
        AstNode init = null;
        if (!base.match(TokenType.SEMICOLON)) {
            if (base.matchAny(TokenType.INT, TokenType.LONG, TokenType.SHORT, TokenType.CHAR, TokenType.FLOAT, TokenType.DOUBLE, TokenType.BOOL, TokenType.SIZE_T)) {
                Type type = typeParser.parseType();
                Identifier name = typeParser.parseIdentifier();
                AstNode initializer = null;
                if (base.match(TokenType.ASSIGN)) {
                    base.eat(TokenType.ASSIGN);
                    initializer = expressionParser.parseExpression();
                }
                init = new VariableDeclaration(type, name, initializer);
            } else {
                init = expressionParser.parseExpression();
            }
        }
        base.eat(TokenType.SEMICOLON);
        AstNode condition = null;
        if (!base.match(TokenType.SEMICOLON)) {
            condition = expressionParser.parseExpression();
        }
        base.eat(TokenType.SEMICOLON);
        AstNode update = null;
        if (!base.match(TokenType.RPAREN)) {
            update = expressionParser.parseExpression();
        }
        base.eat(TokenType.RPAREN);
        Block body = parseStatementAsBlock();
        return new ForStatement(init, condition, update, body);
    }

    DoWhileStatement parseDoWhileStatement() {
        base.eat(TokenType.DO);
        Block body = parseStatementAsBlock();
        base.eat(TokenType.WHILE);
        base.eat(TokenType.LPAREN);
        AstNode condition = expressionParser.parseExpression();
        base.eat(TokenType.RPAREN);
        base.eat(TokenType.SEMICOLON);
        return new DoWhileStatement(body, condition);
    }

    AstNode parseExpressionStatement(AstNode startExpr) {
        AstNode expr = expressionParser.parseAssignment();
        base.eat(TokenType.SEMICOLON);
        return new ExpressionStatement(expr);
    }

    SwitchStatement parseSwitchStatement() {
        base.eat(TokenType.SWITCH);
        base.eat(TokenType.LPAREN);
        AstNode expression = expressionParser.parseExpression();
        base.eat(TokenType.RPAREN);
        base.eat(TokenType.LBRACE);
        List<AstNode> cases = new ArrayList<>();
        while (!base.match(TokenType.RBRACE)) {
            if (base.match(TokenType.CASE)) {
                cases.add(parseCaseStatement());
            } else if (base.match(TokenType.DEFAULT)) {
                cases.add(parseDefaultStatement());
            } else {
                throw new RuntimeException("Unexpected token " + base.currentToken.getType() + " in switch statement");
            }
        }
        base.eat(TokenType.RBRACE);
        return new SwitchStatement(expression, cases);
    }

    CaseStatement parseCaseStatement() {
        base.eat(TokenType.CASE);
        AstNode value = expressionParser.parseExpression();
        base.eat(TokenType.COLON);
        List<AstNode> statements = new ArrayList<>();
        while (!base.matchAny(TokenType.CASE, TokenType.DEFAULT, TokenType.RBRACE)) {
            statements.add(parseStatement());
        }
        return new CaseStatement(value, statements);
    }

    DefaultStatement parseDefaultStatement() {
        base.eat(TokenType.DEFAULT);
        base.eat(TokenType.COLON);
        List<AstNode> statements = new ArrayList<>();
        while (!base.matchAny(TokenType.CASE, TokenType.DEFAULT, TokenType.RBRACE)) {
            statements.add(parseStatement());
        }
        return new DefaultStatement(statements);
    }

    ReturnStatement parseReturnStatement() {
        base.eat(TokenType.RETURN);
        AstNode expression = null;
        if (!base.match(TokenType.SEMICOLON)) {
            expression = expressionParser.parseExpression();
        }
        base.eat(TokenType.SEMICOLON);
        return new ReturnStatement(expression);
    }

    Block parseBlock() {
        base.eat(TokenType.LBRACE);
        List<AstNode> statements = new ArrayList<>();
        while (!base.match(TokenType.RBRACE)) {
            statements.add(parseStatement());
        }
        base.eat(TokenType.RBRACE);
        return new Block(statements);
    }

    Block parseStatementAsBlock() {
        if (base.match(TokenType.LBRACE)) {
            return parseBlock();
        } else {
            List<AstNode> statements = new ArrayList<>();
            statements.add(parseStatement());
            return new Block(statements);
        }
    }

    AstNode parseExpression() {
        return expressionParser.parseExpression();
    }
}