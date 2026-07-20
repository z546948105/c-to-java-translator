package com.translator;

import com.translator.ast.Program;
import com.translator.codegen.CodeGenerator;
import com.translator.parser.Parser;
import com.translator.token.Lexer;
import com.translator.transform.AstTransformer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestFunctionPointer {

    @Test
    void testFunctionPointerDeclaration() {
        String cCode = "void test() {\n" +
                       "    int (*func)(int);\n" +
                       "}";

        Lexer lexer = new Lexer(cCode);
        Parser parser = new Parser(lexer);
        Program program = parser.parse();

        AstTransformer transformer = new AstTransformer();
        Program javaProgram = (Program) transformer.visitProgram(program);

        CodeGenerator generator = new CodeGenerator();
        String javaCode = generator.visitProgram(javaProgram);

        assertNotNull(javaCode);
        System.out.println("=== Function Pointer Declaration ===");
        System.out.println(javaCode);
        assertTrue(javaCode.contains("java.util.function.Function<Integer, Integer> func"), 
                   "Should convert int (*func)(int) to Function<Integer, Integer>");
        System.out.println("=== Generated code contains Function: " + javaCode.contains("java.util.function.Function"));
    }

    @Test
    void testFunctionPointerWithTwoParams() {
        String cCode = "void test() {\n" +
                       "    int (*func)(int, int);\n" +
                       "}";

        Lexer lexer = new Lexer(cCode);
        Parser parser = new Parser(lexer);
        Program program = parser.parse();

        AstTransformer transformer = new AstTransformer();
        Program javaProgram = (Program) transformer.visitProgram(program);

        CodeGenerator generator = new CodeGenerator();
        String javaCode = generator.visitProgram(javaProgram);

        assertNotNull(javaCode);
        assertTrue(javaCode.contains("java.util.function.BiFunction<Integer, Integer, Integer> func"), 
                   "Should convert int (*func)(int, int) to BiFunction<Integer, Integer, Integer>");
        System.out.println("=== Function Pointer With Two Params ===");
        System.out.println(javaCode);
    }

    @Test
    void testFunctionPointerWithVoidReturn() {
        String cCode = "void test() {\n" +
                       "    void (*callback)(int);\n" +
                       "}";

        Lexer lexer = new Lexer(cCode);
        Parser parser = new Parser(lexer);
        Program program = parser.parse();

        AstTransformer transformer = new AstTransformer();
        Program javaProgram = (Program) transformer.visitProgram(program);

        CodeGenerator generator = new CodeGenerator();
        String javaCode = generator.visitProgram(javaProgram);

        assertNotNull(javaCode);
        assertTrue(javaCode.contains("java.util.function.Consumer<Integer> callback"), 
                   "Should convert void (*callback)(int) to Consumer<Integer>");
        System.out.println("=== Function Pointer With Void Return ===");
        System.out.println(javaCode);
    }

    @Test
    void testFunctionPointerWithNoParams() {
        String cCode = "void test() {\n" +
                       "    int (*getter)(void);\n" +
                       "}";

        Lexer lexer = new Lexer(cCode);
        Parser parser = new Parser(lexer);
        Program program = parser.parse();

        AstTransformer transformer = new AstTransformer();
        Program javaProgram = (Program) transformer.visitProgram(program);

        CodeGenerator generator = new CodeGenerator();
        String javaCode = generator.visitProgram(javaProgram);

        assertNotNull(javaCode);
        System.out.println("=== Function Pointer With No Params ===");
        System.out.println(javaCode);
        assertTrue(javaCode.contains("java.util.function.Supplier<Integer> getter"), 
                   "Should convert int (*getter)(void) to Supplier<Integer>");
    }

    @Test
    void testFunctionPointerWithVoidReturnNoParams() {
        String cCode = "void test() {\n" +
                       "    void (*run)(void);\n" +
                       "}";

        Lexer lexer = new Lexer(cCode);
        Parser parser = new Parser(lexer);
        Program program = parser.parse();

        AstTransformer transformer = new AstTransformer();
        Program javaProgram = (Program) transformer.visitProgram(program);

        CodeGenerator generator = new CodeGenerator();
        String javaCode = generator.visitProgram(javaProgram);

        assertNotNull(javaCode);
        assertTrue(javaCode.contains("java.util.function.Runnable run"), 
                   "Should convert void (*run)(void) to Runnable");
        System.out.println("=== Function Pointer With Void Return No Params ===");
        System.out.println(javaCode);
    }

    @Test
    void testFunctionPointerAssignment() {
        String cCode = "void test() {\n" +
                       "    int (*func)(int);\n" +
                       "    func = NULL;\n" +
                       "}";

        Lexer lexer = new Lexer(cCode);
        Parser parser = new Parser(lexer);
        Program program = parser.parse();

        AstTransformer transformer = new AstTransformer();
        Program javaProgram = (Program) transformer.visitProgram(program);

        CodeGenerator generator = new CodeGenerator();
        String javaCode = generator.visitProgram(javaProgram);

        assertNotNull(javaCode);
        assertTrue(javaCode.contains("java.util.function.Function<Integer, Integer> func"), 
                   "Should convert int (*func)(int) to Function<Integer, Integer>");
        System.out.println("=== Function Pointer Assignment ===");
        System.out.println(javaCode);
    }

    @Test
    void testFunctionPointerArray() {
        String cCode = "void test() {\n" +
                       "    int (*funcs[5])(int);\n" +
                       "}";

        Lexer lexer = new Lexer(cCode);
        Parser parser = new Parser(lexer);
        Program program = parser.parse();

        AstTransformer transformer = new AstTransformer();
        Program javaProgram = (Program) transformer.visitProgram(program);

        CodeGenerator generator = new CodeGenerator();
        String javaCode = generator.visitProgram(javaProgram);

        assertNotNull(javaCode);
        System.out.println("=== Function Pointer Array ===");
        System.out.println(javaCode);
        assertTrue(javaCode.contains("java.util.function.Function<Integer, Integer>[] funcs"), 
                   "Should convert int (*funcs[5])(int) to Function<Integer, Integer>[]");
    }
}
