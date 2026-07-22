package com.translator.ast;

import com.translator.error.TranslationError;

/**
 * 不支持的代码节点，表示无法转换的 C 代码片段
 * <p>
 * 包含统一的 TranslationError 对象，包含错误类型、原因、位置和上下文
 * <p>
 * 转换为 Java 时会生成注释说明
 */
public class UnsupportedCode implements AstNode {
    private final TranslationError error;
    private final String originalCode;

    public UnsupportedCode(String originalCode, String reason, int line) {
        this(originalCode, reason, line, "");
    }

    public UnsupportedCode(String originalCode, String reason, int line, String context) {
        this.error = new com.translator.error.TranslationError(
            com.translator.error.ErrorType.UNSUPPORTED_FEATURE,
            reason,
            line,
            0,
            originalCode,
            context
        );
        this.originalCode = originalCode;
    }

    public UnsupportedCode(TranslationError error) {
        this.error = error;
        this.originalCode = error.getOriginalCode();
    }

    public TranslationError getError() {
        return error;
    }

    public String getOriginalCode() {
        return originalCode;
    }

    public String getReason() {
        return error.getMessage();
    }

    public int getLine() {
        return error.getLine();
    }

    public String getContext() {
        return error.getContext();
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitUnsupportedCode(this);
    }
}
