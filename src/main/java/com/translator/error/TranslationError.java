package com.translator.error;

public class TranslationError {
    private final ErrorType type;
    private final String message;
    private final int line;
    private final int column;
    private final String originalCode;
    private final String context;

    public TranslationError(ErrorType type, String message, int line, int column) {
        this(type, message, line, column, "", "");
    }

    public TranslationError(ErrorType type, String message, int line, int column, String originalCode) {
        this(type, message, line, column, originalCode, "");
    }

    public TranslationError(ErrorType type, String message, int line, int column, String originalCode, String context) {
        this.type = type;
        this.message = message;
        this.line = line;
        this.column = column;
        this.originalCode = originalCode;
        this.context = context;
    }

    public ErrorType getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public String getOriginalCode() {
        return originalCode;
    }

    public String getContext() {
        return context;
    }

    public String toDetailedString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[")
          .append(type.getEnglishName())
          .append("] Line ")
          .append(line);
        if (column > 0) {
            sb.append(", Column ").append(column);
        }
        sb.append(": ").append(message);
        
        if (originalCode != null && !originalCode.isEmpty()) {
            sb.append("\n  Original: ").append(originalCode);
        }
        
        if (context != null && !context.isEmpty()) {
            sb.append("\n  Context:\n");
            String[] contextLines = context.split("\n");
            for (String ctxLine : contextLines) {
                sb.append("    ").append(ctxLine).append("\n");
            }
        }
        
        return sb.toString();
    }

    @Override
    public String toString() {
        return String.format("[%s] Line %d: %s", type.getEnglishName(), line, message);
    }
}