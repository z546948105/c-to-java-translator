package com.translator;

import com.translator.ast.Program;
import com.translator.codegen.CodeGenerator;
import com.translator.parser.Parser;
import com.translator.token.Lexer;
import com.translator.transform.AstTransformer;

public class Translator {
    private final Lexer lexer;
    private final Parser parser;
    private final AstTransformer transformer;
    private final CodeGenerator codeGenerator;

    public Translator(String cCode) {
        this.lexer = new Lexer(cCode);
        this.parser = new Parser(lexer);
        this.transformer = new AstTransformer();
        this.codeGenerator = new CodeGenerator();
    }

    public Translator(String cCode, String className) {
        this.lexer = new Lexer(cCode);
        this.parser = new Parser(lexer);
        this.transformer = new AstTransformer(className);
        this.codeGenerator = new CodeGenerator();
    }

    public String translate() {
        Program cAst = parser.parse();
        Program javaAst = (Program) cAst.accept(transformer);
        return codeGenerator.visitProgram(javaAst);
    }

    public static String translateCode(String cCode) {
        Translator translator = new Translator(cCode);
        return translator.translate();
    }

    public static String translateCode(String cCode, String className) {
        Translator translator = new Translator(cCode, className);
        return translator.translate();
    }
}