package com.translator.token;

/**
 * 词法单元（Token）类
 * <p>
 * 表示从源代码中识别出的最小有意义的单元
 * <p>
 * 包含：token 类型、token 值、所在行号和列号
 * <p>
 * 行号和列号用于错误报告和调试信息
 */
public class Token {
    private final TokenType type;
    private final String value;
    private final int line;
    private final int column;

    public Token(TokenType type, String value, int line, int column) {
        this.type = type;
        this.value = value;
        this.line = line;
        this.column = column;
    }

    public TokenType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    @Override
    public String toString() {
        return String.format("Token{%s, '%s', line=%d, col=%d}", type, value, line, column);
    }
}