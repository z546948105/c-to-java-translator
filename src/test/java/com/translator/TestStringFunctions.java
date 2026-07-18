package com.translator;

import com.translator.ast.Program;
import com.translator.codegen.CodeGenerator;
import com.translator.parser.Parser;
import com.translator.token.Lexer;
import com.translator.transform.AstTransformer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestStringFunctions {

    @Test
    void testStrcpy() {
        String cCode = "void copyString() {\n" +
                       "    char dest[100];\n" +
                       "    char src[] = \"hello\";\n" +
                       "    strcpy(dest, src);\n" +
                       "}";

        Lexer lexer = new Lexer(cCode);
        Parser parser = new Parser(lexer);
        Program program = parser.parse();

        AstTransformer transformer = new AstTransformer();
        Program javaProgram = (Program) transformer.visitProgram(program);

        CodeGenerator generator = new CodeGenerator();
        String javaCode = generator.visitProgram(javaProgram);

        assertNotNull(javaCode);
        assertTrue(javaCode.contains("dest = src"), "Should generate assignment");
        System.out.println("=== strcpy ===");
        System.out.println(javaCode);
    }

    @Test
    void testStrcat() {
        String cCode = "void concatString() {\n" +
                       "    char str[100] = \"hello\";\n" +
                       "    char append[] = \" world\";\n" +
                       "    strcat(str, append);\n" +
                       "}";

        Lexer lexer = new Lexer(cCode);
        Parser parser = new Parser(lexer);
        Program program = parser.parse();

        AstTransformer transformer = new AstTransformer();
        Program javaProgram = (Program) transformer.visitProgram(program);

        CodeGenerator generator = new CodeGenerator();
        String javaCode = generator.visitProgram(javaProgram);

        assertNotNull(javaCode);
        System.out.println("=== strcat ===");
        System.out.println(javaCode);
        assertTrue(javaCode.contains("str =") && javaCode.contains("+ append"), "Should generate concatenation");
    }

    @Test
    void testStrcmp() {
        String cCode = "int compareString() {\n" +
                       "    char s1[] = \"apple\";\n" +
                       "    char s2[] = \"banana\";\n" +
                       "    return strcmp(s1, s2);\n" +
                       "}";

        Lexer lexer = new Lexer(cCode);
        Parser parser = new Parser(lexer);
        Program program = parser.parse();

        AstTransformer transformer = new AstTransformer();
        Program javaProgram = (Program) transformer.visitProgram(program);

        CodeGenerator generator = new CodeGenerator();
        String javaCode = generator.visitProgram(javaProgram);

        assertNotNull(javaCode);
        assertTrue(javaCode.contains(".compareTo"), "Should generate compareTo");
        System.out.println("=== strcmp ===");
        System.out.println(javaCode);
    }

    @Test
    void testStrlen() {
        String cCode = "int getLength() {\n" +
                       "    char str[] = \"hello\";\n" +
                       "    return strlen(str);\n" +
                       "}";

        Lexer lexer = new Lexer(cCode);
        Parser parser = new Parser(lexer);
        Program program = parser.parse();

        AstTransformer transformer = new AstTransformer();
        Program javaProgram = (Program) transformer.visitProgram(program);

        CodeGenerator generator = new CodeGenerator();
        String javaCode = generator.visitProgram(javaProgram);

        assertNotNull(javaCode);
        assertTrue(javaCode.contains(".length()"), "Should generate length()");
        System.out.println("=== strlen ===");
        System.out.println(javaCode);
    }

    @Test
    void testStrchr() {
        String cCode = "int findChar() {\n" +
                       "    char str[] = \"hello\";\n" +
                       "    return strchr(str, 'l') != NULL;\n" +
                       "}";

        Lexer lexer = new Lexer(cCode);
        Parser parser = new Parser(lexer);
        Program program = parser.parse();

        AstTransformer transformer = new AstTransformer();
        Program javaProgram = (Program) transformer.visitProgram(program);

        CodeGenerator generator = new CodeGenerator();
        String javaCode = generator.visitProgram(javaProgram);

        assertNotNull(javaCode);
        assertTrue(javaCode.contains(".indexOf"), "Should generate indexOf");
        System.out.println("=== strchr ===");
        System.out.println(javaCode);
    }

    @Test
    void testStrstr() {
        String cCode = "int findSubstring() {\n" +
                       "    char str[] = \"hello world\";\n" +
                       "    return strstr(str, \"world\") != NULL;\n" +
                       "}";

        Lexer lexer = new Lexer(cCode);
        Parser parser = new Parser(lexer);
        Program program = parser.parse();

        AstTransformer transformer = new AstTransformer();
        Program javaProgram = (Program) transformer.visitProgram(program);

        CodeGenerator generator = new CodeGenerator();
        String javaCode = generator.visitProgram(javaProgram);

        assertNotNull(javaCode);
        assertTrue(javaCode.contains(".indexOf"), "Should generate indexOf");
        System.out.println("=== strstr ===");
        System.out.println(javaCode);
    }

    @Test
    void testStrncpy() {
        String cCode = "void copyNChars() {\n" +
                       "    char dest[100];\n" +
                       "    char src[] = \"hello world\";\n" +
                       "    strncpy(dest, src, 5);\n" +
                       "}";

        Lexer lexer = new Lexer(cCode);
        Parser parser = new Parser(lexer);
        Program program = parser.parse();

        AstTransformer transformer = new AstTransformer();
        Program javaProgram = (Program) transformer.visitProgram(program);

        CodeGenerator generator = new CodeGenerator();
        String javaCode = generator.visitProgram(javaProgram);

        assertNotNull(javaCode);
        assertTrue(javaCode.contains(".substring"), "Should generate substring");
        System.out.println("=== strncpy ===");
        System.out.println(javaCode);
    }

    @Test
    void testStrncat() {
        String cCode = "void concatNChars() {\n" +
                       "    char str[100] = \"hello\";\n" +
                       "    char append[] = \" world today\";\n" +
                       "    strncat(str, append, 6);\n" +
                       "}";

        Lexer lexer = new Lexer(cCode);
        Parser parser = new Parser(lexer);
        Program program = parser.parse();

        AstTransformer transformer = new AstTransformer();
        Program javaProgram = (Program) transformer.visitProgram(program);

        CodeGenerator generator = new CodeGenerator();
        String javaCode = generator.visitProgram(javaProgram);

        assertNotNull(javaCode);
        assertTrue(javaCode.contains(".substring"), "Should generate substring");
        assertTrue(javaCode.contains("+"), "Should generate concatenation");
        System.out.println("=== strncat ===");
        System.out.println(javaCode);
    }
}
