package com.translator.error;

import java.util.List;

public class ErrorHandler {
    private final ErrorCollector collector;
    private final String sourceCode;
    private boolean verboseMode = true;

    public ErrorHandler() {
        this.collector = new ErrorCollector();
        this.sourceCode = "";
    }

    public ErrorHandler(String sourceCode) {
        this.collector = new ErrorCollector();
        this.sourceCode = sourceCode;
    }

    public ErrorHandler(ErrorCollector collector, String sourceCode) {
        this.collector = collector;
        this.sourceCode = sourceCode;
    }

    public void setVerboseMode(boolean verbose) {
        this.verboseMode = verbose;
    }

    public boolean isVerboseMode() {
        return verboseMode;
    }

    public void reportSyntaxError(String message, int line, int column) {
        String context = extractContext(line);
        String originalCode = extractOriginalCode(line);
        TranslationError error = new TranslationError(
                ErrorType.SYNTAX_ERROR, message, line, column, originalCode, context);
        collector.addError(error);
        if (verboseMode) {
            System.err.println(error.toDetailedString());
        }
    }

    public void reportSemanticError(String message, int line, int column) {
        String context = extractContext(line);
        String originalCode = extractOriginalCode(line);
        TranslationError error = new TranslationError(
                ErrorType.SEMANTIC_ERROR, message, line, column, originalCode, context);
        collector.addError(error);
        if (verboseMode) {
            System.err.println(error.toDetailedString());
        }
    }

    public void reportTypeError(String message, int line, int column) {
        String context = extractContext(line);
        String originalCode = extractOriginalCode(line);
        TranslationError error = new TranslationError(
                ErrorType.TYPE_ERROR, message, line, column, originalCode, context);
        collector.addError(error);
        if (verboseMode) {
            System.err.println(error.toDetailedString());
        }
    }

    public void reportUnsupportedFeature(String message, int line, int column) {
        String context = extractContext(line);
        String originalCode = extractOriginalCode(line);
        TranslationError error = new TranslationError(
                ErrorType.UNSUPPORTED_FEATURE, message, line, column, originalCode, context);
        collector.addError(error);
        if (verboseMode) {
            System.err.println(error.toDetailedString());
        }
    }

    public void reportParseError(String message, int line, int column) {
        String context = extractContext(line);
        String originalCode = extractOriginalCode(line);
        TranslationError error = new TranslationError(
                ErrorType.PARSE_ERROR, message, line, column, originalCode, context);
        collector.addError(error);
        if (verboseMode) {
            System.err.println(error.toDetailedString());
        }
    }

    public void reportPreprocessorError(String message, int line, int column) {
        String context = extractContext(line);
        String originalCode = extractOriginalCode(line);
        TranslationError error = new TranslationError(
                ErrorType.PREPROCESSOR_ERROR, message, line, column, originalCode, context);
        collector.addError(error);
        if (verboseMode) {
            System.err.println(error.toDetailedString());
        }
    }

    public void reportTransformationError(String message, int line, int column) {
        String context = extractContext(line);
        String originalCode = extractOriginalCode(line);
        TranslationError error = new TranslationError(
                ErrorType.TRANSFORMATION_ERROR, message, line, column, originalCode, context);
        collector.addError(error);
        if (verboseMode) {
            System.err.println(error.toDetailedString());
        }
    }

    public void reportCodeGenerationError(String message, int line, int column) {
        String context = extractContext(line);
        String originalCode = extractOriginalCode(line);
        TranslationError error = new TranslationError(
                ErrorType.CODE_GENERATION_ERROR, message, line, column, originalCode, context);
        collector.addError(error);
        if (verboseMode) {
            System.err.println(error.toDetailedString());
        }
    }

    public void reportError(ErrorType type, String message, int line, int column) {
        String context = extractContext(line);
        String originalCode = extractOriginalCode(line);
        TranslationError error = new TranslationError(type, message, line, column, originalCode, context);
        collector.addError(error);
        if (verboseMode) {
            System.err.println(error.toDetailedString());
        }
    }

    public void reportError(TranslationError error) {
        collector.addError(error);
        if (verboseMode) {
            System.err.println(error.toDetailedString());
        }
    }

    private String extractContext(int lineNumber) {
        if (sourceCode == null || sourceCode.isEmpty()) {
            return "";
        }

        String[] lines = sourceCode.split("\n");
        if (lineNumber < 1 || lineNumber > lines.length) {
            return "";
        }

        StringBuilder context = new StringBuilder();
        int startLine = Math.max(1, lineNumber - 2);
        int endLine = Math.min(lines.length, lineNumber + 2);

        for (int i = startLine; i <= endLine; i++) {
            String line = lines[i - 1];
            String lineNumStr = String.format("%4d:", i);
            if (i == lineNumber) {
                context.append(lineNumStr).append(" ").append(line).append(" <<< ERROR HERE\n");
            } else {
                context.append(lineNumStr).append(" ").append(line).append("\n");
            }
        }

        return context.toString().trim();
    }

    private String extractOriginalCode(int lineNumber) {
        if (sourceCode == null || sourceCode.isEmpty()) {
            return "";
        }

        String[] lines = sourceCode.split("\n");
        if (lineNumber < 1 || lineNumber > lines.length) {
            return "";
        }

        return lines[lineNumber - 1].trim();
    }

    public ErrorCollector getCollector() {
        return collector;
    }

    public List<TranslationError> getErrors() {
        return collector.getErrors();
    }

    public List<TranslationError> getErrorsByType(ErrorType type) {
        return collector.getErrorsByType(type);
    }

    public boolean hasErrors() {
        return collector.hasErrors();
    }

    public int getErrorCount() {
        return collector.getErrorCount();
    }

    public int getErrorCountByType(ErrorType type) {
        return collector.getErrorCountByType(type);
    }

    public String getErrorSummary() {
        return collector.getErrorSummary();
    }

    public String getDetailedErrorReport() {
        return collector.getDetailedErrorReport();
    }

    public void printErrorSummary() {
        System.out.println(collector.getErrorSummary());
    }

    public void printDetailedErrorReport() {
        System.out.println(collector.getDetailedErrorReport());
    }

    public void clearErrors() {
        collector.clear();
    }
}