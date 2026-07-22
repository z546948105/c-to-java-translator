package com.translator.token;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 词法分析器（Lexer）
 * <p>
 * 将 C 源文件文本转换为 token 流
 * <p>
 * 支持的 token 类型：标识符、数字、字符串、关键字、运算符、分隔符等
 * <p>
 * 使用 BufferedReader 流式读取，避免一次性加载大文件
 */
public class Lexer {
    private BufferedReader reader;
    private int currentChar;
    private int line;
    private int column;
    private static final Map<String, TokenType> keywords = new HashMap<>();
    private static final int BUFFER_SIZE = 8192;

    static {
        keywords.put("int", TokenType.INT);
        keywords.put("long", TokenType.LONG);
        keywords.put("short", TokenType.SHORT);
        keywords.put("char", TokenType.CHAR);
        keywords.put("float", TokenType.FLOAT);
        keywords.put("double", TokenType.DOUBLE);
        keywords.put("void", TokenType.VOID);
        keywords.put("bool", TokenType.BOOL);
        keywords.put("struct", TokenType.STRUCT);
        keywords.put("typedef", TokenType.TYPEDEF);
        keywords.put("if", TokenType.IF);
        keywords.put("else", TokenType.ELSE);
        keywords.put("while", TokenType.WHILE);
        keywords.put("for", TokenType.FOR);
        keywords.put("do", TokenType.DO);
        keywords.put("switch", TokenType.SWITCH);
        keywords.put("case", TokenType.CASE);
        keywords.put("default", TokenType.DEFAULT);
        keywords.put("break", TokenType.BREAK);
        keywords.put("continue", TokenType.CONTINUE);
        keywords.put("return", TokenType.RETURN);
        keywords.put("NULL", TokenType.NULL);
        keywords.put("sizeof", TokenType.SIZEOF);
        keywords.put("static", TokenType.STATIC);
        keywords.put("size_t", TokenType.SIZE_T);
        keywords.put("const", TokenType.CONST);
        keywords.put("unsigned", TokenType.UNSIGNED);
        keywords.put("enum", TokenType.ENUM);
        keywords.put("union", TokenType.UNION);
    }

    public Lexer(String input) {
        this.reader = new BufferedReader(new StringReader(input));
        this.line = 1;
        this.column = 0;
        readNextChar();
    }

    public Lexer(File file) throws IOException {
        this(file, StandardCharsets.UTF_8);
    }

