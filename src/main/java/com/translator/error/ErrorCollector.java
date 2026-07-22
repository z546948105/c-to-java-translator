package com.translator.error;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ErrorCollector {
    private final List<TranslationError> errors;
    private boolean hasErrors;

    public ErrorCollector() {
        this.errors = new ArrayList<>();
        this.hasErrors = false;
    }

    public void addError(TranslationError error) {
        errors.add(error);
        hasErrors = true;
    }

    public void addError(ErrorType type, String message, int line, int column) {
        errors.add(new TranslationError(type, message, line, column));
        hasErrors = true;
    }

    public void addError(ErrorType type, String message, int line, int column, String originalCode) {
        errors.add(new TranslationError(type, message, line, column, originalCode));
        hasErrors = true;
    }

    public void addError(ErrorType type, String message, int line, int column, String originalCode, String context) {
        errors.add(new TranslationError(type, message, line, column, originalCode, context));
        hasErrors = true;
    }

    public List<TranslationError> getErrors() {
        return new ArrayList<>(errors);
    }

    public List<TranslationError> getErrorsByType(ErrorType type) {
        return errors.stream()
                     .filter(e -> e.getType() == type)
                     .collect(Collectors.toList());
    }

    public boolean hasErrors() {
        return hasErrors;
    }

    public int getErrorCount() {
        return errors.size();
    }

    public int getErrorCountByType(ErrorType type) {
        return (int) errors.stream()
                           .filter(e -> e.getType() == type)
                           .count();
    }

    public String getErrorSummary() {
        if (!hasErrors) {
            return "No errors found.";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("Found ").append(getErrorCount()).append(" error(s):\n");
        
        for (TranslationError error : errors) {
            sb.append("  ").append(error.toString()).append("\n");
        }
        
        return sb.toString();
    }

    public String getDetailedErrorReport() {
        if (!hasErrors) {
            return "No errors found.";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("=== Translation Error Report ===\n");
        sb.append("Total errors: ").append(getErrorCount()).append("\n\n");
        
        for (int i = 0; i < errors.size(); i++) {
            sb.append("--- Error ").append(i + 1).append(" ---\n");
            sb.append(errors.get(i).toDetailedString()).append("\n\n");
        }
        
        return sb.toString();
    }

    public void clear() {
        errors.clear();
        hasErrors = false;
    }
}