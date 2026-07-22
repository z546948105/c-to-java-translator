package com.translator.error;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestErrorHandler {
    private ErrorHandler errorHandler;
    private String testSourceCode;

    @BeforeEach
    void setUp() {
        testSourceCode = "int main() {\n" +
                        "    int x = 10;\n" +
                        "    unknown_statement;\n" +
                        "    int y = 20;\n" +
                        "    return 0;\n" +
                        "}";
        errorHandler = new ErrorHandler(testSourceCode);
        errorHandler.setVerboseMode(false);
    }

    @Test
    void testReportSyntaxError() {
        errorHandler.reportSyntaxError("Unexpected token", 3, 5);
        
        assertTrue(errorHandler.hasErrors());
        assertEquals(1, errorHandler.getErrorCount());
        
        List<TranslationError> errors = errorHandler.getErrors();
        assertEquals(ErrorType.SYNTAX_ERROR, errors.get(0).getType());
        assertEquals("Unexpected token", errors.get(0).getMessage());
        assertEquals(3, errors.get(0).getLine());
        assertEquals(5, errors.get(0).getColumn());
        assertTrue(errors.get(0).getOriginalCode().contains("unknown_statement"));
        assertTrue(errors.get(0).getContext().contains("ERROR HERE"));
    }

    @Test
    void testReportUnsupportedFeature() {
        errorHandler.reportUnsupportedFeature("Feature not supported", 3, 1);
        
        assertTrue(errorHandler.hasErrors());
        assertEquals(1, errorHandler.getErrorCount());
        
        List<TranslationError> errors = errorHandler.getErrors();
        assertEquals(ErrorType.UNSUPPORTED_FEATURE, errors.get(0).getType());
    }

    @Test
    void testReportMultipleErrors() {
        errorHandler.reportSyntaxError("Error 1", 2, 1);
        errorHandler.reportSemanticError("Error 2", 3, 1);
        errorHandler.reportTypeError("Error 3", 4, 1);
        
        assertEquals(3, errorHandler.getErrorCount());
        assertEquals(1, errorHandler.getErrorCountByType(ErrorType.SYNTAX_ERROR));
        assertEquals(1, errorHandler.getErrorCountByType(ErrorType.SEMANTIC_ERROR));
        assertEquals(1, errorHandler.getErrorCountByType(ErrorType.TYPE_ERROR));
    }

    @Test
    void testGetErrorsByType() {
        errorHandler.reportSyntaxError("Syntax error", 2, 1);
        errorHandler.reportSyntaxError("Another syntax error", 3, 1);
        errorHandler.reportSemanticError("Semantic error", 4, 1);
        
        List<TranslationError> syntaxErrors = errorHandler.getErrorsByType(ErrorType.SYNTAX_ERROR);
        List<TranslationError> semanticErrors = errorHandler.getErrorsByType(ErrorType.SEMANTIC_ERROR);
        List<TranslationError> typeErrors = errorHandler.getErrorsByType(ErrorType.TYPE_ERROR);
        
        assertEquals(2, syntaxErrors.size());
        assertEquals(1, semanticErrors.size());
        assertEquals(0, typeErrors.size());
    }

    @Test
    void testErrorSummary() {
        errorHandler.reportParseError("Test error", 1, 1);
        
        String summary = errorHandler.getErrorSummary();
        assertTrue(summary.contains("1 error"));
        assertTrue(summary.contains("Parse Error"));
    }

    @Test
    void testDetailedErrorReport() {
        errorHandler.reportUnsupportedFeature("Expected type keyword", 3, 1);
        
        String report = errorHandler.getDetailedErrorReport();
        assertTrue(report.contains("Translation Error Report"));
        assertTrue(report.contains("Unsupported Feature"));
        assertTrue(report.contains("Line 3"));
        assertTrue(report.contains("ERROR HERE"));
    }

    @Test
    void testContextExtraction() {
        errorHandler.reportError(ErrorType.PARSE_ERROR, "Test", 3, 1);
        
        TranslationError error = errorHandler.getErrors().get(0);
        String context = error.getContext();
        
        assertTrue(context.contains("int x = 10;"));
        assertTrue(context.contains("unknown_statement;"));
        assertTrue(context.contains("int y = 20;"));
        assertTrue(context.contains("<<< ERROR HERE"));
    }

    @Test
    void testClearErrors() {
        errorHandler.reportSyntaxError("Error", 1, 1);
        assertTrue(errorHandler.hasErrors());
        
        errorHandler.clearErrors();
        assertFalse(errorHandler.hasErrors());
        assertEquals(0, errorHandler.getErrorCount());
    }

    @Test
    void testVerboseMode() {
        ErrorHandler verboseHandler = new ErrorHandler(testSourceCode);
        verboseHandler.setVerboseMode(true);
        
        verboseHandler.reportSyntaxError("Test verbose", 1, 1);
        assertTrue(verboseHandler.hasErrors());
    }

    @Test
    void testNoErrors() {
        assertFalse(errorHandler.hasErrors());
        assertEquals(0, errorHandler.getErrorCount());
        assertTrue(errorHandler.getErrorSummary().contains("No errors"));
    }
}