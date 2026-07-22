package com.translator.error;

public enum ErrorType {
    SYNTAX_ERROR("Syntax Error", "语法错误"),
    SEMANTIC_ERROR("Semantic Error", "语义错误"),
    TYPE_ERROR("Type Error", "类型错误"),
    UNSUPPORTED_FEATURE("Unsupported Feature", "不支持的特性"),
    PARSE_ERROR("Parse Error", "解析错误"),
    PREPROCESSOR_ERROR("Preprocessor Error", "预处理错误"),
    TRANSFORMATION_ERROR("Transformation Error", "转换错误"),
    CODE_GENERATION_ERROR("Code Generation Error", "代码生成错误");

    private final String englishName;
    private final String chineseName;

    ErrorType(String englishName, String chineseName) {
        this.englishName = englishName;
        this.chineseName = chineseName;
    }

    public String getEnglishName() {
        return englishName;
    }

    public String getChineseName() {
        return chineseName;
    }

    @Override
    public String toString() {
        return englishName;
    }
}