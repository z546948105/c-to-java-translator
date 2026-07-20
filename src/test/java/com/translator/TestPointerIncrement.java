package com.translator;

import com.translator.ast.Program;
import com.translator.codegen.CodeGenerator;
import com.translator.parser.Parser;
import com.translator.token.Lexer;
import com.translator.transform.AstTransformer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestPointerIncrement {

    @Test
    void testPointerPostfixIncrement() {
        String cCode = "void test() {\n" +
                       "    int arr[5] = {1, 2, 3, 4, 5};\n" +
                       "    int *ptr = arr;\n" +
                       "    int result = *ptr++;\n" +
                       "}";

        Lexer lexer = new Lexer(cCode);
        Parser parser = new Parser(lexer);
        Program program = parser.parse();

        AstTransformer transformer = new AstTransformer();
        Program javaProgram = (Program) transformer.visitProgram(program);

        CodeGenerator generator = new CodeGenerator();
        String javaCode = generator.visitProgram(javaProgram);

        assertNotNull(javaCode);
        assertTrue(javaCode.contains("arr[ptr_index++]"), "Should convert *ptr++ to arr[ptr_index++]");
        System.out.println("=== Pointer Postfix Increment ===");
        System.out.println(javaCode);
    }

    @Test
    void testPointerPrefixIncrement() {
        String cCode = "void test() {\n" +
                       "    int arr[5] = {1, 2, 3, 4, 5};\n" +
                       "    int *ptr = arr;\n" +
                       "    int result = *++ptr;\n" +
                       "}";

        Lexer lexer = new Lexer(cCode);
        Parser parser = new Parser(lexer);
        Program program = parser.parse();

        AstTransformer transformer = new AstTransformer();
        Program javaProgram = (Program) transformer.visitProgram(program);

        CodeGenerator generator = new CodeGenerator();
        String javaCode = generator.visitProgram(javaProgram);

        assertNotNull(javaCode);
        assertTrue(javaCode.contains("arr[++ptr_index]"), "Should convert *++ptr to arr[++ptr_index]");
        System.out.println("=== Pointer Prefix Increment ===");
        System.out.println(javaCode);
    }

    @Test
    void testPointerPostfixDecrement() {
        String cCode = "void test() {\n" +
                       "    int arr[5] = {1, 2, 3, 4, 5};\n" +
                       "    int *ptr = arr;\n" +
                       "    int result = *ptr--;\n" +
                       "}";

        Lexer lexer = new Lexer(cCode);
        Parser parser = new Parser(lexer);
        Program program = parser.parse();

        AstTransformer transformer = new AstTransformer();
        Program javaProgram = (Program) transformer.visitProgram(program);

        CodeGenerator generator = new CodeGenerator();
        String javaCode = generator.visitProgram(javaProgram);

        assertNotNull(javaCode);
        assertTrue(javaCode.contains("arr[ptr_index--]"), "Should convert *ptr-- to arr[ptr_index--]");
        System.out.println("=== Pointer Postfix Decrement ===");
        System.out.println(javaCode);
    }

    @Test
    void testPointerPrefixDecrement() {
        String cCode = "void test() {\n" +
                       "    int arr[5] = {1, 2, 3, 4, 5};\n" +
                       "    int *ptr = arr;\n" +
                       "    int result = *--ptr;\n" +
                       "}";

        Lexer lexer = new Lexer(cCode);
        Parser parser = new Parser(lexer);
        Program program = parser.parse();

        AstTransformer transformer = new AstTransformer();
        Program javaProgram = (Program) transformer.visitProgram(program);

        CodeGenerator generator = new CodeGenerator();
        String javaCode = generator.visitProgram(javaProgram);

        assertNotNull(javaCode);
        assertTrue(javaCode.contains("arr[--ptr_index]"), "Should convert *--ptr to arr[--ptr_index]");
        System.out.println("=== Pointer Prefix Decrement ===");
        System.out.println(javaCode);
    }

    @Test
    void testPointerIncrementStatement() {
        String cCode = "void test() {\n" +
                       "    int arr[5] = {1, 2, 3, 4, 5};\n" +
                       "    int *ptr = arr;\n" +
                       "    ptr++;\n" +
                       "}";

        Lexer lexer = new Lexer(cCode);
        Parser parser = new Parser(lexer);
        Program program = parser.parse();

        AstTransformer transformer = new AstTransformer();
        Program javaProgram = (Program) transformer.visitProgram(program);

        CodeGenerator generator = new CodeGenerator();
        String javaCode = generator.visitProgram(javaProgram);

        assertNotNull(javaCode);
        assertTrue(javaCode.contains("ptr_index++"), "Should convert ptr++ to ptr_index++");
        System.out.println("=== Pointer Increment Statement ===");
        System.out.println(javaCode);
    }

    @Test
    void testPointerDecrementStatement() {
        String cCode = "void test() {\n" +
                       "    int arr[5] = {1, 2, 3, 4, 5};\n" +
                       "    int *ptr = arr;\n" +
                       "    --ptr;\n" +
                       "}";

        Lexer lexer = new Lexer(cCode);
        Parser parser = new Parser(lexer);
        Program program = parser.parse();

        AstTransformer transformer = new AstTransformer();
        Program javaProgram = (Program) transformer.visitProgram(program);

        CodeGenerator generator = new CodeGenerator();
        String javaCode = generator.visitProgram(javaProgram);

        assertNotNull(javaCode);
        assertTrue(javaCode.contains("--ptr_index"), "Should convert --ptr to --ptr_index");
        System.out.println("=== Pointer Decrement Statement ===");
        System.out.println(javaCode);
    }

    @Test
    void testPointerIncrementInLoop() {
        String cCode = "void test() {\n" +
                       "    int arr[5] = {1, 2, 3, 4, 5};\n" +
                       "    int *ptr = arr;\n" +
                       "    for (int i = 0; i < 5; i++) {\n" +
                       "        printf(\"%d\\n\", *ptr++);\n" +
                       "    }\n" +
                       "}";

        Lexer lexer = new Lexer(cCode);
        Parser parser = new Parser(lexer);
        Program program = parser.parse();

        AstTransformer transformer = new AstTransformer();
        Program javaProgram = (Program) transformer.visitProgram(program);

        CodeGenerator generator = new CodeGenerator();
        String javaCode = generator.visitProgram(javaProgram);

        assertNotNull(javaCode);
        assertTrue(javaCode.contains("arr[ptr_index++]"), "Should convert *ptr++ to arr[ptr_index++]");
        System.out.println("=== Pointer Increment in Loop ===");
        System.out.println(javaCode);
    }

    @Test
    void testPointerDereferenceAfterIncrement() {
        String cCode = "void test() {\n" +
                       "    int arr[5] = {1, 2, 3, 4, 5};\n" +
                       "    int *ptr = arr;\n" +
                       "    ptr++;\n" +
                       "    int result = *ptr;\n" +
                       "}";

        Lexer lexer = new Lexer(cCode);
        Parser parser = new Parser(lexer);
        Program program = parser.parse();

        AstTransformer transformer = new AstTransformer();
        Program javaProgram = (Program) transformer.visitProgram(program);

        CodeGenerator generator = new CodeGenerator();
        String javaCode = generator.visitProgram(javaProgram);

        assertNotNull(javaCode);
        assertTrue(javaCode.contains("ptr_index++"), "Should convert ptr++");
        assertTrue(javaCode.contains("arr[ptr_index]"), "Should convert *ptr to arr[ptr_index]");
        System.out.println("=== Pointer Dereference After Increment ===");
        System.out.println(javaCode);
    }
}
