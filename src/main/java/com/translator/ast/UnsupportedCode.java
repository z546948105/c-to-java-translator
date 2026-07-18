package com.translator.ast;

public class UnsupportedCode implements AstNode {
    private final String originalCode;
    private final String reason;
    private final int line;

    public UnsupportedCode(String originalCode, String reason, int line) {
        this.originalCode = originalCode;
        this.reason = reason;
        this.line = line;
    }

    public String getOriginalCode() {
        return originalCode;
    }

    public String getReason() {
        return reason;
    }

    public int getLine() {
        return line;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitUnsupportedCode(this);
    }
}
