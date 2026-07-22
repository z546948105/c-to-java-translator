package com.translator.parser;

import com.translator.ast.*;
import com.translator.token.Token;
import com.translator.token.TokenType;

import java.util.ArrayList;
import java.util.List;

class DeclarationParser {
    private final ParserBase base;
    private final TypeParser typeParser;
    private final StatementParser statementParser;

    DeclarationParser(ParserBase base, TypeParser typeParser, StatementParser statementParser) {
        this.base = base;
        this.typeParser = typeParser;
        this.statementParser = statementParser;
    }

    AstNode parseDeclaration() {
        boolean isStatic = false;
        if (base.currentToken.getType() == TokenType.STATIC) {
            base.eat(TokenType.STATIC);
            isStatic = true;
        }
        if (base.currentToken.getType() == TokenType.TYPEDEF) {
            return parseTypedefDeclaration();
        } else if (base.currentToken.getType() == TokenType.STRUCT) {
            return parseStructDeclarationStart();
        } else if (base.currentToken.getType() == TokenType.ENUM) {
            return parseEnumDeclarationStart();
        } else if (base.currentToken.getType() == TokenType.UNION) {
            return parseUnionDeclarationStart();
        } else if (base.matchAny(TokenType.IF, TokenType.WHILE, TokenType.FOR, TokenType.DO, TokenType.SWITCH, TokenType.BREAK, TokenType.CONTINUE, TokenType.RETURN, TokenType.LBRACE)) {
            return statementParser.parseStatement();
        } else if (base.match(TokenType.RBRACE)) {
            base.eat(TokenType.RBRACE);
            return null;
        } else if (!base.isTypeStart(base.currentToken)) {
            return base.createUnsupportedCode("Expected type keyword but got: " + base.currentToken.getType());
        }
        
        if (base.currentToken.getType() == TokenType.IDENTIFIER) {
            Token next = base.peek();
            if (next != null && next.getType() != TokenType.MUL && next.getType() != TokenType.LPAREN) {
                return statementParser.parseStatement();
            }
        }
        
        Type type = typeParser.parseType();
        
        if (base.match(TokenType.LPAREN)) {
            base.eat(TokenType.LPAREN);
            if (base.match(TokenType.MUL)) {
                return parseFunctionPointerDeclaration(type);
            }
            base.eat(TokenType.RPAREN);
        }
        
        Identifier name = typeParser.parseIdentifier();
        if (base.currentToken.getType() == TokenType.LPAREN) {
            return parseFunctionDeclaration(type, name);
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

    private AstNode parseFunctionPointerDeclaration(Type type) {
        base.eat(TokenType.MUL);
        Identifier ptrName = typeParser.parseIdentifier();
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
        
        FunctionPointerType fpType = new FunctionPointerType(type, paramTypes);
        AstNode initializer = null;
        if (base.match(TokenType.ASSIGN)) {
            base.eat(TokenType.ASSIGN);
            initializer = statementParser.parseExpression();
        }
        base.eat(TokenType.SEMICOLON);
        return new VariableDeclaration(fpType, ptrName, initializer);
    }

    private AstNode parseStructDeclarationStart() {
        base.eat(TokenType.STRUCT);
        if (base.match(TokenType.LBRACE)) {
            return parseStructDeclaration(null);
        } else if (base.match(TokenType.IDENTIFIER)) {
            Identifier structName = typeParser.parseIdentifier();
            if (base.match(TokenType.LBRACE)) {
                return parseStructDeclaration(structName);
            } else {
                Type type = new Type("struct " + structName.getName(), 0, false, null);
                if (base.match(TokenType.LPAREN)) {
                    return parseFunctionDeclaration(type, structName);
                } else {
                    return typeParser.parseVariableDeclaration(type, structName);
                }
            }
        } else {
            return base.createUnsupportedCode("Unexpected token after struct: " + base.currentToken.getType());
        }
    }

    private AstNode parseEnumDeclarationStart() {
        base.eat(TokenType.ENUM);
        if (base.match(TokenType.LBRACE)) {
            return parseEnumDeclaration(null);
        } else if (base.match(TokenType.IDENTIFIER)) {
            Identifier enumName = typeParser.parseIdentifier();
            if (base.match(TokenType.LBRACE)) {
                return parseEnumDeclaration(enumName);
            } else {
                Type type = new Type("enum " + enumName.getName(), 0, false, null);
                if (base.match(TokenType.LPAREN)) {
                    return parseFunctionDeclaration(type, enumName);
                } else {
                    return typeParser.parseVariableDeclaration(type, enumName);
                }
            }
        } else {
            return base.createUnsupportedCode("Unexpected token after enum: " + base.currentToken.getType());
        }
    }

    private AstNode parseUnionDeclarationStart() {
        base.eat(TokenType.UNION);
        if (base.match(TokenType.LBRACE)) {
            return parseUnionDeclaration(null);
        } else if (base.match(TokenType.IDENTIFIER)) {
            Identifier unionName = typeParser.parseIdentifier();
            if (base.match(TokenType.LBRACE)) {
                return parseUnionDeclaration(unionName);
            } else {
                Type type = new Type("union " + unionName.getName(), 0, false, null);
                if (base.match(TokenType.LPAREN)) {
                    return parseFunctionDeclaration(type, unionName);
                } else {
                    return typeParser.parseVariableDeclaration(type, unionName);
                }
            }
        } else {
            return base.createUnsupportedCode("Unexpected token after union: " + base.currentToken.getType());
        }
    }

    StructDeclaration parseStructDeclaration(Identifier name) {
        base.eat(TokenType.LBRACE);
        List<VariableDeclaration> fields = new ArrayList<>();
        while (!base.match(TokenType.RBRACE)) {
            Type type = typeParser.parseType();
            Identifier fieldName = typeParser.parseIdentifier();
            parseFieldArrayAndInitializer(type, fieldName, fields);
        }
        base.eat(TokenType.RBRACE);
        base.eat(TokenType.SEMICOLON);
        return new StructDeclaration(name, fields);
    }

    StructDeclaration parseUnionDeclaration(Identifier name) {
        base.eat(TokenType.LBRACE);
        List<VariableDeclaration> fields = new ArrayList<>();
        while (!base.match(TokenType.RBRACE)) {
            Type type = typeParser.parseType();
            Identifier fieldName = typeParser.parseIdentifier();
            parseFieldArrayAndInitializer(type, fieldName, fields);
        }
        base.eat(TokenType.RBRACE);
        base.eat(TokenType.SEMICOLON);
        return new StructDeclaration(name, fields);
    }

    private void parseFieldArrayAndInitializer(Type type, Identifier fieldName, List<VariableDeclaration> fields) {
        if (base.match(TokenType.LBRACKET)) {
            base.eat(TokenType.LBRACKET);
            Integer arraySize = null;
            if (base.match(TokenType.NUMBER)) {
                arraySize = Integer.parseInt(base.currentToken.getValue());
                base.eat(TokenType.NUMBER);
            }
            base.eat(TokenType.RBRACKET);
            type = new Type(type.getName(), type.getPointerLevel(), true, arraySize);
        }
        if (base.match(TokenType.COLON)) {
            base.eat(TokenType.COLON);
            if (base.match(TokenType.NUMBER)) {
                base.eat(TokenType.NUMBER);
            }
        }
        AstNode initializer = null;
        if (base.match(TokenType.ASSIGN)) {
            base.eat(TokenType.ASSIGN);
            initializer = statementParser.parseExpression();
        }
        base.eat(TokenType.SEMICOLON);
        fields.add(new VariableDeclaration(type, fieldName, initializer));
    }

    EnumDeclaration parseEnumDeclaration(Identifier name) {
        base.eat(TokenType.LBRACE);
        List<Identifier> values = new ArrayList<>();
        while (!base.match(TokenType.RBRACE)) {
            values.add(typeParser.parseIdentifier());
            if (base.match(TokenType.COMMA)) {
                base.eat(TokenType.COMMA);
            }
        }
        base.eat(TokenType.RBRACE);
        base.eat(TokenType.SEMICOLON);
        return new EnumDeclaration(name, values);
    }

    FunctionDeclaration parseFunctionDeclaration(Type returnType, Identifier name) {
        base.eat(TokenType.LPAREN);
        List<VariableDeclaration> parameters = new ArrayList<>();
        if (!base.match(TokenType.RPAREN)) {
            if (base.match(TokenType.DOT) && base.peek() != null && base.peek().getType() == TokenType.DOT) {
                base.eat(TokenType.DOT);
                base.eat(TokenType.DOT);
                base.eat(TokenType.DOT);
            } else {
                parseParameter(parameters);
                while (base.match(TokenType.COMMA)) {
                    base.eat(TokenType.COMMA);
                    if (base.match(TokenType.DOT) && base.peek() != null && base.peek().getType() == TokenType.DOT) {
                        base.eat(TokenType.DOT);
                        base.eat(TokenType.DOT);
                        base.eat(TokenType.DOT);
                        break;
                    }
                    parseParameter(parameters);
                }
            }
        }
        base.eat(TokenType.RPAREN);
        if (base.match(TokenType.SEMICOLON)) {
            base.eat(TokenType.SEMICOLON);
            return new FunctionDeclaration(returnType, name, parameters, null);
        }
        Block body = statementParser.parseBlock();
        return new FunctionDeclaration(returnType, name, parameters, body);
    }

    private void parseParameter(List<VariableDeclaration> parameters) {
        Type paramType = typeParser.parseType();
        Identifier paramName = typeParser.parseIdentifier();
        if (base.match(TokenType.LBRACKET)) {
            base.eat(TokenType.LBRACKET);
            Integer arraySize = null;
            if (base.match(TokenType.NUMBER)) {
                arraySize = Integer.parseInt(base.currentToken.getValue());
                base.eat(TokenType.NUMBER);
            }
            base.eat(TokenType.RBRACKET);
            paramType = new Type(paramType.getName(), paramType.getPointerLevel(), true, arraySize);
        }
        parameters.add(new VariableDeclaration(paramType, paramName));
    }

    AstNode parseTypedefDeclaration() {
        base.eat(TokenType.TYPEDEF);
        if (base.match(TokenType.STRUCT)) {
            return parseTypedefStruct();
        } else if (base.match(TokenType.UNION)) {
            return parseTypedefUnion();
        } else if (base.match(TokenType.ENUM)) {
            return parseTypedefEnum();
        } else {
            return parseTypedefBasic();
        }
    }

    private AstNode parseTypedefStruct() {
        base.eat(TokenType.STRUCT);
        Identifier structName = null;
        if (base.match(TokenType.IDENTIFIER)) {
            structName = typeParser.parseIdentifier();
        }
        base.eat(TokenType.LBRACE);
        List<VariableDeclaration> fields = new ArrayList<>();
        while (!base.match(TokenType.RBRACE)) {
            Type type = typeParser.parseType();
            Identifier fieldName = typeParser.parseIdentifier();
            parseFieldArrayAndInitializer(type, fieldName, fields);
        }
        base.eat(TokenType.RBRACE);
        Identifier alias = structName;
        if (base.match(TokenType.IDENTIFIER)) {
            alias = typeParser.parseIdentifier();
        }
        base.eat(TokenType.SEMICOLON);
        StructDeclaration structDecl = new StructDeclaration(alias, fields);
        return new TypedefDeclaration(structDecl, alias);
    }

    private AstNode parseTypedefUnion() {
        base.eat(TokenType.UNION);
        Identifier unionName = null;
        if (base.match(TokenType.IDENTIFIER)) {
            unionName = typeParser.parseIdentifier();
        }
        base.eat(TokenType.LBRACE);
        List<VariableDeclaration> fields = new ArrayList<>();
        while (!base.match(TokenType.RBRACE)) {
            Type type = typeParser.parseType();
            Identifier fieldName = typeParser.parseIdentifier();
            parseFieldArrayAndInitializer(type, fieldName, fields);
        }
        base.eat(TokenType.RBRACE);
        Identifier alias = unionName;
        if (base.match(TokenType.IDENTIFIER)) {
            alias = typeParser.parseIdentifier();
        }
        base.eat(TokenType.SEMICOLON);
        StructDeclaration unionDecl = new StructDeclaration(alias, fields);
        return new TypedefDeclaration(unionDecl, alias);
    }

    private AstNode parseTypedefEnum() {
        base.eat(TokenType.ENUM);
        Identifier enumName = null;
        if (base.match(TokenType.IDENTIFIER)) {
            enumName = typeParser.parseIdentifier();
        }
        base.eat(TokenType.LBRACE);
        List<Identifier> values = new ArrayList<>();
        while (!base.match(TokenType.RBRACE)) {
            values.add(typeParser.parseIdentifier());
            if (base.match(TokenType.COMMA)) {
                base.eat(TokenType.COMMA);
            }
        }
        base.eat(TokenType.RBRACE);
        Identifier alias = enumName;
        if (base.match(TokenType.IDENTIFIER)) {
            alias = typeParser.parseIdentifier();
        }
        base.eat(TokenType.SEMICOLON);
        EnumDeclaration enumDecl = new EnumDeclaration(alias, values);
        return new TypedefDeclaration(enumDecl, alias);
    }

    private AstNode parseTypedefBasic() {
        Type type = typeParser.parseType();
        if (base.match(TokenType.LPAREN) && base.peek() != null && base.peek().getType() == TokenType.MUL) {
            base.eat(TokenType.LPAREN);
            base.eat(TokenType.MUL);
            Identifier alias = typeParser.parseIdentifier();
            base.eat(TokenType.RPAREN);
            base.eat(TokenType.LPAREN);
            while (!base.match(TokenType.RPAREN)) {
                typeParser.parseType();
                if (base.match(TokenType.IDENTIFIER)) {
                    typeParser.parseIdentifier();
                }
                if (base.match(TokenType.COMMA)) {
                    base.eat(TokenType.COMMA);
                }
            }
            base.eat(TokenType.RPAREN);
            base.eat(TokenType.SEMICOLON);
            return new TypedefDeclaration(type, alias);
        }
        Identifier alias = typeParser.parseIdentifier();
        base.eat(TokenType.SEMICOLON);
        return new TypedefDeclaration(type, alias);
    }
}