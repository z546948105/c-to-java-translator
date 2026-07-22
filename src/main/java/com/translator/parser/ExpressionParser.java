package com.translator.parser;

import com.translator.ast.*;
import com.translator.token.Token;
import com.translator.token.TokenType;

import java.util.ArrayList;
import java.util.List;

class ExpressionParser {
    private final ParserBase base;
    private final TypeParser typeParser;

    ExpressionParser(ParserBase base, TypeParser typeParser) {
        this.base = base;
        this.typeParser = typeParser;
    }

    AstNode parseExpression() {
        return parseAssignment();
    }

    AstNode parseAssignment() {
        AstNode left = parseLogicalOr();
        if (base.match(TokenType.QUESTION)) {
            base.eat(TokenType.QUESTION);
            AstNode trueExpr = parseExpression();
            base.eat(TokenType.COLON);
            AstNode falseExpr = parseAssignment();
            return new TernaryExpression(left, trueExpr, falseExpr);
        }
        return parseAssignmentWithLeft(left);
    }
    
    AstNode parseAssignmentWithLeft(AstNode left) {
        if (base.matchAny(TokenType.ASSIGN, TokenType.PLUS_ASSIGN, TokenType.MINUS_ASSIGN, TokenType.MUL_ASSIGN, TokenType.DIV_ASSIGN, TokenType.MOD_ASSIGN,
                TokenType.BIT_AND_ASSIGN, TokenType.BIT_OR_ASSIGN, TokenType.BIT_XOR_ASSIGN, TokenType.SHL_ASSIGN, TokenType.SHR_ASSIGN)) {
            String operator = base.currentToken.getValue();
            base.eat(base.currentToken.getType());
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
        while (base.match(TokenType.OR)) {
            String operator = base.currentToken.getValue();
            base.eat(TokenType.OR);
            AstNode right = parseLogicalAnd();
            left = new BinaryExpression(left, operator, right);
        }
        return left;
    }

    private AstNode parseLogicalAnd() {
        AstNode left = parseBitwiseOr();
        while (base.match(TokenType.AND)) {
            String operator = base.currentToken.getValue();
            base.eat(TokenType.AND);
            AstNode right = parseBitwiseOr();
            left = new BinaryExpression(left, operator, right);
        }
        return left;
    }

    private AstNode parseBitwiseOr() {
        AstNode left = parseBitwiseXor();
        while (base.match(TokenType.BIT_OR)) {
            String operator = base.currentToken.getValue();
            base.eat(TokenType.BIT_OR);
            AstNode right = parseBitwiseXor();
            left = new BinaryExpression(left, operator, right);
        }
        return left;
    }

    private AstNode parseBitwiseXor() {
        AstNode left = parseBitwiseAnd();
        while (base.match(TokenType.BIT_XOR)) {
            String operator = base.currentToken.getValue();
            base.eat(TokenType.BIT_XOR);
            AstNode right = parseBitwiseAnd();
            left = new BinaryExpression(left, operator, right);
        }
        return left;
    }

    private AstNode parseBitwiseAnd() {
        AstNode left = parseEquality();
        while (base.match(TokenType.BIT_AND)) {
            String operator = base.currentToken.getValue();
            base.eat(TokenType.BIT_AND);
            AstNode right = parseEquality();
            left = new BinaryExpression(left, operator, right);
        }
        return left;
    }

    private AstNode parseEquality() {
        AstNode left = parseRelational();
        while (base.matchAny(TokenType.EQ, TokenType.NEQ)) {
            String operator = base.currentToken.getValue();
            base.eat(base.currentToken.getType());
            AstNode right = parseRelational();
            left = new BinaryExpression(left, operator, right);
        }
        return left;
    }

    private AstNode parseRelational() {
        AstNode left = parseShift();
        while (base.matchAny(TokenType.LT, TokenType.GT, TokenType.LE, TokenType.GE)) {
            String operator = base.currentToken.getValue();
            base.eat(base.currentToken.getType());
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
        while (base.matchAny(TokenType.SHL, TokenType.SHR)) {
            String operator = base.currentToken.getValue();
            base.eat(base.currentToken.getType());
            AstNode right = parseAdditive();
            left = new BinaryExpression(left, operator, right);
        }
        return left;
    }

    private AstNode parseAdditive() {
        AstNode left = parseMultiplicative();
        while (base.matchAny(TokenType.PLUS, TokenType.MINUS)) {
            String operator = base.currentToken.getValue();
            base.eat(base.currentToken.getType());
            AstNode right = parseMultiplicative();
            left = new BinaryExpression(left, operator, right);
        }
        return left;
    }

    private AstNode parseMultiplicative() {
        AstNode left = parseUnary();
        while (base.matchAny(TokenType.MUL, TokenType.DIV, TokenType.MOD)) {
            String operator = base.currentToken.getValue();
            base.eat(base.currentToken.getType());
            AstNode right = parseUnary();
            left = new BinaryExpression(left, operator, right);
        }
        return left;
    }

    private AstNode parseUnary() {
        if (base.matchAny(TokenType.PLUS, TokenType.MINUS, TokenType.NOT, TokenType.BIT_NOT, TokenType.MUL, TokenType.BIT_AND)) {
            String operator = base.currentToken.getValue();
            base.eat(base.currentToken.getType());
            AstNode operand = parseUnary();
            return new UnaryExpression(operator, operand);
        } else if (base.match(TokenType.INC)) {
            base.eat(TokenType.INC);
            AstNode operand = parsePostfix();
            return new UnaryExpression("++", operand, false);
        } else if (base.match(TokenType.DEC)) {
            base.eat(TokenType.DEC);
            AstNode operand = parsePostfix();
            return new UnaryExpression("--", operand, false);
        }
        return parsePostfix();
    }

    private AstNode parsePostfix() {
        AstNode left = parsePrimary();
        return parsePostfixExpression(left);
    }
    
    AstNode parsePostfixExpression(AstNode left) {
        while (true) {
            if (base.match(TokenType.LBRACKET)) {
                base.eat(TokenType.LBRACKET);
                AstNode index = parseExpression();
                base.eat(TokenType.RBRACKET);
                left = new ArrayAccess(left, index);
            } else if (base.match(TokenType.LPAREN)) {
                base.eat(TokenType.LPAREN);
                List<AstNode> arguments = new ArrayList<>();
                if (!base.match(TokenType.RPAREN)) {
                    if (left instanceof Identifier && ((Identifier) left).getName().equals("va_arg")) {
                        arguments.add(parseExpression());
                        base.eat(TokenType.COMMA);
                        Type type = typeParser.parseType();
                        arguments.add(new Identifier(type.getName()));
                    } else {
                        arguments.add(parseExpression());
                        while (base.match(TokenType.COMMA)) {
                            base.eat(TokenType.COMMA);
                            arguments.add(parseExpression());
                        }
                    }
                }
                base.eat(TokenType.RPAREN);
                if (left instanceof Identifier) {
                    left = new FunctionCall((Identifier) left, arguments);
                } else {
                    left = new FunctionCall(new Identifier(left.toString()), arguments);
                }
            } else if (base.match(TokenType.DOT)) {
                base.eat(TokenType.DOT);
                Identifier field = typeParser.parseIdentifier();
                left = new StructAccess(left, field);
            } else if (base.match(TokenType.STRUCT_ACCESS)) {
                base.eat(TokenType.STRUCT_ACCESS);
                Identifier field = typeParser.parseIdentifier();
                left = new StructAccess(left, field);
            } else if (base.match(TokenType.INC)) {
                base.eat(TokenType.INC);
                left = new UnaryExpression("++", left, true);
            } else if (base.match(TokenType.DEC)) {
                base.eat(TokenType.DEC);
                left = new UnaryExpression("--", left, true);
            } else {
                break;
            }
        }
        return left;
    }

    private AstNode parsePrimary() {
        if (base.match(TokenType.IDENTIFIER)) {
            return typeParser.parseIdentifier();
        } else if (base.match(TokenType.NUMBER)) {
            Token token = base.currentToken;
            base.eat(TokenType.NUMBER);
            String value = token.getValue();
            if (value.contains(".")) {
                return new Literal(value, Literal.LiteralType.FLOAT);
            }
            return new Literal(value, Literal.LiteralType.INTEGER);
        } else if (base.match(TokenType.STRING)) {
            Token token = base.currentToken;
            base.eat(TokenType.STRING);
            return new Literal(token.getValue(), Literal.LiteralType.STRING);
        } else if (base.match(TokenType.CHAR_LITERAL)) {
            Token token = base.currentToken;
            base.eat(TokenType.CHAR_LITERAL);
            return new Literal(token.getValue(), Literal.LiteralType.CHARACTER);
        } else if (base.match(TokenType.NULL)) {
            base.eat(TokenType.NULL);
            return new Literal("null", Literal.LiteralType.NULL);
        } else if (base.match(TokenType.SIZEOF)) {
            return parseSizeofExpression();
        } else if (base.match(TokenType.LBRACE)) {
            return parseArrayInitializer();
        } else if (base.match(TokenType.LPAREN)) {
            return parseParenExpression();
        } else {
            throw new RuntimeException("Unexpected token " + base.currentToken.getType() + " at line " + base.currentToken.getLine());
        }
    }

    private AstNode parseSizeofExpression() {
        base.eat(TokenType.SIZEOF);
        base.eat(TokenType.LPAREN);
        AstNode argument;
        if (base.matchAny(TokenType.INT, TokenType.LONG, TokenType.SHORT, TokenType.CHAR, 
                     TokenType.FLOAT, TokenType.DOUBLE, TokenType.VOID, TokenType.BOOL, 
                     TokenType.CONST, TokenType.UNSIGNED, TokenType.STRUCT, TokenType.ENUM, TokenType.UNION)) {
            Type type = typeParser.parseType();
            argument = new Identifier(type.getName());
        } else {
            argument = parsePrimary();
        }
        base.eat(TokenType.RPAREN);
        return new FunctionCall(new Identifier("sizeof"), java.util.Arrays.asList(argument));
    }

    private AstNode parseArrayInitializer() {
        base.eat(TokenType.LBRACE);
        List<AstNode> elements = new ArrayList<>();
        if (!base.match(TokenType.RBRACE)) {
            elements.add(parseExpression());
            while (base.match(TokenType.COMMA)) {
                base.eat(TokenType.COMMA);
                elements.add(parseExpression());
            }
        }
        base.eat(TokenType.RBRACE);
        return new ArrayInitializer(elements);
    }

    private AstNode parseParenExpression() {
        base.eat(TokenType.LPAREN);
        
        if (base.isTypeStart(base.currentToken) || (base.currentToken.getType() == TokenType.IDENTIFIER && base.peek() != null && base.peek().getType() == TokenType.MUL)) {
            try {
                Type type = typeParser.parseType();
                if (base.match(TokenType.RPAREN)) {
                    base.eat(TokenType.RPAREN);
                    return new TypeCastExpression(type, parseExpression());
                }
            } catch (Exception e) {
            }
        }
        
        AstNode expr = parseExpression();
        base.eat(TokenType.RPAREN);
        return expr;
    }
}