package com.translator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 宏转换测试类
 * <p>
 * 验证宏在预处理阶段展开后的代码行为
 */
public class TestMacroTransformation {

    @Test
    void testSimpleMacroFunction() {
        String cCode = "#define MAX(a,b) ((a) > (b) ? (a) : (b))\n" +
                       "int test() {\n" +
                       "    return MAX(10, 20);\n" +
                       "}";

        String javaCode = Translator.translateCode(cCode);

        assertNotNull(javaCode);
        assertTrue(javaCode.contains("return"), "Should have return statement");
        assertTrue(javaCode.contains(">"), "Should have comparison");
        System.out.println("=== Simple Macro Function ===");
        System.out.println(javaCode);
    }

    @Test
    void testMacroWithMultipleParams() {
        String cCode = "#define ADD(a,b,c) ((a) + (b) + (c))\n" +
                       "int test() {\n" +
                       "    return ADD(1, 2, 3);\n" +
                       "}";

        String javaCode = Translator.translateCode(cCode);

        assertNotNull(javaCode);
        assertTrue(javaCode.contains("return"), "Should have return statement");
        assertTrue(javaCode.contains("+"), "Should have addition");
        System.out.println("=== Macro With Multiple Params ===");
        System.out.println(javaCode);
    }

    @Test
    void testMacroWithExpression() {
        String cCode = "#define SQUARE(x) ((x) * (x))\n" +
                       "int test() {\n" +
                       "    return SQUARE(5);\n" +
                       "}";

        String javaCode = Translator.translateCode(cCode);

        assertNotNull(javaCode);
        assertTrue(javaCode.contains("return"), "Should have return statement");
        assertTrue(javaCode.contains("*"), "Should have multiplication");
        System.out.println("=== Macro With Expression ===");
        System.out.println(javaCode);
    }
}