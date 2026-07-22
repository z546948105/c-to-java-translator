package com.translator.parser;

import com.translator.ast.AstNode;
import com.translator.ast.FunctionPointerType;
import com.translator.ast.Identifier;
import com.translator.ast.Type;
import com.translator.ast.VariableDeclaration;
import com.translator.token.Token;
import com.translator.token.TokenType;

import java.util.ArrayList;
import java.util.List;

class TypeParser {
    private final ParserBase base;
    private ExpressionParser expressionParser;

    TypeParser(ParserBase base) {
        this.base = base;
    }

    void setExpressionParser(ExpressionParser expressionParser) {
        this.expressionParser = expressionParser;
    }

    Type parseType() {
        StringBuilder typeName = new StringBuilder();
        if (base.match(TokenType.CONST)) {
            typeName.append("const ");
            base.eat(TokenType.CONST);
        }
        if (base.match(TokenType.UNSIGNED)) {
            typeName.append("unsigned ");
            base.eat(TokenType.UNSIGNED);
        }
        if (base.match(TokenType.STRUCT)) {
            base.eat(TokenType.STRUCT);
            typeName.append("struct ").append(base.currentToken.getValue());
            base.eat(TokenType.IDENTIFIER);
        } else if (base.match(TokenType.ENUM)) {
            base.eat(TokenType.ENUM);
            typeName.append("enum ").append(base.currentToken.getValue());
            base.eat(TokenType.IDENTIFIER);
        } else if (base.match(TokenType.UNION)) {
            base.eat(TokenType.UNION);
            typeName.append("union ").append(base.currentToken.getValue());
            base.eat(TokenType.IDENTIFIER);
        } else if (base.matchAny(TokenType.INT, TokenType.LONG, TokenType.SHORT, TokenType.CHAR, TokenType.FLOAT, TokenType.DOUBLE, TokenType.VOID, TokenType.BOOL, TokenType.SIZE_T)) {
            typeName.append(base.currentToken.getValue());
            base.eat(base.currentToken.getType());
            while (base.matchAny(TokenType.LONG, TokenType.INT)) {
                typeName.append(" ").append(base.currentToken.getValue());
                base.eat(base.currentToken.getType());
            }
        } else if (base.match(TokenType.IDENTIFIER)) {
            typeName.append(base.currentToken.getValue());
            base.eat(TokenType.IDENTIFIER);
        } else {
            throw new RuntimeException("Expected type but got " + base.currentToken.getType() + " at line " + base.currentToken.getLine());
        }
        
        int pointerLevel = 0;
        while (base.match(TokenType.MUL)) {
            base.eat(TokenType.MUL);
            pointerLevel++;
        }
        
        return new Type(typeName.toString(), pointerLevel, false, null);
    }

    Identifier parseIdentifier() {
        Token token = base.currentToken;
        base.eat(TokenType.IDENTIFIER);
        return new Identifier(token.getValue());
    }

    VariableDeclaration parseVariableDeclarationHelper(Type type, Identifier name) {
        if (base.match(TokenType.LPAREN)) {
            base.eat(TokenType.LPAREN);
            if (base.match(TokenType.MUL)) {
                base.eat(TokenType.MUL);
                Identifier ptrName = parseIdentifier();
                base.eat(TokenType.RPAREN);
                
                base.eat(TokenType.LPAREN);
                List<Type> paramTypes = new ArrayList<>();
                if (!base.match(TokenType.RPAREN)) {
                    paramTypes.add(parseType());
                    while (base.match(TokenType.COMMA)) {
                        base.eat(TokenType.COMMA);
                        paramTypes.add(parseType());
                    }
                }
                base.eat(TokenType.RPAREN);
                
                FunctionPointerType fpType = new FunctionPointerType(type, paramTypes);
                AstNode initializer = null;
                if (base.match(TokenType.ASSIGN)) {
                    base.eat(TokenType.ASSIGN);
                    initializer = parseExpression();
                }
                return new VariableDeclaration(fpType, ptrName, initializer);
            }
            base.eat(TokenType.RPAREN);
        }
        
        while (base.match(TokenType.MUL)) {
            base.eat(TokenType.MUL);
            type = new Type(type.getName(), type.getPointerLevel() + 1, false, null);
        }
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
        AstNode initializer = null;
        if (base.match(TokenType.ASSIGN)) {
            base.eat(TokenType.ASSIGN);
            initializer = parseExpression();
        }
        return new VariableDeclaration(type, name, initializer);
    }
    
    VariableDeclaration parseVariableDeclaration(Type type, Identifier name) {
        VariableDeclaration var = parseVariableDeclarationHelper(type, name);
        base.eat(TokenType.SEMICOLON);
        return var;
    }

    private AstNode parseExpression() {
        return expressionParser.parseExpression();
    }
}