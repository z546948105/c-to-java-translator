package com.translator;

import com.translator.ast.Program;
import com.translator.codegen.CodeGenerator;
import com.translator.parser.Parser;
import com.translator.token.Lexer;
import com.translator.transform.AstTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Translator {
    private static final Logger log = LoggerFactory.getLogger(Translator.class);
    
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
        log.debug("Starting translation process");
        long startTime = System.currentTimeMillis();
        
        Program cAst = parser.parse();
        log.debug("Parsing completed, declarations count: {}", cAst.getDeclarations().size());
        
        Program javaAst = (Program) cAst.accept(transformer);
        log.debug("Transformation completed");
        
        String result = codeGenerator.visitProgram(javaAst);
        long endTime = System.currentTimeMillis();
        
        log.debug("Translation completed in {}ms, output length: {}", endTime - startTime, result.length());
        return result;
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