package com.translator.parser;

import com.translator.ast.AstNode;
import com.translator.ast.Program;
import com.translator.error.ErrorCollector;
import com.translator.token.Lexer;
import com.translator.token.TokenType;

import java.util.ArrayList;
import java.util.List;

/**
 * 语法分析器（Parser）
 * <p>
 * 将 token 流解析为抽象语法树（AST）
 * <p>
 * 使用递归下降解析方法，支持 C 语言的主要语法结构：
 * - 函数声明和定义
 * - 变量声明和初始化
 * - 结构体和枚举定义
 * - 宏定义（#define）
 * - 控制流语句（if, while, for, switch）
 * - 表达式和赋值语句
 */
public class Parser extends ParserBase {
    private final TypeParser typeParser;
    private final ExpressionParser expressionParser;
    private final StatementParser statementParser;
    private final DeclarationParser declarationParser;

    public Parser(Lexer lexer) {
        super(lexer);
        this.typeParser = new TypeParser(this);
        this.expressionParser = new ExpressionParser(this, typeParser);
        this.typeParser.setExpressionParser(expressionParser);
        this.statementParser = new StatementParser(this, typeParser, null, expressionParser);
        this.declarationParser = new DeclarationParser(this, typeParser, statementParser);
        this.statementParser.setDeclarationParser(declarationParser);
    }

    public Parser(Lexer lexer, String source) {
        super(lexer, source);
        this.typeParser = new TypeParser(this);
        this.expressionParser = new ExpressionParser(this, typeParser);
        this.typeParser.setExpressionParser(expressionParser);
        this.statementParser = new StatementParser(this, typeParser, null, expressionParser);
        this.declarationParser = new DeclarationParser(this, typeParser, statementParser);
        this.statementParser.setDeclarationParser(declarationParser);
    }

    public Parser(Lexer lexer, String source, ErrorCollector errorCollector) {
        super(lexer, source, errorCollector);
        this.typeParser = new TypeParser(this);
        this.expressionParser = new ExpressionParser(this, typeParser);
        this.typeParser.setExpressionParser(expressionParser);
        this.statementParser = new StatementParser(this, typeParser, null, expressionParser);
        this.declarationParser = new DeclarationParser(this, typeParser, statementParser);
        this.statementParser.setDeclarationParser(declarationParser);
    }

    public ErrorCollector getErrorCollector() {
        return errorCollector;
    }

    public Program parse() {
        List<AstNode> declarations = new ArrayList<>();
        while (!match(TokenType.EOF)) {
            try {
                AstNode decl = declarationParser.parseDeclaration();
                if (decl != null) {
                    declarations.add(decl);
                }
            } catch (RuntimeException e) {
                String reason = e.getMessage();
                declarations.add(createUnsupportedCode(reason));
                panicModeRecovery();
            }
        }
        return new Program(declarations);
    }
}