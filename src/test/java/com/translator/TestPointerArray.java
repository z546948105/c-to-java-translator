package com.translator;

import com.translator.ast.Program;
import com.translator.codegen.CodeGenerator;
import com.translator.parser.Parser;
import com.translator.token.Lexer;
import com.translator.transform.AstTransformer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestPointerArray {

    @Test
    void testPointerArrayDeclaration() {
        String cCode = "void test() {\n" +
                       "    int* arr[10];\n" +
                       "}";

        Lexer lexer = new Lexer(cCode);
        Parser parser = new Parser(lexer);
        Program program = parser.parse();

        AstTransformer transformer = new AstTransformer();
        Program javaProgram = (Program) transformer.visitProgram(program);

        CodeGenerator generator = new CodeGenerator();
        String javaCode = generator.visitProgram(javaProgram);

        assertNotNull(javaCode);
        assertTrue(javaCode.contains("Object[10] arr"), "Should convert int* arr[10] to Object[10] arr");
        System.out.println("=== Pointer Array Declaration ===");
        System.out.println(javaCode);
    }

    @Test
    void testPointerArrayWithoutSize() {
        String cCode = "void test() {\n" +
                       "    int* arr[];\n" +
                       "}";

        Lexer lexer = new Lexer(cCode);
        Parser parser = new Parser(lexer);
        Program program = parser.parse();

        AstTransformer transformer = new AstTransformer();
        Program javaProgram = (Program) transformer.visitProgram(program);

        CodeGenerator generator = new CodeGenerator();
        String javaCode = generator.visitProgram(javaProgram);

        assertNotNull(javaCode);
        assertTrue(javaCode.contains("Object[] arr"), "Should convert int* arr[] to Object[] arr");
        System.out.println("=== Pointer Array Without Size ===");
        System.out.println(javaCode);
    }

    @Test
    void testDoublePointerArray() {
        String cCode = "void test() {\n" +
                       "    int** arr[5];\n" +
                       "}";

        Lexer lexer = new Lexer(cCode);
        Parser parser = new Parser(lexer);
        Program program = parser.parse();

        AstTransformer transformer = new AstTransformer();
        Program javaProgram = (Program) transformer.visitProgram(program);

        CodeGenerator generator = new CodeGenerator();
        String javaCode = generator.visitProgram(javaProgram);

        assertNotNull(javaCode);
        assertTrue(javaCode.contains("Object[5] arr"), "Should convert int** arr[5] to Object[5] arr");
        System.out.println("=== Double Pointer Array ===");
        System.out.println(javaCode);
    }

    @Test
    void testCharPointerArray() {
        String cCode = "void test() {\n" +
                       "    char* names[3];\n" +
                       "}";

        Lexer lexer = new Lexer(cCode);
        Parser parser = new Parser(lexer);
        Program program = parser.parse();

        AstTransformer transformer = new AstTransformer();
        Program javaProgram = (Program) transformer.visitProgram(program);

        CodeGenerator generator = new CodeGenerator();
        String javaCode = generator.visitProgram(javaProgram);

        assertNotNull(javaCode);
        assertTrue(javaCode.contains("Object[3] names"), "Should convert char* names[3] to Object[3] names");
        System.out.println("=== Char Pointer Array ===");
        System.out.println(javaCode);
    }

    @Test
    void testPointerArrayWithInitialization() {
        String cCode = "void test() {\n" +
                       "    int x = 10;\n" +
                       "    int y = 20;\n" +
                       "    int* arr[2] = {&x, &y};\n" +
                       "}";

        Lexer lexer = new Lexer(cCode);
        Parser parser = new Parser(lexer);
        Program program = parser.parse();

        AstTransformer transformer = new AstTransformer();
        Program javaProgram = (Program) transformer.visitProgram(program);

        CodeGenerator generator = new CodeGenerator();
        String javaCode = generator.visitProgram(javaProgram);

        assertNotNull(javaCode);
        assertTrue(javaCode.contains("Object[2] arr"), "Should convert int* arr[2] to Object[2] arr");
        System.out.println("=== Pointer Array With Initialization ===");
        System.out.println(javaCode);
    }

    @Test
    void testFunctionWithPointerArrayParameter() {
        String cCode = "void process(int* arr[], int count) {\n" +
                       "}\n";

        Lexer lexer = new Lexer(cCode);
        Parser parser = new Parser(lexer);
        Program program = parser.parse();

        AstTransformer transformer = new AstTransformer();
        Program javaProgram = (Program) transformer.visitProgram(program);

        CodeGenerator generator = new CodeGenerator();
        String javaCode = generator.visitProgram(javaProgram);

        assertNotNull(javaCode);
        assertTrue(javaCode.contains("Object[] arr"), "Should convert int* arr[] parameter to Object[] arr");
        System.out.println("=== Function With Pointer Array Parameter ===");
        System.out.println(javaCode);
    }
}
