package com.translator;

import com.translator.ast.Program;
import com.translator.codegen.CodeGenerator;
import com.translator.parser.Parser;
import com.translator.token.Lexer;
import com.translator.transform.AstTransformer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TestPointerOffset {

    @Test
    void testPointerOffsetTranslation() {
        String cCode = "int arr[5] = {1, 2, 3, 4, 5};\n" +
                      "int *ptr = arr;\n" +
                      "int x = *(ptr + 2);\n";

        Lexer lexer = new Lexer(cCode);
        Parser parser = new Parser(lexer);

        Program program = parser.parse();

        AstTransformer transformer = new AstTransformer();
        Program javaProgram = (Program) transformer.visitProgram(program);

        CodeGenerator generator = new CodeGenerator();
        String javaCode = generator.visitProgram(javaProgram);

        assertNotNull(javaCode);
        System.out.println("=== Generated Java Code ===");
        System.out.println(javaCode);
        assertTrue(javaCode.contains("arr[2]"), "Should convert *(ptr + 2) to arr[2]");
    }

    @Test
    void testSimplePointerDereference() {
        String cCode = "int a = 10;\n" +
                      "int *p = &a;\n" +
                      "int b = *p;\n";

        Lexer lexer = new Lexer(cCode);
        Parser parser = new Parser(lexer);

        Program program = parser.parse();

        AstTransformer transformer = new AstTransformer();
        Program javaProgram = (Program) transformer.visitProgram(program);

        CodeGenerator generator = new CodeGenerator();
        String javaCode = generator.visitProgram(javaProgram);

        assertNotNull(javaCode);
        System.out.println("=== Simple Pointer Test ===");
        System.out.println(javaCode);
    }
}
