package com.translator.ast;

/**
 * 不支持的代码节点，表示无法转换的 C 代码片段
 * <p>
 * 包含原始代码、不支持的原因和所在行号
 * <p>
 * 转换为 Java 时会生成注释说明
 */
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
