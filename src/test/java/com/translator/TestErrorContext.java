package com.translator;

import com.translator.ast.Program;
import com.translator.codegen.CodeGenerator;
import com.translator.error.ErrorCollector;
import com.translator.error.ErrorType;
import com.translator.parser.Parser;
import com.translator.token.Lexer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestErrorContext {

    @Test
    void testErrorContextDisplayed() {
        String cCode = "int **pp;\n" +
                      "int *p;\n" +
                      "int x = 10;\n" +
                      "p = &x;\n" +
                      "pp = &p;\n" +
                      "int y = **pp;\n";

        Lexer lexer = new Lexer(cCode);
        Parser parser = new Parser(lexer, cCode);

        Program program = parser.parse();

        CodeGenerator generator = new CodeGenerator();
        String javaCode = generator.visitProgram(program);

        assertNotNull(javaCode);
        assertTrue(javaCode.contains("Context"), "Should contain error context");
        assertTrue(javaCode.contains("<<< ERROR HERE"), "Should mark error line");
        assertTrue(javaCode.contains("p = &x"), "Should show original code in context");
        
        System.out.println("=== Error Context Test ===");
        System.out.println(javaCode);
    }

    @Test
    void testErrorContextWithMultiLineCode() {
        String cCode = "#include <stdio.h>\n" +
                      "int main() {\n" +
                      "    int a = 10;\n" +
                      "    int b = 20;\n" +
                      "    unknown_function();\n" +
                      "    return 0;\n" +
                      "}\n";

        Lexer lexer = new Lexer(cCode);
        Parser parser = new Parser(lexer, cCode);

        Program program = parser.parse();

        CodeGenerator generator = new CodeGenerator();
        String javaCode = generator.visitProgram(program);

        assertNotNull(javaCode);
        
        System.out.println("=== Multi-line Error Context Test ===");
        System.out.println(javaCode);
    }

    @Test
    void testErrorContextWithTopOfFile() {
        String cCode = "unknown_statement;\n" +
                      "int x = 10;\n" +
                      "int y = 20;\n";

        Lexer lexer = new Lexer(cCode);
        Parser parser = new Parser(lexer, cCode);

        Program program = parser.parse();

        CodeGenerator generator = new CodeGenerator();
        String javaCode = generator.visitProgram(program);

        assertNotNull(javaCode);
        assertTrue(javaCode.contains("Context"), "Should contain error context");
        
        System.out.println("=== Top of File Error Context Test ===");
        System.out.println(javaCode);
    }

    @Test
    void testErrorCollectorCollectsAllErrors() {
        String cCode = "unknown_statement1;\n" +
                      "int x = 10;\n" +
                      "unknown_statement2;\n" +
                      "int y = 20;\n";

        Lexer lexer = new Lexer(cCode);
        Parser parser = new Parser(lexer, cCode);

        Program program = parser.parse();

        ErrorCollector collector = parser.getErrorCollector();
        assertTrue(collector.hasErrors(), "Should have errors");
        assertEquals(2, collector.getErrorCount(), "Should collect 2 errors");
        
        System.out.println("=== Error Collector Test ===");
        System.out.println(collector.getErrorSummary());
    }

    @Test
    void testErrorTypes() {
        String cCode = "unknown_statement;\n";

        Lexer lexer = new Lexer(cCode);
        Parser parser = new Parser(lexer, cCode);

        Program program = parser.parse();

        ErrorCollector collector = parser.getErrorCollector();
        assertEquals(1, collector.getErrorCountByType(ErrorType.UNSUPPORTED_FEATURE), 
                     "Should have one Unsupported Feature error");
        
        System.out.println("=== Error Types Test ===");
        System.out.println(collector.getDetailedErrorReport());
    }

    @Test
    void testPanicModeRecovery() {
        String cCode = "int a;\n" +
                      "invalid_code_here;\n" +
                      "int b;\n" +
                      "more_invalid_code;\n" +
                      "int c;\n";

        Lexer lexer = new Lexer(cCode);
        Parser parser = new Parser(lexer, cCode);

        Program program = parser.parse();

        CodeGenerator generator = new CodeGenerator();
        String javaCode = generator.visitProgram(program);

        assertNotNull(javaCode);
        assertTrue(javaCode.contains("int a"), "Should parse first declaration");
        assertTrue(javaCode.contains("int b"), "Should parse second declaration after error");
        assertTrue(javaCode.contains("int c"), "Should parse third declaration after error");
        
        System.out.println("=== Panic Mode Recovery Test ===");
        System.out.println(javaCode);
    }
}