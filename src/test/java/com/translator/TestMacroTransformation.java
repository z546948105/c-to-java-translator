package com.translator;

import com.translator.ast.Program;
import com.translator.codegen.CodeGenerator;
import com.translator.parser.Parser;
import com.translator.token.Lexer;
import com.translator.transform.AstTransformer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestMacroTransformation {

    @Test
    void testSimpleMacroFunction() {
        String cCode = "#define MAX(a,b) ((a) > (b) ? (a) : (b))\n";

        Lexer lexer = new Lexer(cCode);
        Parser parser = new Parser(lexer);

        Program program = parser.parse();

        AstTransformer transformer = new AstTransformer();
        Program javaProgram = (Program) transformer.visitProgram(program);

        CodeGenerator generator = new CodeGenerator();
        String javaCode = generator.visitProgram(javaProgram);

        assertNotNull(javaCode);
        assertTrue(javaCode.contains("public static int max"), "Should generate static method");
        assertTrue(javaCode.contains("int a"), "Should have parameter a");
        assertTrue(javaCode.contains("int b"), "Should have parameter b");
        System.out.println("=== Simple Macro Function ===");
        System.out.println(javaCode);
    }

    @Test
    void testMacroWithMultipleParams() {
        String cCode = "#define ADD(a,b,c) ((a) + (b) + (c))\n";

        Lexer lexer = new Lexer(cCode);
        Parser parser = new Parser(lexer);

        Program program = parser.parse();

        AstTransformer transformer = new AstTransformer();
        Program javaProgram = (Program) transformer.visitProgram(program);

        CodeGenerator generator = new CodeGenerator();
        String javaCode = generator.visitProgram(javaProgram);

        assertNotNull(javaCode);
        assertTrue(javaCode.contains("int a"), "Should have parameter a");
        assertTrue(javaCode.contains("int b"), "Should have parameter b");
        assertTrue(javaCode.contains("int c"), "Should have parameter c");
        System.out.println("=== Macro With Multiple Params ===");
        System.out.println(javaCode);
    }

    @Test
    void testMacroWithExpression() {
        String cCode = "#define SQUARE(x) ((x) * (x))\n";

        Lexer lexer = new Lexer(cCode);
        Parser parser = new Parser(lexer);

        Program program = parser.parse();

        AstTransformer transformer = new AstTransformer();
        Program javaProgram = (Program) transformer.visitProgram(program);

        CodeGenerator generator = new CodeGenerator();
        String javaCode = generator.visitProgram(javaProgram);

        assertNotNull(javaCode);
        assertTrue(javaCode.contains("return ((x)") && javaCode.contains("*") && javaCode.contains("(x))"), "Should have correct return expression");
        System.out.println("=== Macro With Expression ===");
        System.out.println(javaCode);
    }
}
