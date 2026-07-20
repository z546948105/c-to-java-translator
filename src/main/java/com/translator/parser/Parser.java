package com.translator.parser;

import com.translator.ast.*;
import com.translator.token.Lexer;
import com.translator.token.Token;
import com.translator.token.TokenType;

import java.util.ArrayList;
import java.util.List;

/**
 * 语法分析器（Parser）
 * <p>
 * 将 token 流解析为抽象语法树（AST）
 * <p>
 * 使用递归下降解析方法，支持 C 语言的主要语法结构：
 * - 函数声明和定义
 * - 变量声明和初始化
 * - 结构体和枚举定义
 * - 宏定义（#define）
 * - 控制流语句（if, while, for, switch）
 * - 表达式和赋值语句
 */
public class Parser {
    private final Lexer lexer;
    private Token currentToken;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
        this.currentToken = lexer.nextToken();
    }

    private Token peek() {
        return lexer.peekToken();
    }

    private void eat(TokenType type) {
        if (currentToken.getType() == type) {
            currentToken = lexer.nextToken();
        } else {
            throw new RuntimeException("Expected token " + type + " but got " + currentToken.getType() + " at line " + currentToken.getLine());
        }
    }

    private String consumeUntil(TokenType... stopTokens) {
        StringBuilder sb = new StringBuilder();
        while (!match(TokenType.EOF)) {
            boolean foundStop = false;
            for (TokenType stop : stopTokens) {
                if (match(stop)) {
                    foundStop = true;
                    break;
                }
            }
            if (foundStop) break;
            sb.append(currentToken.getValue());
            currentToken = lexer.nextToken();
        }
        return sb.toString();
    }

    private UnsupportedCode createUnsupportedCode(String reason) {
        String originalCode = consumeUntil(TokenType.SEMICOLON, TokenType.LBRACE, TokenType.RBRACE, TokenType.EOF);
        int line = currentToken.getLine();
        if (match(TokenType.SEMICOLON) || match(TokenType.LBRACE) || match(TokenType.RBRACE)) {
            currentToken = lexer.nextToken();
        }
        return new UnsupportedCode(originalCode, reason, line);
    }

    private boolean match(TokenType type) {
        return currentToken.getType() == type;
    }

    private void skipWhitespace() {
        while (currentToken != null && 
               (currentToken.getType() == TokenType.IDENTIFIER || 
                currentToken.getType() == TokenType.NUMBER ||
                currentToken.getType() == TokenType.STRING ||
                currentToken.getType() == TokenType.CHAR_LITERAL)) {
            currentToken = lexer.nextToken();
        }
    }

    private boolean matchAny(TokenType... types) {
        for (TokenType type : types) {
            if (match(type)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isTypeStart(Token token) {
        if (token == null) return false;
        TokenType type = token.getType();
        return type == TokenType.INT || type == TokenType.LONG || type == TokenType.SHORT ||
               type == TokenType.CHAR || type == TokenType.FLOAT || type == TokenType.DOUBLE ||
               type == TokenType.VOID || type == TokenType.BOOL || type == TokenType.STRUCT ||
               type == TokenType.SIZE_T || type == TokenType.CONST || type == TokenType.UNSIGNED ||
               type == TokenType.ENUM || type == TokenType.UNION;
    }

    public Program parse() {
        List<AstNode> declarations = new ArrayList<>();
        while (!match(TokenType.EOF)) {
            try {
                AstNode decl = parseDeclaration();
                if (decl != null) {
                    declarations.add(decl);
                }
            } catch (RuntimeException e) {
                String reason = e.getMessage();
                declarations.add(createUnsupportedCode(reason));
            }
        }
        return new Program(declarations);
    }

    private AstNode parseDeclaration() {
        boolean isStatic = false;
        if (currentToken.getType() == TokenType.STATIC) {
            eat(TokenType.STATIC);
            isStatic = true;
        }
        if (currentToken.getType() == TokenType.DEFINE) {
            return parseMacroDeclaration();
        }
        if (currentToken.getType() == TokenType.TYPEDEF) {
            return parseTypedefDeclaration();
        } else if (currentToken.getType() == TokenType.STRUCT) {
            eat(TokenType.STRUCT);
            if (currentToken.getType() == TokenType.LBRACE) {
                return parseStructDeclaration();
            } else if (currentToken.getType() == TokenType.IDENTIFIER) {
                Identifier structName = parseIdentifier();
                if (currentToken.getType() == TokenType.LBRACE) {
                    return parseStructDeclarationWithName(structName);
                } else {
                    Type type = new Type("struct " + structName.getName(), 0, false, null);
                    if (currentToken.getType() == TokenType.LPAREN) {
                        return parseFunctionDeclaration(type, structName);
                    } else {
                        return parseVariableDeclaration(type, structName);
                    }
                }
            } else {
                return createUnsupportedCode("Unexpected token after struct: " + currentToken.getType());
            }
        } else if (currentToken.getType() == TokenType.ENUM) {
            eat(TokenType.ENUM);
            if (currentToken.getType() == TokenType.LBRACE) {
                return parseEnumDeclaration();
            } else if (currentToken.getType() == TokenType.IDENTIFIER) {
                Identifier enumName = parseIdentifier();
                if (currentToken.getType() == TokenType.LBRACE) {
                    return parseEnumDeclarationWithName(enumName);
                } else {
                    Type type = new Type("enum " + enumName.getName(), 0, false, null);
                    if (currentToken.getType() == TokenType.LPAREN) {
                        return parseFunctionDeclaration(type, enumName);
                    } else {
                        return parseVariableDeclaration(type, enumName);
                    }
                }
            } else {
                return createUnsupportedCode("Unexpected token after enum: " + currentToken.getType());
            }
        } else if (currentToken.getType() == TokenType.UNION) {
            eat(TokenType.UNION);
            if (currentToken.getType() == TokenType.LBRACE) {
                return parseUnionDeclaration();
            } else if (currentToken.getType() == TokenType.IDENTIFIER) {
                Identifier unionName = parseIdentifier();
                if (currentToken.getType() == TokenType.LBRACE) {
                    return parseUnionDeclarationWithName(unionName);
                } else {
                    Type type = new Type("union " + unionName.getName(), 0, false, null);
                    if (currentToken.getType() == TokenType.LPAREN) {
                        return parseFunctionDeclaration(type, unionName);
                    } else {
                        return parseVariableDeclaration(type, unionName);
                    }
                }
            } else {
                return createUnsupportedCode("Unexpected token after union: " + currentToken.getType());
            }
        } else if (matchAny(TokenType.IF, TokenType.WHILE, TokenType.FOR, TokenType.DO, TokenType.SWITCH, TokenType.BREAK, TokenType.CONTINUE, TokenType.RETURN, TokenType.LBRACE)) {
            return parseStatement();
        } else if (match(TokenType.RBRACE)) {
            eat(TokenType.RBRACE);
            return null;
        } else if (!isTypeStart(currentToken)) {
            return createUnsupportedCode("Expected type keyword but got: " + currentToken.getType());
        }
        
        if (currentToken.getType() == TokenType.IDENTIFIER) {
            Token next = peek();
            if (next != null && next.getType() != TokenType.MUL && next.getType() != TokenType.LPAREN) {
                return parseStatement();
            }
        }
        
        Type type = parseType();
        Identifier name = parseIdentifier();
        if (currentToken.getType() == TokenType.LPAREN) {
            return parseFunctionDeclaration(type, name);
        } else {
            List<VariableDeclaration> vars = new ArrayList<>();
            vars.add(parseVariableDeclarationHelper(type, name));
            while (match(TokenType.COMMA)) {
                eat(TokenType.COMMA);
                Identifier nextName = parseIdentifier();
                vars.add(parseVariableDeclarationHelper(type, nextName));
            }
            eat(TokenType.SEMICOLON);
            if (vars.size() == 1) {
                return vars.get(0);
            } else {
                List<AstNode> statements = new ArrayList<>();
                statements.addAll(vars);
                return new Block(statements);
            }
        }
    }

    private StructDeclaration parseStructDeclaration() {
        eat(TokenType.LBRACE);
        List<VariableDeclaration> fields = new ArrayList<>();
        while (!match(TokenType.RBRACE)) {
            Type type = parseType();
            Identifier fieldName = parseIdentifier();
            if (match(TokenType.LBRACKET)) {
                eat(TokenType.LBRACKET);
                Integer arraySize = null;
                if (match(TokenType.NUMBER)) {
                    arraySize = Integer.parseInt(currentToken.getValue());
                    eat(TokenType.NUMBER);
                }
                eat(TokenType.RBRACKET);
                type = new Type(type.getName(), type.getPointerLevel(), true, arraySize);
            }
            if (match(TokenType.COLON)) {
                eat(TokenType.COLON);
                if (match(TokenType.NUMBER)) {
                    eat(TokenType.NUMBER);
                }
            }
            AstNode initializer = null;
            if (match(TokenType.ASSIGN)) {
                eat(TokenType.ASSIGN);
                initializer = parseExpression();
            }
            eat(TokenType.SEMICOLON);
            fields.add(new VariableDeclaration(type, fieldName, initializer));
        }
        eat(TokenType.RBRACE);
        eat(TokenType.SEMICOLON);
        return new StructDeclaration(null, fields);
    }

    private StructDeclaration parseStructDeclarationWithName(Identifier name) {
        eat(TokenType.LBRACE);
        List<VariableDeclaration> fields = new ArrayList<>();
        while (!match(TokenType.RBRACE)) {
            Type type = parseType();
            Identifier fieldName = parseIdentifier();
            if (match(TokenType.LBRACKET)) {
                eat(TokenType.LBRACKET);
                Integer arraySize = null;
                if (match(TokenType.NUMBER)) {
                    arraySize = Integer.parseInt(currentToken.getValue());
                    eat(TokenType.NUMBER);
                }
                eat(TokenType.RBRACKET);
                type = new Type(type.getName(), type.getPointerLevel(), true, arraySize);
            }
            if (match(TokenType.COLON)) {
                eat(TokenType.COLON);
                if (match(TokenType.NUMBER)) {
                    eat(TokenType.NUMBER);
                }
            }
            AstNode initializer = null;
            if (match(TokenType.ASSIGN)) {
                eat(TokenType.ASSIGN);
                initializer = parseExpression();
            }
            eat(TokenType.SEMICOLON);
            fields.add(new VariableDeclaration(type, fieldName, initializer));
        }
        eat(TokenType.RBRACE);
        eat(TokenType.SEMICOLON);
        return new StructDeclaration(name, fields);
    }

    private StructDeclaration parseUnionDeclaration() {
        eat(TokenType.LBRACE);
        List<VariableDeclaration> fields = new ArrayList<>();
        while (!match(TokenType.RBRACE)) {
            Type type = parseType();
            Identifier fieldName = parseIdentifier();
            if (match(TokenType.LBRACKET)) {
                eat(TokenType.LBRACKET);
                Integer arraySize = null;
                if (match(TokenType.NUMBER)) {
                    arraySize = Integer.parseInt(currentToken.getValue());
                    eat(TokenType.NUMBER);
                }
                eat(TokenType.RBRACKET);
                type = new Type(type.getName(), type.getPointerLevel(), true, arraySize);
            }
            if (match(TokenType.COLON)) {
                eat(TokenType.COLON);
                if (match(TokenType.NUMBER)) {
                    eat(TokenType.NUMBER);
                }
            }
            AstNode initializer = null;
            if (match(TokenType.ASSIGN)) {
                eat(TokenType.ASSIGN);
                initializer = parseExpression();
            }
            eat(TokenType.SEMICOLON);
            fields.add(new VariableDeclaration(type, fieldName, initializer));
        }
        eat(TokenType.RBRACE);
        eat(TokenType.SEMICOLON);
        return new StructDeclaration(null, fields);
    }

    private StructDeclaration parseUnionDeclarationWithName(Identifier name) {
        eat(TokenType.LBRACE);
        List<VariableDeclaration> fields = new ArrayList<>();
        while (!match(TokenType.RBRACE)) {
            Type type = parseType();
            Identifier fieldName = parseIdentifier();
            if (match(TokenType.LBRACKET)) {
                eat(TokenType.LBRACKET);
                Integer arraySize = null;
                if (match(TokenType.NUMBER)) {
                    arraySize = Integer.parseInt(currentToken.getValue());
                    eat(TokenType.NUMBER);
                }
                eat(TokenType.RBRACKET);
                type = new Type(type.getName(), type.getPointerLevel(), true, arraySize);
            }
            if (match(TokenType.COLON)) {
                eat(TokenType.COLON);
                if (match(TokenType.NUMBER)) {
                    eat(TokenType.NUMBER);
                }
            }
            AstNode initializer = null;
            if (match(TokenType.ASSIGN)) {
                eat(TokenType.ASSIGN);
                initializer = parseExpression();
            }
            eat(TokenType.SEMICOLON);
            fields.add(new VariableDeclaration(type, fieldName, initializer));
        }
        eat(TokenType.RBRACE);
        eat(TokenType.SEMICOLON);
        return new StructDeclaration(name, fields);
    }

    private EnumDeclaration parseEnumDeclaration() {
        eat(TokenType.LBRACE);
        List<Identifier> values = new ArrayList<>();
        while (!match(TokenType.RBRACE)) {
            values.add(parseIdentifier());
            if (match(TokenType.COMMA)) {
                eat(TokenType.COMMA);
            }
        }
        eat(TokenType.RBRACE);
        eat(TokenType.SEMICOLON);
        return new EnumDeclaration(null, values);
    }

    private EnumDeclaration parseEnumDeclarationWithName(Identifier name) {
        eat(TokenType.LBRACE);
        List<Identifier> values = new ArrayList<>();
        while (!match(TokenType.RBRACE)) {
            values.add(parseIdentifier());
            if (match(TokenType.COMMA)) {
                eat(TokenType.COMMA);
            }
        }
        eat(TokenType.RBRACE);
        eat(TokenType.SEMICOLON);
        return new EnumDeclaration(name, values);
    }

    private AstNode parseTypedefDeclaration() {
        eat(TokenType.TYPEDEF);
        if (match(TokenType.STRUCT)) {
            eat(TokenType.STRUCT);
            Identifier structName = null;
            if (match(TokenType.IDENTIFIER)) {
                structName = parseIdentifier();
            }
            eat(TokenType.LBRACE);
            List<VariableDeclaration> fields = new ArrayList<>();
            while (!match(TokenType.RBRACE)) {
                Type type = parseType();
                Identifier fieldName = parseIdentifier();
                if (match(TokenType.LBRACKET)) {
                    eat(TokenType.LBRACKET);
                    Integer arraySize = null;
                    if (match(TokenType.NUMBER)) {
                        arraySize = Integer.parseInt(currentToken.getValue());
                        eat(TokenType.NUMBER);
                    }
                    eat(TokenType.RBRACKET);
                    type = new Type(type.getName(), type.getPointerLevel(), true, arraySize);
                }
                if (match(TokenType.COLON)) {
                    eat(TokenType.COLON);
                    if (match(TokenType.NUMBER)) {
                        eat(TokenType.NUMBER);
                    }
                }
                AstNode initializer = null;
                if (match(TokenType.ASSIGN)) {
                    eat(TokenType.ASSIGN);
                    initializer = parseExpression();
                }
                eat(TokenType.SEMICOLON);
                fields.add(new VariableDeclaration(type, fieldName, initializer));
            }
            eat(TokenType.RBRACE);
            Identifier alias = structName;
            if (match(TokenType.IDENTIFIER)) {
                alias = parseIdentifier();
            }
            eat(TokenType.SEMICOLON);
            StructDeclaration structDecl = new StructDeclaration(alias, fields);
            return new TypedefDeclaration(structDecl, alias);
        } else if (match(TokenType.UNION)) {
            eat(TokenType.UNION);
            Identifier unionName = null;
            if (match(TokenType.IDENTIFIER)) {
                unionName = parseIdentifier();
            }
            eat(TokenType.LBRACE);
            List<VariableDeclaration> fields = new ArrayList<>();
            while (!match(TokenType.RBRACE)) {
                Type type = parseType();
                Identifier fieldName = parseIdentifier();
                if (match(TokenType.LBRACKET)) {
                    eat(TokenType.LBRACKET);
                    Integer arraySize = null;
                    if (match(TokenType.NUMBER)) {
                        arraySize = Integer.parseInt(currentToken.getValue());
                        eat(TokenType.NUMBER);
                    }
                    eat(TokenType.RBRACKET);
                    type = new Type(type.getName(), type.getPointerLevel(), true, arraySize);
                }
                if (match(TokenType.COLON)) {
                    eat(TokenType.COLON);
                    if (match(TokenType.NUMBER)) {
                        eat(TokenType.NUMBER);
                    }
                }
                AstNode initializer = null;
                if (match(TokenType.ASSIGN)) {
                    eat(TokenType.ASSIGN);
                    initializer = parseExpression();
                }
                eat(TokenType.SEMICOLON);
                fields.add(new VariableDeclaration(type, fieldName, initializer));
            }
            eat(TokenType.RBRACE);
            Identifier alias = unionName;
            if (match(TokenType.IDENTIFIER)) {
                alias = parseIdentifier();
            }
            eat(TokenType.SEMICOLON);
            StructDeclaration unionDecl = new StructDeclaration(alias, fields);
            return new TypedefDeclaration(unionDecl, alias);
        } else if (match(TokenType.ENUM)) {
            eat(TokenType.ENUM);
            Identifier enumName = null;
            if (match(TokenType.IDENTIFIER)) {
                enumName = parseIdentifier();
            }
            eat(TokenType.LBRACE);
            List<Identifier> values = new ArrayList<>();
            while (!match(TokenType.RBRACE)) {
                values.add(parseIdentifier());
                if (match(TokenType.COMMA)) {
                    eat(TokenType.COMMA);
                }
            }
            eat(TokenType.RBRACE);
            Identifier alias = enumName;
            if (match(TokenType.IDENTIFIER)) {
                alias = parseIdentifier();
            }
            eat(TokenType.SEMICOLON);
            EnumDeclaration enumDecl = new EnumDeclaration(alias, values);
            return new TypedefDeclaration(enumDecl, alias);
        } else {
            Type type = parseType();
            if (match(TokenType.LPAREN) && peek() != null && peek().getType() == TokenType.MUL) {
                eat(TokenType.LPAREN);
                eat(TokenType.MUL);
                Identifier alias = parseIdentifier();
                eat(TokenType.RPAREN);
                eat(TokenType.LPAREN);
                while (!match(TokenType.RPAREN)) {
                    parseType();
                    if (match(TokenType.IDENTIFIER)) {
                        parseIdentifier();
                    }
                    if (match(TokenType.COMMA)) {
                        eat(TokenType.COMMA);
                    }
                }
                eat(TokenType.RPAREN);
                eat(TokenType.SEMICOLON);
                return new TypedefDeclaration(type, alias);
            }
            Identifier alias = parseIdentifier();
            eat(TokenType.SEMICOLON);
            return new TypedefDeclaration(type, alias);
        }
    }

    private FunctionDeclaration parseFunctionDeclaration(Type returnType, Identifier name) {
        eat(TokenType.LPAREN);
        List<VariableDeclaration> parameters = new ArrayList<>();
        if (!match(TokenType.RPAREN)) {
            if (match(TokenType.DOT) && peek() != null && peek().getType() == TokenType.DOT) {
                eat(TokenType.DOT);
                eat(TokenType.DOT);
                eat(TokenType.DOT);
            } else {
                Type paramType = parseType();
                Identifier paramName = parseIdentifier();
                if (match(TokenType.LBRACKET)) {
                    eat(TokenType.LBRACKET);
                    Integer arraySize = null;
                    if (match(TokenType.NUMBER)) {
                        arraySize = Integer.parseInt(currentToken.getValue());
                        eat(TokenType.NUMBER);
                    }
                    eat(TokenType.RBRACKET);
                    paramType = new Type(paramType.getName(), paramType.getPointerLevel(), true, arraySize);
                }
                parameters.add(new VariableDeclaration(paramType, paramName));
                while (match(TokenType.COMMA)) {
                    eat(TokenType.COMMA);
                    if (match(TokenType.DOT) && peek() != null && peek().getType() == TokenType.DOT) {
                        eat(TokenType.DOT);
                        eat(TokenType.DOT);
                        eat(TokenType.DOT);
                        break;
                    }
                    paramType = parseType();
                    paramName = parseIdentifier();
                    if (match(TokenType.LBRACKET)) {
                        eat(TokenType.LBRACKET);
                        Integer arraySize = null;
                        if (match(TokenType.NUMBER)) {
                            arraySize = Integer.parseInt(currentToken.getValue());
                            eat(TokenType.NUMBER);
                        }
                        eat(TokenType.RBRACKET);
                        paramType = new Type(paramType.getName(), paramType.getPointerLevel(), true, arraySize);
                    }
                    parameters.add(new VariableDeclaration(paramType, paramName));
                }
            }
        }
        eat(TokenType.RPAREN);
        if (match(TokenType.SEMICOLON)) {
            eat(TokenType.SEMICOLON);
            return new FunctionDeclaration(returnType, name, parameters, null);
        }
        Block body = parseBlock();
        return new FunctionDeclaration(returnType, name, parameters, body);
    }

    private VariableDeclaration parseVariableDeclarationHelper(Type type, Identifier name) {
        while (match(TokenType.MUL)) {
            eat(TokenType.MUL);
            type = new Type(type.getName(), type.getPointerLevel() + 1, false, null);
        }
        if (match(TokenType.LBRACKET)) {
            eat(TokenType.LBRACKET);
            Integer arraySize = null;
            if (match(TokenType.NUMBER)) {
                arraySize = Integer.parseInt(currentToken.getValue());
                eat(TokenType.NUMBER);
            }
            eat(TokenType.RBRACKET);
            type = new Type(type.getName(), type.getPointerLevel(), true, arraySize);
        }
        AstNode initializer = null;
        if (match(TokenType.ASSIGN)) {
            eat(TokenType.ASSIGN);
            initializer = parseExpression();
        }
        return new VariableDeclaration(type, name, initializer);
    }
    
    private VariableDeclaration parseVariableDeclaration(Type type, Identifier name) {
        VariableDeclaration var = parseVariableDeclarationHelper(type, name);
        eat(TokenType.SEMICOLON);
        return var;
    }

    private Type parseType() {
        StringBuilder typeName = new StringBuilder();
        if (match(TokenType.CONST)) {
            typeName.append("const ");
            eat(TokenType.CONST);
        }
        if (match(TokenType.UNSIGNED)) {
            typeName.append("unsigned ");
            eat(TokenType.UNSIGNED);
        }
        if (match(TokenType.STRUCT)) {
            eat(TokenType.STRUCT);
            typeName.append("struct ").append(currentToken.getValue());
            eat(TokenType.IDENTIFIER);
        } else if (match(TokenType.ENUM)) {
            eat(TokenType.ENUM);
            typeName.append("enum ").append(currentToken.getValue());
            eat(TokenType.IDENTIFIER);
        } else if (match(TokenType.UNION)) {
            eat(TokenType.UNION);
            typeName.append("union ").append(currentToken.getValue());
            eat(TokenType.IDENTIFIER);
        } else if (matchAny(TokenType.INT, TokenType.LONG, TokenType.SHORT, TokenType.CHAR, TokenType.FLOAT, TokenType.DOUBLE, TokenType.VOID, TokenType.BOOL, TokenType.SIZE_T)) {
            typeName.append(currentToken.getValue());
            eat(currentToken.getType());
            while (matchAny(TokenType.LONG, TokenType.INT)) {
                typeName.append(" ").append(currentToken.getValue());
                eat(currentToken.getType());
            }
        } else if (match(TokenType.IDENTIFIER)) {
            typeName.append(currentToken.getValue());
            eat(TokenType.IDENTIFIER);
        } else {
            throw new RuntimeException("Expected type but got " + currentToken.getType() + " at line " + currentToken.getLine());
        }
        
        int pointerLevel = 0;
        while (match(TokenType.MUL)) {
            eat(TokenType.MUL);
            pointerLevel++;
        }
        
        return new Type(typeName.toString(), pointerLevel, false, null);
    }

    private Identifier parseIdentifier() {
        Token token = currentToken;
        eat(TokenType.IDENTIFIER);
        return new Identifier(token.getValue());
    }

    private Block parseBlock() {
        eat(TokenType.LBRACE);
        List<AstNode> statements = new ArrayList<>();
        while (!match(TokenType.RBRACE)) {
            statements.add(parseStatement());
        }
        eat(TokenType.RBRACE);
        return new Block(statements);
    }

    private Block parseStatementAsBlock() {
        if (match(TokenType.LBRACE)) {
            return parseBlock();
        } else {
            List<AstNode> statements = new ArrayList<>();
            statements.add(parseStatement());
            return new Block(statements);
        }
    }

    private AstNode parseStatement() {
        if (match(TokenType.IF)) {
            return parseIfStatement();
        } else if (match(TokenType.WHILE)) {
            return parseWhileStatement();
        } else if (match(TokenType.FOR)) {
            return parseForStatement();
        } else if (match(TokenType.DO)) {
            return parseDoWhileStatement();
        } else if (match(TokenType.SWITCH)) {
            return parseSwitchStatement();
        } else if (match(TokenType.BREAK)) {
            eat(TokenType.BREAK);
            eat(TokenType.SEMICOLON);
            return new BreakStatement();
        } else if (match(TokenType.CONTINUE)) {
            eat(TokenType.CONTINUE);
            eat(TokenType.SEMICOLON);
            return new ContinueStatement();
        } else if (match(TokenType.RETURN)) {
            return parseReturnStatement();
        } else if (match(TokenType.LBRACE)) {
            return parseBlock();
        } else if (match(TokenType.STRUCT)) {
            eat(TokenType.STRUCT);
            if (match(TokenType.LBRACE)) {
                return parseStructDeclaration();
            } else if (match(TokenType.IDENTIFIER)) {
                Identifier structName = parseIdentifier();
                Type type = new Type("struct " + structName.getName(), 0, false, null);
                Identifier name = parseIdentifier();
                return parseVariableDeclaration(type, name);
            } else {
                throw new RuntimeException("Unexpected token " + currentToken.getType() + " after struct at line " + currentToken.getLine());
            }
        } else if (matchAny(TokenType.INT, TokenType.LONG, TokenType.SHORT, TokenType.CHAR, TokenType.FLOAT, TokenType.DOUBLE, TokenType.VOID, TokenType.BOOL, TokenType.SIZE_T, TokenType.UNSIGNED, TokenType.CONST)) {
            Type type = parseType();
            Identifier name = parseIdentifier();
            if (match(TokenType.LPAREN)) {
                return parseFunctionDeclaration(type, name);
            } else {
                List<VariableDeclaration> vars = new ArrayList<>();
                vars.add(parseVariableDeclarationHelper(type, name));
                while (match(TokenType.COMMA)) {
                    eat(TokenType.COMMA);
                    Identifier nextName = parseIdentifier();
                    vars.add(parseVariableDeclarationHelper(type, nextName));
                }
                eat(TokenType.SEMICOLON);
                if (vars.size() == 1) {
                    return vars.get(0);
                } else {
                    List<AstNode> statements = new ArrayList<>();
                    statements.addAll(vars);
                    return new Block(statements);
                }
            }
        } else if (currentToken.getType() == TokenType.IDENTIFIER) {
            Token typeToken = currentToken;
            eat(TokenType.IDENTIFIER);
            
            if (match(TokenType.MUL)) {
                eat(TokenType.MUL);
                Type type = new Type(typeToken.getValue(), 1, false, null);
                Identifier name = parseIdentifier();
                AstNode initializer = null;
                if (match(TokenType.ASSIGN)) {
                    eat(TokenType.ASSIGN);
                    initializer = parseExpression();
                }
                eat(TokenType.SEMICOLON);
                return new VariableDeclaration(type, name, initializer);
            } else if (currentToken.getType() == TokenType.IDENTIFIER) {
                int pointerLevel = 0;
                while (match(TokenType.MUL)) {
                    eat(TokenType.MUL);
                    pointerLevel++;
                }
                Type type = new Type(typeToken.getValue(), pointerLevel, false, null);
                Identifier name = parseIdentifier();
                AstNode initializer = null;
                if (match(TokenType.ASSIGN)) {
                    eat(TokenType.ASSIGN);
                    initializer = parseExpression();
                }
                eat(TokenType.SEMICOLON);
                return new VariableDeclaration(type, name, initializer);
            } else {
                AstNode left = new Identifier(typeToken.getValue());
                AstNode postfix = parsePostfixExpression(left);
                AstNode expr = parseAssignmentWithLeft(postfix);
                eat(TokenType.SEMICOLON);
                return new ExpressionStatement(expr);
            }
        } else {
            AstNode expr = parseExpression();
            eat(TokenType.SEMICOLON);
            return new ExpressionStatement(expr);
        }
    }

    private IfStatement parseIfStatement() {
        eat(TokenType.IF);
        eat(TokenType.LPAREN);
        AstNode condition = parseExpression();
        eat(TokenType.RPAREN);
        Block thenBlock = parseStatementAsBlock();
        Block elseBlock = null;
        if (match(TokenType.ELSE)) {
            eat(TokenType.ELSE);
            elseBlock = parseStatementAsBlock();
        }
        return new IfStatement(condition, thenBlock, elseBlock);
    }

    private WhileStatement parseWhileStatement() {
        eat(TokenType.WHILE);
        eat(TokenType.LPAREN);
        AstNode condition = parseExpression();
        eat(TokenType.RPAREN);
        Block body = parseStatementAsBlock();
        return new WhileStatement(condition, body);
    }

    private ForStatement parseForStatement() {
        eat(TokenType.FOR);
        eat(TokenType.LPAREN);
        AstNode init = null;
        if (!match(TokenType.SEMICOLON)) {
            if (matchAny(TokenType.INT, TokenType.LONG, TokenType.SHORT, TokenType.CHAR, TokenType.FLOAT, TokenType.DOUBLE, TokenType.BOOL, TokenType.SIZE_T)) {
                Type type = parseType();
                Identifier name = parseIdentifier();
                AstNode initializer = null;
                if (match(TokenType.ASSIGN)) {
                    eat(TokenType.ASSIGN);
                    initializer = parseExpression();
                }
                init = new VariableDeclaration(type, name, initializer);
            } else {
                init = parseExpression();
            }
        }
        eat(TokenType.SEMICOLON);
        AstNode condition = null;
        if (!match(TokenType.SEMICOLON)) {
            condition = parseExpression();
        }
        eat(TokenType.SEMICOLON);
        AstNode update = null;
        if (!match(TokenType.RPAREN)) {
            update = parseExpression();
        }
        eat(TokenType.RPAREN);
        Block body = parseStatementAsBlock();
        return new ForStatement(init, condition, update, body);
    }

    private DoWhileStatement parseDoWhileStatement() {
        eat(TokenType.DO);
        Block body = parseStatementAsBlock();
        eat(TokenType.WHILE);
        eat(TokenType.LPAREN);
        AstNode condition = parseExpression();
        eat(TokenType.RPAREN);
        eat(TokenType.SEMICOLON);
        return new DoWhileStatement(body, condition);
    }

    private AstNode parseExpressionStatement(AstNode startExpr) {
        AstNode expr = parseAssignment();
        eat(TokenType.SEMICOLON);
        return new ExpressionStatement(expr);
    }

    private SwitchStatement parseSwitchStatement() {
        eat(TokenType.SWITCH);
        eat(TokenType.LPAREN);
        AstNode expression = parseExpression();
        eat(TokenType.RPAREN);
        eat(TokenType.LBRACE);
        List<AstNode> cases = new ArrayList<>();
        while (!match(TokenType.RBRACE)) {
            if (match(TokenType.CASE)) {
                cases.add(parseCaseStatement());
            } else if (match(TokenType.DEFAULT)) {
                cases.add(parseDefaultStatement());
            } else {
                throw new RuntimeException("Unexpected token " + currentToken.getType() + " in switch statement");
            }
        }
        eat(TokenType.RBRACE);
        return new SwitchStatement(expression, cases);
    }

    private CaseStatement parseCaseStatement() {
        eat(TokenType.CASE);
        AstNode value = parseExpression();
        eat(TokenType.COLON);
        List<AstNode> statements = new ArrayList<>();
        while (!matchAny(TokenType.CASE, TokenType.DEFAULT, TokenType.RBRACE)) {
            statements.add(parseStatement());
        }
        return new CaseStatement(value, statements);
    }

    private DefaultStatement parseDefaultStatement() {
        eat(TokenType.DEFAULT);
        eat(TokenType.COLON);
        List<AstNode> statements = new ArrayList<>();
        while (!matchAny(TokenType.CASE, TokenType.DEFAULT, TokenType.RBRACE)) {
            statements.add(parseStatement());
        }
        return new DefaultStatement(statements);
    }

    private ReturnStatement parseReturnStatement() {
        eat(TokenType.RETURN);
        AstNode expression = null;
        if (!match(TokenType.SEMICOLON)) {
            expression = parseExpression();
        }
        eat(TokenType.SEMICOLON);
        return new ReturnStatement(expression);
    }

    private AstNode parseExpression() {
        return parseAssignment();
    }

    private AstNode parseAssignment() {
        AstNode left = parseLogicalOr();
        if (match(TokenType.QUESTION)) {
            eat(TokenType.QUESTION);
            AstNode trueExpr = parseExpression();
            eat(TokenType.COLON);
            AstNode falseExpr = parseAssignment();
            return new TernaryExpression(left, trueExpr, falseExpr);
        }
        return parseAssignmentWithLeft(left);
    }
    
    private AstNode parseAssignmentWithLeft(AstNode left) {
        if (matchAny(TokenType.ASSIGN, TokenType.PLUS_ASSIGN, TokenType.MINUS_ASSIGN, TokenType.MUL_ASSIGN, TokenType.DIV_ASSIGN, TokenType.MOD_ASSIGN,
                TokenType.BIT_AND_ASSIGN, TokenType.BIT_OR_ASSIGN, TokenType.BIT_XOR_ASSIGN, TokenType.SHL_ASSIGN, TokenType.SHR_ASSIGN)) {
            String operator = currentToken.getValue();
            eat(currentToken.getType());
            AstNode right = parseAssignment();
            return new Assignment(left, operator, right);
        }
        return left;
    }
    
    private AstNode parseLogicalOr() {
        AstNode left = parseLogicalAnd();
        return parseLogicalOrWithLeft(left);
    }
    
    private AstNode parseLogicalOrWithLeft(AstNode left) {
        while (match(TokenType.OR)) {
            String operator = currentToken.getValue();
            eat(TokenType.OR);
            AstNode right = parseLogicalAnd();
            left = new BinaryExpression(left, operator, right);
        }
        return left;
    }

    private AstNode parseLogicalAnd() {
        AstNode left = parseBitwiseOr();
        while (match(TokenType.AND)) {
            String operator = currentToken.getValue();
            eat(TokenType.AND);
            AstNode right = parseBitwiseOr();
            left = new BinaryExpression(left, operator, right);
        }
        return left;
    }

    private AstNode parseBitwiseOr() {
        AstNode left = parseBitwiseXor();
        while (match(TokenType.BIT_OR)) {
            String operator = currentToken.getValue();
            eat(TokenType.BIT_OR);
            AstNode right = parseBitwiseXor();
            left = new BinaryExpression(left, operator, right);
        }
        return left;
    }

    private AstNode parseBitwiseXor() {
        AstNode left = parseBitwiseAnd();
        while (match(TokenType.BIT_XOR)) {
            String operator = currentToken.getValue();
            eat(TokenType.BIT_XOR);
            AstNode right = parseBitwiseAnd();
            left = new BinaryExpression(left, operator, right);
        }
        return left;
    }

    private AstNode parseBitwiseAnd() {
        AstNode left = parseEquality();
        while (match(TokenType.BIT_AND)) {
            String operator = currentToken.getValue();
            eat(TokenType.BIT_AND);
            AstNode right = parseEquality();
            left = new BinaryExpression(left, operator, right);
        }
        return left;
    }

    private AstNode parseEquality() {
        AstNode left = parseRelational();
        while (matchAny(TokenType.EQ, TokenType.NEQ)) {
            String operator = currentToken.getValue();
            eat(currentToken.getType());
            AstNode right = parseRelational();
            left = new BinaryExpression(left, operator, right);
        }
        return left;
    }

    private AstNode parseRelational() {
        AstNode left = parseShift();
        while (matchAny(TokenType.LT, TokenType.GT, TokenType.LE, TokenType.GE)) {
            String operator = currentToken.getValue();
            eat(currentToken.getType());
            AstNode right = parseShift();
            left = new BinaryExpression(left, operator, right);
        }
        return left;
    }

    private AstNode parseShift() {
        AstNode left = parseAdditive();
        return parseShiftWithLeft(left);
    }
    
    private AstNode parseShiftWithLeft(AstNode left) {
        while (matchAny(TokenType.SHL, TokenType.SHR)) {
            String operator = currentToken.getValue();
            eat(currentToken.getType());
            AstNode right = parseAdditive();
            left = new BinaryExpression(left, operator, right);
        }
        return left;
    }

    private AstNode parseAdditive() {
        AstNode left = parseMultiplicative();
        while (matchAny(TokenType.PLUS, TokenType.MINUS)) {
            String operator = currentToken.getValue();
            eat(currentToken.getType());
            AstNode right = parseMultiplicative();
            left = new BinaryExpression(left, operator, right);
        }
        return left;
    }

    private AstNode parseMultiplicative() {
        AstNode left = parseUnary();
        while (matchAny(TokenType.MUL, TokenType.DIV, TokenType.MOD)) {
            String operator = currentToken.getValue();
            eat(currentToken.getType());
            AstNode right = parseUnary();
            left = new BinaryExpression(left, operator, right);
        }
        return left;
    }

    private AstNode parseUnary() {
        if (matchAny(TokenType.PLUS, TokenType.MINUS, TokenType.NOT, TokenType.BIT_NOT, TokenType.MUL, TokenType.BIT_AND)) {
            String operator = currentToken.getValue();
            eat(currentToken.getType());
            AstNode operand = parseUnary();
            return new UnaryExpression(operator, operand);
        } else if (match(TokenType.INC)) {
            eat(TokenType.INC);
            AstNode operand = parsePostfix();
            return new UnaryExpression("++", operand, false);
        } else if (match(TokenType.DEC)) {
            eat(TokenType.DEC);
            AstNode operand = parsePostfix();
            return new UnaryExpression("--", operand, false);
        }
        return parsePostfix();
    }

    private AstNode parsePostfix() {
        AstNode left = parsePrimary();
        return parsePostfixExpression(left);
    }
    
    private AstNode parsePostfixExpression(AstNode left) {
        while (true) {
            if (match(TokenType.LBRACKET)) {
                eat(TokenType.LBRACKET);
                AstNode index = parseExpression();
                eat(TokenType.RBRACKET);
                left = new ArrayAccess(left, index);
            } else if (match(TokenType.LPAREN)) {
                eat(TokenType.LPAREN);
                List<AstNode> arguments = new ArrayList<>();
                if (!match(TokenType.RPAREN)) {
                    if (left instanceof Identifier && ((Identifier) left).getName().equals("va_arg")) {
                        arguments.add(parseExpression());
                        eat(TokenType.COMMA);
                        Type type = parseType();
                        arguments.add(new Identifier(type.getName()));
                    } else {
                        arguments.add(parseExpression());
                        while (match(TokenType.COMMA)) {
                            eat(TokenType.COMMA);
                            arguments.add(parseExpression());
                        }
                    }
                }
                eat(TokenType.RPAREN);
                if (left instanceof Identifier) {
                    left = new FunctionCall((Identifier) left, arguments);
                } else {
                    left = new FunctionCall(new Identifier(left.toString()), arguments);
                }
            } else if (match(TokenType.DOT)) {
                eat(TokenType.DOT);
                Identifier field = parseIdentifier();
                left = new StructAccess(left, field);
            } else if (match(TokenType.STRUCT_ACCESS)) {
                eat(TokenType.STRUCT_ACCESS);
                Identifier field = parseIdentifier();
                left = new StructAccess(left, field);
            } else if (match(TokenType.INC)) {
                eat(TokenType.INC);
                left = new UnaryExpression("++", left, true);
            } else if (match(TokenType.DEC)) {
                eat(TokenType.DEC);
                left = new UnaryExpression("--", left, true);
            } else {
                break;
            }
        }
        return left;
    }

    private AstNode parsePrimary() {
        if (match(TokenType.IDENTIFIER)) {
            return parseIdentifier();
        } else if (match(TokenType.NUMBER)) {
            Token token = currentToken;
            eat(TokenType.NUMBER);
            String value = token.getValue();
            if (value.contains(".")) {
                return new Literal(value, Literal.LiteralType.FLOAT);
            }
            return new Literal(value, Literal.LiteralType.INTEGER);
        } else if (match(TokenType.STRING)) {
            Token token = currentToken;
            eat(TokenType.STRING);
            return new Literal(token.getValue(), Literal.LiteralType.STRING);
        } else if (match(TokenType.CHAR_LITERAL)) {
            Token token = currentToken;
            eat(TokenType.CHAR_LITERAL);
            return new Literal(token.getValue(), Literal.LiteralType.CHARACTER);
        } else if (match(TokenType.NULL)) {
            eat(TokenType.NULL);
            return new Literal("null", Literal.LiteralType.NULL);
        } else if (match(TokenType.SIZEOF)) {
            eat(TokenType.SIZEOF);
            eat(TokenType.LPAREN);
            AstNode argument;
            if (matchAny(TokenType.INT, TokenType.LONG, TokenType.SHORT, TokenType.CHAR, 
                         TokenType.FLOAT, TokenType.DOUBLE, TokenType.VOID, TokenType.BOOL, 
                         TokenType.CONST, TokenType.UNSIGNED, TokenType.STRUCT, TokenType.ENUM, TokenType.UNION)) {
                Type type = parseType();
                argument = new Identifier(type.getName());
            } else {
                argument = parsePrimary();
            }
            eat(TokenType.RPAREN);
            return new FunctionCall(new Identifier("sizeof"), java.util.Arrays.asList(argument));
        } else if (match(TokenType.LBRACE)) {
            eat(TokenType.LBRACE);
            List<AstNode> elements = new ArrayList<>();
            if (!match(TokenType.RBRACE)) {
                elements.add(parseExpression());
                while (match(TokenType.COMMA)) {
                    eat(TokenType.COMMA);
                    elements.add(parseExpression());
                }
            }
            eat(TokenType.RBRACE);
            return new ArrayInitializer(elements);
        } else if (match(TokenType.LPAREN)) {
            eat(TokenType.LPAREN);
            
            if (isTypeStart(currentToken) || (currentToken.getType() == TokenType.IDENTIFIER && peek() != null && peek().getType() == TokenType.MUL)) {
                try {
                    Type type = parseType();
                    if (match(TokenType.RPAREN)) {
                        eat(TokenType.RPAREN);
                        return new TypeCastExpression(type, parseExpression());
                    }
                } catch (Exception e) {
                }
            }
            
            AstNode expr = parseExpression();
            eat(TokenType.RPAREN);
            return expr;
        } else {
            throw new RuntimeException("Unexpected token " + currentToken.getType() + " at line " + currentToken.getLine());
        }
    }

    private AstNode parseMacroDeclaration() {
        eat(TokenType.DEFINE);
        Identifier name = parseIdentifier();
        
        List<Identifier> parameters = new ArrayList<>();
        
        if (match(TokenType.LPAREN)) {
            eat(TokenType.LPAREN);
            if (!match(TokenType.RPAREN)) {
                parameters.add(parseIdentifier());
                while (match(TokenType.COMMA)) {
                    eat(TokenType.COMMA);
                    parameters.add(parseIdentifier());
                }
            }
            eat(TokenType.RPAREN);
        }
        
        StringBuilder bodyText = new StringBuilder();
        int parenCount = 0;
        
        while (!match(TokenType.EOF) && !match(TokenType.SEMICOLON)) {
            if (match(TokenType.LPAREN)) {
                parenCount++;
                bodyText.append("(");
                eat(TokenType.LPAREN);
            } else if (match(TokenType.RPAREN)) {
                parenCount--;
                bodyText.append(")");
                eat(TokenType.RPAREN);
                if (parenCount == 0 && !parameters.isEmpty()) {
                    break;
                }
            } else {
                bodyText.append(currentToken.getValue());
                eat(currentToken.getType());
            }
        }
        
        if (match(TokenType.SEMICOLON)) {
            eat(TokenType.SEMICOLON);
        }
        
        AstNode body = new Literal(bodyText.toString().trim(), Literal.LiteralType.STRING);
        
        return new MacroDeclaration(name, parameters, body);
    }
}