    public Lexer(File file, Charset charset) throws IOException {
        this.reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), charset), BUFFER_SIZE);
        this.line = 1;
        this.column = 0;
        readNextChar();
    }

    public Lexer(InputStream inputStream) throws IOException {
        this(inputStream, StandardCharsets.UTF_8);
    }

    public Lexer(InputStream inputStream, Charset charset) throws IOException {
        this.reader = new BufferedReader(new InputStreamReader(inputStream, charset), BUFFER_SIZE);
        this.line = 1;
        this.column = 0;
        readNextChar();
    }

    public void close() throws IOException {
        if (reader != null) {
            reader.close();
        }
    }

    public Token peekToken() {
        try {
            reader.mark(10000);
            int currentLine = line;
            int currentColumn = column;
            int currentCharBackup = currentChar;
            Token token = nextToken();
            reader.reset();
            line = currentLine;
            column = currentColumn;
            currentChar = currentCharBackup;
            return token;
        } catch (IOException e) {
            return new Token(TokenType.EOF, "", line, column);
        }
    }

    private void skipWhitespace() {
        while (currentChar != -1 && Character.isWhitespace(currentChar)) {
            readNextChar();
        }
    }

    private boolean matchKeyword(String keyword) {
        int backupChar = currentChar;
        int backupLine = line;
        int backupColumn = column;
        
        for (int i = 0; i < keyword.length(); i++) {
            if (currentChar != keyword.charAt(i)) {
                currentChar = backupChar;
                line = backupLine;
                column = backupColumn;
                return false;
            }
            readNextChar();
        }
        
        if (!Character.isLetterOrDigit(currentChar) && currentChar != '_') {
            return true;
        }
        
        currentChar = backupChar;
        line = backupLine;
        column = backupColumn;
        return false;
    }

    private void eatKeyword(String keyword) {
        for (int i = 0; i < keyword.length(); i++) {
            readNextChar();
        }
    }

    private void readNextChar() {
        try {
            currentChar = reader.read();
            if (currentChar == '\n') {
                line++;
                column = 0;
            } else {
                column++;
            }
        } catch (IOException e) {
            currentChar = -1;
        }
    }

    public Token nextToken() {
        while (currentChar != -1 && Character.isWhitespace(currentChar)) {
            readNextChar();
        }

        if (currentChar == -1) {
            return new Token(TokenType.EOF, "", line, column);
        }

        if (currentChar == '#') {
            while (currentChar != -1 && currentChar != '\n') {
                readNextChar();
            }
            if (currentChar == '\n') {
                readNextChar();
            }
            return nextToken();
        }

        if (Character.isLetter(currentChar) || currentChar == '_') {
            return readIdentifier();
        }

        if (Character.isDigit(currentChar)) {
            return readNumber();
        }

        if (currentChar == '"') {
            return readString();
        }

        if (currentChar == '\'') {
            return readCharLiteral();
        }

        return readSymbol();
    }

    private Token readIdentifier() {
        StringBuilder sb = new StringBuilder();
        int startLine = line;
        int startCol = column;

        while (currentChar != -1 && (Character.isLetterOrDigit(currentChar) || currentChar == '_')) {
            sb.append((char) currentChar);
            readNextChar();
        }

        String value = sb.toString();
        TokenType type = keywords.getOrDefault(value, TokenType.IDENTIFIER);
        return new Token(type, value, startLine, startCol);
    }

    private Token readNumber() {
        StringBuilder sb = new StringBuilder();
        int startLine = line;
        int startCol = column;
        boolean hasDecimal = false;

        if (currentChar == '0') {
            sb.append((char) currentChar);
            readNextChar();
            if (currentChar == 'x' || currentChar == 'X') {
                sb.append((char) currentChar);
                readNextChar();
                while (currentChar != -1 && (Character.isDigit(currentChar) || 
                       (currentChar >= 'a' && currentChar <= 'f') || 
                       (currentChar >= 'A' && currentChar <= 'F'))) {
                    sb.append((char) currentChar);
                    readNextChar();
                }
            } else if (currentChar == 'b' || currentChar == 'B') {
                sb.append((char) currentChar);
                readNextChar();
                while (currentChar != -1 && (currentChar == '0' || currentChar == '1')) {
                    sb.append((char) currentChar);
                    readNextChar();
                }
            } else {
                while (currentChar != -1 && (Character.isDigit(currentChar) || currentChar == '.')) {
                    if (currentChar == '.') {
                        if (hasDecimal) {
                            break;
                        }
                        hasDecimal = true;
                    }
                    sb.append((char) currentChar);
                    readNextChar();
                }
            }
        } else {
            while (currentChar != -1 && (Character.isDigit(currentChar) || currentChar == '.')) {
                if (currentChar == '.') {
                    if (hasDecimal) {
                        break;
                    }
                    hasDecimal = true;
                }
                sb.append((char) currentChar);
                readNextChar();
            }
        }

        while (currentChar != -1 && (currentChar == 'u' || currentChar == 'U' || 
               currentChar == 'l' || currentChar == 'L' || 
               currentChar == 'f' || currentChar == 'F' ||
               currentChar == 'd' || currentChar == 'D')) {
            readNextChar();
        }

        return new Token(TokenType.NUMBER, sb.toString(), startLine, startCol);
    }

    private Token readString() {
        StringBuilder sb = new StringBuilder();
        int startLine = line;
        int startCol = column;

        readNextChar();
        while (currentChar != -1 && currentChar != '"') {
            if (currentChar == '\\') {
                readNextChar();
                if (currentChar != -1) {
                    switch (currentChar) {
                        case 'n': sb.append('\n'); break;
                        case 't': sb.append('\t'); break;
                        case 'r': sb.append('\r'); break;
                        case '\\': sb.append('\\'); break;
                        case '"': sb.append('"'); break;
                        default: sb.append((char) currentChar);
                    }
                }
            } else {
                sb.append((char) currentChar);
            }
            readNextChar();
        }

        readNextChar();
        return new Token(TokenType.STRING, sb.toString(), startLine, startCol);
    }

    private Token readCharLiteral() {
        StringBuilder sb = new StringBuilder();
        int startLine = line;
        int startCol = column;

        readNextChar();
        while (currentChar != -1 && currentChar != '\'') {
            if (currentChar == '\\') {
                readNextChar();
                if (currentChar != -1) {
                    switch (currentChar) {
                        case 'n': sb.append('\n'); break;
                        case 't': sb.append('\t'); break;
                        case 'r': sb.append('\r'); break;
                        case '\\': sb.append('\\'); break;
                        case '\'': sb.append('\''); break;
                        case '"': sb.append('"'); break;
                        case '0': sb.append('\0'); break;
                        default: sb.append((char) currentChar);
                    }
                }
            } else {
                sb.append((char) currentChar);
            }
            readNextChar();
        }

        readNextChar();
        return new Token(TokenType.CHAR_LITERAL, sb.toString(), startLine, startCol);
    }

    private Token readSymbol() {
        int startLine = line;
        int startCol = column;
        char c = (char) currentChar;

        switch (c) {
            case '+':
                readNextChar();
                if (currentChar == '=') {
                    readNextChar();
                    return new Token(TokenType.PLUS_ASSIGN, "+=", startLine, startCol);
                } else if (currentChar == '+') {
                    readNextChar();
                    return new Token(TokenType.INC, "++", startLine, startCol);
                }
                return new Token(TokenType.PLUS, "+", startLine, startCol);

            case '-':
                readNextChar();
                if (currentChar == '=') {
                    readNextChar();
                    return new Token(TokenType.MINUS_ASSIGN, "-=", startLine, startCol);
                } else if (currentChar == '-') {
                    readNextChar();
                    return new Token(TokenType.DEC, "--", startLine, startCol);
                } else if (currentChar == '>') {
                    readNextChar();
                    return new Token(TokenType.STRUCT_ACCESS, "->", startLine, startCol);
                }
                return new Token(TokenType.MINUS, "-", startLine, startCol);

            case '*':
                readNextChar();
                if (currentChar == '=') {
                    readNextChar();
                    return new Token(TokenType.MUL_ASSIGN, "*=", startLine, startCol);
                }
                return new Token(TokenType.MUL, "*", startLine, startCol);

            case '/':
                readNextChar();
                if (currentChar == '=') {
                    readNextChar();
                    return new Token(TokenType.DIV_ASSIGN, "/=", startLine, startCol);
                } else if (currentChar == '/') {
                    while (currentChar != -1 && currentChar != '\n') {
                        readNextChar();
                    }
                    return nextToken();
                } else if (currentChar == '*') {
                    readNextChar();
                    while (currentChar != -1) {
                        if (currentChar == '*' && peek() == '/') {
                            readNextChar();
                            readNextChar();
                            break;
                        }
                        readNextChar();
                    }
                    return nextToken();
                }
                return new Token(TokenType.DIV, "/", startLine, startCol);

            case '%':
                readNextChar();
                if (currentChar == '=') {
                    readNextChar();
                    return new Token(TokenType.MOD_ASSIGN, "%=", startLine, startCol);
                }
                return new Token(TokenType.MOD, "%", startLine, startCol);

            case '=':
                readNextChar();
                if (currentChar == '=') {
                    readNextChar();
                    return new Token(TokenType.EQ, "==", startLine, startCol);
                }
                return new Token(TokenType.ASSIGN, "=", startLine, startCol);

            case '!':
                readNextChar();
                if (currentChar == '=') {
                    readNextChar();
                    return new Token(TokenType.NEQ, "!=", startLine, startCol);
                }
                return new Token(TokenType.NOT, "!", startLine, startCol);

            case '<':
                readNextChar();
                if (currentChar == '=') {
                    readNextChar();
                    return new Token(TokenType.LE, "<=", startLine, startCol);
                } else if (currentChar == '<') {
                    readNextChar();
                    if (currentChar == '=') {
                        readNextChar();
                        return new Token(TokenType.SHL_ASSIGN, "<<=", startLine, startCol);
                    }
                    return new Token(TokenType.SHL, "<<", startLine, startCol);
                }
                return new Token(TokenType.LT, "<", startLine, startCol);

            case '>':
                readNextChar();
                if (currentChar == '=') {
                    readNextChar();
                    return new Token(TokenType.GE, ">=", startLine, startCol);
                } else if (currentChar == '>') {
                    readNextChar();
                    if (currentChar == '=') {
                        readNextChar();
                        return new Token(TokenType.SHR_ASSIGN, ">>=", startLine, startCol);
                    }
                    return new Token(TokenType.SHR, ">>", startLine, startCol);
                }
                return new Token(TokenType.GT, ">", startLine, startCol);

            case '&':
                readNextChar();
                if (currentChar == '&') {
                    readNextChar();
                    return new Token(TokenType.AND, "&&", startLine, startCol);
                } else if (currentChar == '=') {
                    readNextChar();
                    return new Token(TokenType.BIT_AND_ASSIGN, "&=", startLine, startCol);
                }
                return new Token(TokenType.BIT_AND, "&", startLine, startCol);

            case '|':
                readNextChar();
                if (currentChar == '|') {
                    readNextChar();
                    return new Token(TokenType.OR, "||", startLine, startCol);
                } else if (currentChar == '=') {
                    readNextChar();
                    return new Token(TokenType.BIT_OR_ASSIGN, "|=", startLine, startCol);
                }
                return new Token(TokenType.BIT_OR, "|", startLine, startCol);

            case '^':
                readNextChar();
                if (currentChar == '=') {
                    readNextChar();
                    return new Token(TokenType.BIT_XOR_ASSIGN, "^=", startLine, startCol);
                }
                return new Token(TokenType.BIT_XOR, "^", startLine, startCol);

            case '~':
                readNextChar();
                return new Token(TokenType.BIT_NOT, "~", startLine, startCol);

            case '(':
                readNextChar();
                return new Token(TokenType.LPAREN, "(", startLine, startCol);

            case ')':
                readNextChar();
                return new Token(TokenType.RPAREN, ")", startLine, startCol);

            case '{':
                readNextChar();
                return new Token(TokenType.LBRACE, "{", startLine, startCol);

            case '}':
                readNextChar();
                return new Token(TokenType.RBRACE, "}", startLine, startCol);

            case '[':
                readNextChar();
                return new Token(TokenType.LBRACKET, "[", startLine, startCol);

            case ']':
                readNextChar();
                return new Token(TokenType.RBRACKET, "]", startLine, startCol);

            case ';':
                readNextChar();
                return new Token(TokenType.SEMICOLON, ";", startLine, startCol);

            case ',':
                readNextChar();
                return new Token(TokenType.COMMA, ",", startLine, startCol);

            case '.':
                readNextChar();
                return new Token(TokenType.DOT, ".", startLine, startCol);

            case ':':
                readNextChar();
                return new Token(TokenType.COLON, ":", startLine, startCol);

            case '?':
                readNextChar();
                return new Token(TokenType.QUESTION, "?", startLine, startCol);

            default:
                readNextChar();
                return new Token(TokenType.IDENTIFIER, String.valueOf(c), startLine, startCol);
        }
    }

    private int peek() {
        try {
            reader.mark(1);
            int next = reader.read();
            reader.reset();
            return next;
        } catch (IOException e) {
            return -1;
        }
    }
}