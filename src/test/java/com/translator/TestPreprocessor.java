package com.translator;

import com.translator.preprocessor.Preprocessor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 预处理器测试类
 * <p>
 * 验证宏展开功能：对象宏、函数宏、递归宏展开等
 */
public class TestPreprocessor {

    @Test
    public void testObjectMacro() {
        String source = "#define MAX 100\nint x = MAX;";
        Preprocessor preprocessor = new Preprocessor();
        String result = preprocessor.preprocess(source);
        
        System.out.println("=== Object Macro Test ===");
        System.out.println("Input: " + source);
        System.out.println("Output: " + result);
        System.out.println("Defined macros: " + preprocessor.getDefinedMacros());
        
        assertTrue(result.contains("int x = 100;"), 
                   "Object macro should be expanded: " + result);
    }

    @Test
    public void testFunctionMacro() {
        String source = "#define ADD(a, b) (a + b)\nint x = ADD(1, 2);";
        Preprocessor preprocessor = new Preprocessor();
        String result = preprocessor.preprocess(source);
        
        assertTrue(result.contains("int x = (1 + 2);"), 
                   "Function macro should be expanded: " + result);
    }

    @Test
    public void testFunctionMacroWithMultipleParams() {
        String source = "#define MAX(a, b) ((a) > (b) ? (a) : (b))\nint x = MAX(10, 20);";
        Preprocessor preprocessor = new Preprocessor();
        String result = preprocessor.preprocess(source);
        
        assertTrue(result.contains("int x = ((10) > (20) ? (10) : (20));"), 
                   "Multi-param function macro should be expanded: " + result);
    }

    @Test
    public void testNestedMacro() {
        String source = "#define A 10\n#define B A + 5\nint x = B;";
        Preprocessor preprocessor = new Preprocessor();
        String result = preprocessor.preprocess(source);
        
        assertTrue(result.contains("int x = 10 + 5;"), 
                   "Nested macro should be expanded: " + result);
    }

    @Test
    public void testRecursiveMacroPrevented() {
        String source = "#define SELF SELF\nint x = SELF;";
        Preprocessor preprocessor = new Preprocessor();
        String result = preprocessor.preprocess(source);
        
        assertTrue(result.contains("int x = SELF;"), 
                   "Recursive macro should be prevented: " + result);
    }

    @Test
    public void testMacroInStringLiteral() {
        String source = "#define NAME \"test\"\nchar* str = \"NAME\";";
        Preprocessor preprocessor = new Preprocessor();
        String result = preprocessor.preprocess(source);
        
        assertTrue(result.contains("char* str = \"NAME\";"), 
                   "Macro in string literal should not be expanded: " + result);
    }

    @Test
    public void testMacroWithArgumentsContainingExpressions() {
        String source = "#define SQUARE(x) (x) * (x)\nint x = SQUARE(2 + 3);";
        Preprocessor preprocessor = new Preprocessor();
        String result = preprocessor.preprocess(source);
        
        assertTrue(result.contains("int x = (2 + 3) * (2 + 3);"), 
                   "Macro with expression arguments should be expanded: " + result);
    }

    @Test
    public void testMultipleMacros() {
        String source = "#define PI 3.14\n#define RADIUS 5\nfloat area = PI * RADIUS * RADIUS;";
        Preprocessor preprocessor = new Preprocessor();
        String result = preprocessor.preprocess(source);
        
        assertTrue(result.contains("float area = 3.14 * 5 * 5;"), 
                   "Multiple macros should be expanded: " + result);
    }

    @Test
    public void testPreprocessorInTranslation() {
        String cCode = "#define MAX(a, b) ((a) > (b) ? (a) : (b))\n" +
                       "int test() {\n" +
                       "    int x = 10;\n" +
                       "    int y = 20;\n" +
                       "    return MAX(x, y);\n" +
                       "}";
        
        String javaCode = Translator.translateCode(cCode);
        
        assertNotNull(javaCode);
        System.out.println("=== Preprocessor Test ===");
        System.out.println(javaCode);
        assertTrue(javaCode.contains("return") && javaCode.contains("x > y"), 
                   "Macro should be expanded in translation: " + javaCode);
    }

    @Test
    public void testMacroWithParenthesesInArguments() {
        String source = "#define FUNC(a, b) (a + b)\nint x = FUNC((1+2), (3*4));";
        Preprocessor preprocessor = new Preprocessor();
        String result = preprocessor.preprocess(source);
        
        assertTrue(result.contains("int x = ((1+2) + (3*4));"), 
                   "Macro with parentheses in arguments should be expanded: " + result);
    }

    @Test
    public void testUndefinedMacro() {
        String source = "int x = UNDEFINED_MACRO;";
        Preprocessor preprocessor = new Preprocessor();
        String result = preprocessor.preprocess(source);
        
        assertTrue(result.contains("int x = UNDEFINED_MACRO;"), 
                   "Undefined macro should not be changed: " + result);
    }

    @Test
    public void testMultiLineMacro() {
        String source = "#define ADD(a, b) (a) + \\\n    (b)\nint x = ADD(1, 2);";
        Preprocessor preprocessor = new Preprocessor();
        String result = preprocessor.preprocess(source);
        
        assertTrue(result.contains("int x = (1)") && result.contains("(2)"), 
                   "Multi-line macro should be expanded: " + result);
    }

    @Test
    public void testFunctionMacroWithNoSpace() {
        String source = "#define FOO(x) (x + 1)\nint x = FOO(5);";
        Preprocessor preprocessor = new Preprocessor();
        String result = preprocessor.preprocess(source);
        
        assertTrue(result.contains("int x = (5 + 1);"), 
                   "Function macro FOO(x) should be expanded: " + result);
    }

    @Test
    public void testObjectMacroWithSpace() {
        String source = "#define FOO (x + 1)\nint x = FOO;";
        Preprocessor preprocessor = new Preprocessor();
        String result = preprocessor.preprocess(source);
        
        assertTrue(result.contains("int x = (x + 1);"), 
                   "Object macro FOO (x+1) should be expanded: " + result);
    }

    @Test
    public void testObjectMacroNotCalledAsFunction() {
        String source = "#define FOO (x + 1)\nint x = FOO(5);";
        Preprocessor preprocessor = new Preprocessor();
        String result = preprocessor.preprocess(source);
        
        assertTrue(result.contains("int x = (x + 1)(5);"), 
                   "Object macro FOO should not be treated as function: " + result);
    }
}