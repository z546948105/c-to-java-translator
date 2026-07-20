package com.translator;

import com.translator.ast.Program;
import com.translator.codegen.CodeGenerator;
import com.translator.parser.Parser;
import com.translator.token.Lexer;
import com.translator.transform.AstTransformer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestPointerArithmetic {

    @Test
    void testPointerToArrayMapping() {
        String cCode = "void test() {\n" +
                       "    int arr[5] = {1, 2, 3, 4, 5};\n" +
                       "    int *ptr = arr;\n" +
                       "}";

        Lexer lexer = new Lexer(cCode);
        Parser parser = new Parser(lexer);
        Program program = parser.parse();

        AstTransformer transformer = new AstTransformer();
        Program javaProgram = (Program) transformer.visitProgram(program);

        CodeGenerator generator = new CodeGenerator();
        String javaCode = generator.visitProgram(javaProgram);

        assertNotNull(javaCode);
        assertTrue(javaCode.contains("int[5] arr"), "Should declare array");
        assertTrue(javaCode.contains("ptr -> arr"), "Should generate pointer mapping comment");
        System.out.println("=== Pointer to Array Mapping ===");
        System.out.println(javaCode);
    }

    @Test
    void testPointerDereference() {
        String cCode = "void test() {\n" +
                       "    int value = 10;\n" +
                       "    int *ptr = &value;\n" +
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
        assertTrue(javaCode.contains("int result = value"), "Should dereference to value");
        System.out.println("=== Pointer Dereference ===");
        System.out.println(javaCode);
    }

    @Test
    void testPointerAddition() {
        String cCode = "void test() {\n" +
                       "    int arr[5] = {1, 2, 3, 4, 5};\n" +
                       "    int *ptr = arr;\n" +
                       "    int result = *(ptr + 2);\n" +
                       "}";

        Lexer lexer = new Lexer(cCode);
        Parser parser = new Parser(lexer);
        Program program = parser.parse();

        AstTransformer transformer = new AstTransformer();
        Program javaProgram = (Program) transformer.visitProgram(program);

        CodeGenerator generator = new CodeGenerator();
        String javaCode = generator.visitProgram(javaProgram);

        assertNotNull(javaCode);
        assertTrue(javaCode.contains("arr[2]"), "Should convert *(ptr + 2) to arr[2]");
        System.out.println("=== Pointer Addition ===");
        System.out.println(javaCode);
    }

    @Test
    void testPointerSubtraction() {
        String cCode = "void test() {\n" +
                       "    int arr[5] = {1, 2, 3, 4, 5};\n" +
                       "    int *ptr = arr;\n" +
                       "    int result = *(ptr - 1);\n" +
                       "}";

        Lexer lexer = new Lexer(cCode);
        Parser parser = new Parser(lexer);
        Program program = parser.parse();

        AstTransformer transformer = new AstTransformer();
        Program javaProgram = (Program) transformer.visitProgram(program);

        CodeGenerator generator = new CodeGenerator();
        String javaCode = generator.visitProgram(javaProgram);

        assertNotNull(javaCode);
        assertTrue(javaCode.contains("arr[-1]"), "Should convert *(ptr - 1) to arr[-1]");
        System.out.println("=== Pointer Subtraction ===");
        System.out.println(javaCode);
    }

    @Test
    void testPointerArrayAccess() {
        String cCode = "void test() {\n" +
                       "    int arr[5] = {1, 2, 3, 4, 5};\n" +
                       "    int *ptr = arr;\n" +
                       "    int result = ptr[2];\n" +
                       "}";

        Lexer lexer = new Lexer(cCode);
        Parser parser = new Parser(lexer);
        Program program = parser.parse();

        AstTransformer transformer = new AstTransformer();
        Program javaProgram = (Program) transformer.visitProgram(program);

        CodeGenerator generator = new CodeGenerator();
        String javaCode = generator.visitProgram(javaProgram);

        assertNotNull(javaCode);
        assertTrue(javaCode.contains("arr[2]"), "Should convert ptr[2] to arr[2]");
        System.out.println("=== Pointer Array Access ===");
        System.out.println(javaCode);
    }

    @Test
    void testPointerInLoop() {
        String cCode = "void test() {\n" +
                       "    int arr[5] = {1, 2, 3, 4, 5};\n" +
                       "    int *ptr = arr;\n" +
                       "    for (int i = 0; i < 5; i++) {\n" +
                       "        printf(\"%d\\n\", *(ptr + i));\n" +
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
        assertTrue(javaCode.contains("arr[i]"), "Should convert *(ptr + i) to arr[i]");
        System.out.println("=== Pointer in Loop ===");
        System.out.println(javaCode);
    }

    @Test
    void testPointerAssignment() {
        String cCode = "void test() {\n" +
                       "    int arr[5] = {1, 2, 3, 4, 5};\n" +
                       "    int *ptr = arr;\n" +
                       "    *(ptr + 1) = 100;\n" +
                       "}";

        Lexer lexer = new Lexer(cCode);
        Parser parser = new Parser(lexer);
        Program program = parser.parse();

        AstTransformer transformer = new AstTransformer();
        Program javaProgram = (Program) transformer.visitProgram(program);

        CodeGenerator generator = new CodeGenerator();
        String javaCode = generator.visitProgram(javaProgram);

        assertNotNull(javaCode);
        assertTrue(javaCode.contains("arr[1] = 100"), "Should convert assignment to array");
        System.out.println("=== Pointer Assignment ===");
        System.out.println(javaCode);
    }
}
