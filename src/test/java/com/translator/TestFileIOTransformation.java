package com.translator;

import com.translator.ast.Program;
import com.translator.codegen.CodeGenerator;
import com.translator.parser.Parser;
import com.translator.token.Lexer;
import com.translator.transform.AstTransformer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestFileIOTransformation {

    @Test
    void testFopenReadMode() {
        String cCode = "void readFile() {\n" +
                       "    FILE *fp = fopen(\"test.txt\", \"r\");\n" +
                       "}";

        Lexer lexer = new Lexer(cCode);
        Parser parser = new Parser(lexer);
        Program program = parser.parse();

        AstTransformer transformer = new AstTransformer();
        Program javaProgram = (Program) transformer.visitProgram(program);

        CodeGenerator generator = new CodeGenerator();
        String javaCode = generator.visitProgram(javaProgram);

        assertNotNull(javaCode);
        assertTrue(javaCode.contains("FileReader"), "Should use FileReader for read mode");
        assertTrue(javaCode.contains("test.txt"), "Should have filename");
        System.out.println("=== fopen Read Mode ===");
        System.out.println(javaCode);
    }

    @Test
    void testFopenWriteMode() {
        String cCode = "void writeFile() {\n" +
                       "    FILE *fp = fopen(\"output.txt\", \"w\");\n" +
                       "}";

        Lexer lexer = new Lexer(cCode);
        Parser parser = new Parser(lexer);
        Program program = parser.parse();

        AstTransformer transformer = new AstTransformer();
        Program javaProgram = (Program) transformer.visitProgram(program);

        CodeGenerator generator = new CodeGenerator();
        String javaCode = generator.visitProgram(javaProgram);

        assertNotNull(javaCode);
        assertTrue(javaCode.contains("FileWriter"), "Should use FileWriter for write mode");
        System.out.println("=== fopen Write Mode ===");
        System.out.println(javaCode);
    }

    @Test
    void testFopenBinaryReadMode() {
        String cCode = "void readBinary() {\n" +
                       "    FILE *fp = fopen(\"data.bin\", \"rb\");\n" +
                       "}";

        Lexer lexer = new Lexer(cCode);
        Parser parser = new Parser(lexer);
        Program program = parser.parse();

        AstTransformer transformer = new AstTransformer();
        Program javaProgram = (Program) transformer.visitProgram(program);

        CodeGenerator generator = new CodeGenerator();
        String javaCode = generator.visitProgram(javaProgram);

        assertNotNull(javaCode);
        assertTrue(javaCode.contains("FileInputStream"), "Should use FileInputStream for binary read");
        System.out.println("=== fopen Binary Read Mode ===");
        System.out.println(javaCode);
    }

    @Test
    void testFclose() {
        String cCode = "void closeFile() {\n" +
                       "    FILE *fp = fopen(\"test.txt\", \"r\");\n" +
                       "    fclose(fp);\n" +
                       "}";

        Lexer lexer = new Lexer(cCode);
        Parser parser = new Parser(lexer);
        Program program = parser.parse();

        AstTransformer transformer = new AstTransformer();
        Program javaProgram = (Program) transformer.visitProgram(program);

        CodeGenerator generator = new CodeGenerator();
        String javaCode = generator.visitProgram(javaProgram);

        assertNotNull(javaCode);
        assertTrue(javaCode.contains(".close()"), "Should generate close method call");
        System.out.println("=== fclose ===");
        System.out.println(javaCode);
    }

    @Test
    void testFprintf() {
        String cCode = "void logMessage() {\n" +
                       "    FILE *fp = fopen(\"log.txt\", \"w\");\n" +
                       "    fprintf(fp, \"Value: %d\\n\", 42);\n" +
                       "}";

        Lexer lexer = new Lexer(cCode);
        Parser parser = new Parser(lexer);
        Program program = parser.parse();

        AstTransformer transformer = new AstTransformer();
        Program javaProgram = (Program) transformer.visitProgram(program);

        CodeGenerator generator = new CodeGenerator();
        String javaCode = generator.visitProgram(javaProgram);

        assertNotNull(javaCode);
        assertTrue(javaCode.contains(".printf"), "Should generate printf method call");
        System.out.println("=== fprintf ===");
        System.out.println(javaCode);
    }

    @Test
    void testFgets() {
        String cCode = "void readLine() {\n" +
                       "    FILE *fp = fopen(\"test.txt\", \"r\");\n" +
                       "    char buf[100];\n" +
                       "    fgets(buf, 100, fp);\n" +
                       "}";

        Lexer lexer = new Lexer(cCode);
        Parser parser = new Parser(lexer);
        Program program = parser.parse();

        AstTransformer transformer = new AstTransformer();
        Program javaProgram = (Program) transformer.visitProgram(program);

        CodeGenerator generator = new CodeGenerator();
        String javaCode = generator.visitProgram(javaProgram);

        assertNotNull(javaCode);
        assertTrue(javaCode.contains(".readLine"), "Should generate readLine method call");
        System.out.println("=== fgets ===");
        System.out.println(javaCode);
    }
}
