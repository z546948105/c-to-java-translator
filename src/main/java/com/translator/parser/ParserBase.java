package com.translator.parser;

import com.translator.ast.UnsupportedCode;
import com.translator.error.ErrorCollector;
import com.translator.error.ErrorType;
import com.translator.token.Lexer;
import com.translator.token.Token;
import com.translator.token.TokenType;

abstract class ParserBase {
    protected final Lexer lexer;
    protected Token currentToken;
    protected String[] sourceLines;
    protected ErrorCollector errorCollector;

    protected ParserBase(Lexer lexer) {
        this.lexer = lexer;
        this.currentToken = lexer.nextToken();
        this.errorCollector = new ErrorCollector();
    }

    protected ParserBase(Lexer lexer, String source) {
        this.lexer = lexer;
        this.currentToken = lexer.nextToken();
        this.sourceLines = source.split("\n");
        this.errorCollector = new ErrorCollector();
    }

    protected ParserBase(Lexer lexer, String source, ErrorCollector errorCollector) {
        this.lexer = lexer;
        this.currentToken = lexer.nextToken();
        this.sourceLines = source.split("\n");
        this.errorCollector = errorCollector;
    }

    protected Token peek() {
        return lexer.peekToken();
    }

    protected void eat(TokenType type) {
        if (currentToken.getType() == type) {
            currentToken = lexer.nextToken();
        } else {
            String message = String.format("Expected token %s but got %s", type, currentToken.getType());
            String context = getErrorContext(currentToken.getLine(), 2);
            errorCollector.addError(
                ErrorType.SYNTAX_ERROR,
                message,
                currentToken.getLine(),
                currentToken.getColumn(),
                currentToken.getValue(),
                context
            );
            throw new RuntimeException(message);
        }
    }

    protected String consumeUntil(TokenType... stopTokens) {
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

    protected UnsupportedCode createUnsupportedCode(String reason) {
        return createUnsupportedCode(reason, ErrorType.UNSUPPORTED_FEATURE);
    }

    protected UnsupportedCode createUnsupportedCode(String reason, ErrorType errorType) {
        String originalCode = consumeUntil(TokenType.SEMICOLON, TokenType.LBRACE, TokenType.RBRACE, TokenType.EOF);
        int line = currentToken.getLine();
        int column = currentToken.getColumn();
        if (match(TokenType.SEMICOLON) || match(TokenType.LBRACE) || match(TokenType.RBRACE)) {
            currentToken = lexer.nextToken();
        }
        String context = getErrorContext(line, 2);
        
        errorCollector.addError(errorType, reason, line, column, originalCode, context);
        
        return new UnsupportedCode(originalCode, reason, line, context);
    }

    protected void panicModeRecovery() {
        while (currentToken != null && 
               currentToken.getType() != TokenType.EOF &&
               currentToken.getType() != TokenType.SEMICOLON &&
               currentToken.getType() != TokenType.LBRACE &&
               currentToken.getType() != TokenType.RBRACE &&
               currentToken.getType() != TokenType.IF &&
               currentToken.getType() != TokenType.WHILE &&
               currentToken.getType() != TokenType.FOR &&
               currentToken.getType() != TokenType.DO &&
               currentToken.getType() != TokenType.SWITCH &&
               currentToken.getType() != TokenType.RETURN &&
               currentToken.getType() != TokenType.BREAK &&
               currentToken.getType() != TokenType.CONTINUE) {
            currentToken = lexer.nextToken();
        }
        
        if (currentToken != null && currentToken.getType() == TokenType.SEMICOLON) {
            currentToken = lexer.nextToken();
        }
    }

    protected boolean match(TokenType type) {
        return currentToken.getType() == type;
    }

    protected String getErrorContext(int errorLine, int contextSize) {
        if (sourceLines == null) {
            return "";
        }
        StringBuilder context = new StringBuilder();
        int startLine = Math.max(0, errorLine - 1 - contextSize);
        int endLine = Math.min(sourceLines.length, errorLine + contextSize);
        
        for (int i = startLine; i < endLine; i++) {
            int displayLine = i + 1;
            context.append(String.format("%4d: %s", displayLine, sourceLines[i]));
            if (i == errorLine - 1) {
                context.append(" <<< ERROR HERE");
            }
            if (i < endLine - 1) {
                context.append("\n");
            }
        }
        return context.toString();
    }

    protected void skipWhitespace() {
        while (currentToken != null && 
               (currentToken.getType() == TokenType.IDENTIFIER || 
                currentToken.getType() == TokenType.NUMBER ||
                currentToken.getType() == TokenType.STRING ||
                currentToken.getType() == TokenType.CHAR_LITERAL)) {
            currentToken = lexer.nextToken();
        }
    }

    protected boolean matchAny(TokenType... types) {
        for (TokenType type : types) {
            if (match(type)) {
                return true;
            }
        }
        return false;
    }
    
    protected boolean isTypeStart(Token token) {
        if (token == null) return false;
        TokenType type = token.getType();
        return type == TokenType.INT || type == TokenType.LONG || type == TokenType.SHORT ||
               type == TokenType.CHAR || type == TokenType.FLOAT || type == TokenType.DOUBLE ||
               type == TokenType.VOID || type == TokenType.BOOL || type == TokenType.STRUCT ||
               type == TokenType.SIZE_T || type == TokenType.CONST || type == TokenType.UNSIGNED ||
               type == TokenType.ENUM || type == TokenType.UNION;
    }

    protected ErrorCollector getErrorCollector() {
        return errorCollector;
    }
}