package com.translator.preprocessor;

import java.util.*;

/**
 * 预处理器（Preprocessor）
 * <p>
 * 在词法分析之前对 C 源代码进行预处理，主要功能：
 * - 解析 #define 宏定义
 * - 对象宏（无参数）的文本替换
 * - 函数宏（有参数）的参数展开和文本替换
 * - 防止递归宏展开
 * - 处理注释（保留行号信息）
 */
public class Preprocessor {
    private final Map<String, Macro> macros = new HashMap<>();
    private final Set<String> expandingMacros = new HashSet<>();

    /**
     * 宏定义类
     */
    private static class Macro {
        private final String name;
        private final List<String> parameters;
        private final String body;

        public Macro(String name, List<String> parameters, String body) {
            this.name = name;
            this.parameters = parameters;
            this.body = body;
        }

        public boolean isFunctionMacro() {
            return parameters != null && !parameters.isEmpty();
        }

        public String expand(List<String> arguments) {
            if (!isFunctionMacro()) {
                return body;
            }
            String expanded = body;
            for (int i = 0; i < parameters.size(); i++) {
                String param = parameters.get(i);
                String arg = i < arguments.size() ? arguments.get(i) : "";
                expanded = replaceParameter(expanded, param, arg);
            }
            return expanded;
        }

        private String replaceParameter(String text, String param, String arg) {
            StringBuilder result = new StringBuilder();
            int i = 0;
            while (i < text.length()) {
                int idx = text.indexOf(param, i);
                if (idx == -1) {
                    result.append(text.substring(i));
                    break;
                }
                boolean isWordBoundary = true;
                if (idx > 0 && Character.isJavaIdentifierPart(text.charAt(idx - 1))) {
                    isWordBoundary = false;
                }
                if (idx + param.length() < text.length() && Character.isJavaIdentifierPart(text.charAt(idx + param.length()))) {
                    isWordBoundary = false;
                }
                if (isWordBoundary) {
                    result.append(text.substring(i, idx));
                    result.append(arg);
                    i = idx + param.length();
                } else {
                    result.append(text.charAt(i));
                    i++;
                }
            }
            return result.toString();
        }
    }

    /**
     * 预处理 C 源代码，进行宏展开
     *
     * @param source C 源代码
     * @return 宏展开后的源代码
     */
    public String preprocess(String source) {
        macros.clear();
        expandingMacros.clear();

        String normalized = source.replace("\\\n", "");
        List<String> lines = Arrays.asList(normalized.split("\n"));
        StringBuilder result = new StringBuilder();

        for (int lineNum = 0; lineNum < lines.size(); lineNum++) {
            String line = lines.get(lineNum);
            String trimmed = line.trim();

            if (trimmed.startsWith("#define")) {
                parseDefine(line);
            } else if (trimmed.startsWith("#")) {
                result.append(line).append("\n");
            } else {
                String expandedLine = expandLine(line);
                result.append(expandedLine).append("\n");
            }
        }

        return result.toString();
    }

    /**
     * 解析 #define 指令
     */
    private void parseDefine(String line) {
        String remaining = line.trim().substring(7).trim();

        int nameEnd = 0;
        while (nameEnd < remaining.length() && Character.isJavaIdentifierPart(remaining.charAt(nameEnd))) {
            nameEnd++;
        }

        String name = remaining.substring(0, nameEnd);
        if (name.isEmpty()) {
            return;
        }

        List<String> parameters = null;
        String body = "";

        remaining = remaining.substring(nameEnd);

        if (remaining.startsWith("(")) {
            int paramEnd = findMatchingParen(remaining, 0);
            if (paramEnd != -1) {
                String paramStr = remaining.substring(1, paramEnd);
                parameters = parseParameters(paramStr);
                remaining = remaining.substring(paramEnd + 1).trim();
            }
        } else {
            remaining = remaining.trim();
        }

        if (remaining.startsWith("=")) {
            body = remaining.substring(1).trim();
        } else {
            body = remaining;
        }

        macros.put(name, new Macro(name, parameters, body));
    }

    /**
     * 查找匹配的右括号
     */
    private int findMatchingParen(String str, int start) {
        int depth = 0;
        for (int i = start; i < str.length(); i++) {
            if (str.charAt(i) == '(') {
                depth++;
            } else if (str.charAt(i) == ')') {
                depth--;
                if (depth == 0) {
                    return i;
                }
            } else if (str.charAt(i) == '"' || str.charAt(i) == '\'') {
                i = skipStringLiteral(str, i);
            }
        }
        return -1;
    }

    /**
     * 跳过字符串字面量
     */
    private int skipStringLiteral(String str, int start) {
        char quote = str.charAt(start);
        int i = start + 1;
        while (i < str.length()) {
            if (str.charAt(i) == '\\' && i + 1 < str.length()) {
                i += 2;
                continue;
            }
            if (str.charAt(i) == quote) {
                return i;
            }
            i++;
        }
        return i;
    }

    /**
     * 解析函数宏参数
     */
    private List<String> parseParameters(String paramStr) {
        List<String> params = new ArrayList<>();
        String[] parts = paramStr.split(",");
        for (String part : parts) {
            String param = part.trim();
            if (!param.isEmpty()) {
                params.add(param);
            }
        }
        return params;
    }

    /**
     * 展开一行代码中的所有宏
     */
    private String expandLine(String line) {
        StringBuilder result = new StringBuilder();
        int i = 0;
        while (i < line.length()) {
            if (line.charAt(i) == '"' || line.charAt(i) == '\'') {
                int end = skipStringLiteral(line, i);
                result.append(line.substring(i, end + 1));
                i = end + 1;
            } else if (line.charAt(i) == '/') {
                if (i + 1 < line.length() && line.charAt(i + 1) == '/') {
                    result.append(line.substring(i));
                    break;
                } else if (i + 1 < line.length() && line.charAt(i + 1) == '*') {
                    result.append("/*");
                    i += 2;
                    while (i + 1 < line.length() && !(line.charAt(i) == '*' && line.charAt(i + 1) == '/')) {
                        result.append(line.charAt(i));
                        i++;
                    }
                    if (i + 1 < line.length()) {
                        result.append("*/");
                        i += 2;
                    }
                } else {
                    result.append(line.charAt(i));
                    i++;
                }
            } else if (Character.isJavaIdentifierStart(line.charAt(i))) {
                StringBuilder identifier = new StringBuilder();
                while (i < line.length() && Character.isJavaIdentifierPart(line.charAt(i))) {
                    identifier.append(line.charAt(i));
                    i++;
                }
                String name = identifier.toString();
                if (macros.containsKey(name) && !expandingMacros.contains(name)) {
                    expandingMacros.add(name);
                    Macro macro = macros.get(name);
                    if (macro.isFunctionMacro()) {
                        if (i < line.length() && line.charAt(i) == '(') {
                            int argEnd = findMatchingParen(line, i);
                            if (argEnd != -1) {
                                String argStr = line.substring(i + 1, argEnd);
                                List<String> args = parseArguments(argStr);
                                String expanded = macro.expand(args);
                                String recursivelyExpanded = expandLine(expanded);
                                result.append(recursivelyExpanded);
                                i = argEnd + 1;
                            } else {
                                result.append(name);
                            }
                        } else {
                            result.append(name);
                        }
                    } else {
                        String expanded = macro.expand(null);
                        String recursivelyExpanded = expandLine(expanded);
                        result.append(recursivelyExpanded);
                    }
                    expandingMacros.remove(name);
                } else {
                    result.append(name);
                }
            } else {
                result.append(line.charAt(i));
                i++;
            }
        }
        return result.toString();
    }

    /**
     * 解析函数宏调用参数
     */
    private List<String> parseArguments(String argStr) {
        List<String> args = new ArrayList<>();
        StringBuilder currentArg = new StringBuilder();
        int parenDepth = 0;

        for (int i = 0; i < argStr.length(); i++) {
            char c = argStr.charAt(i);

            if (c == '"' || c == '\'') {
                currentArg.append(c);
                i = skipStringLiteral(argStr, i);
                currentArg.append(argStr.charAt(i));
            } else if (c == '(') {
                parenDepth++;
                currentArg.append(c);
            } else if (c == ')') {
                parenDepth--;
                currentArg.append(c);
            } else if (c == ',' && parenDepth == 0) {
                args.add(currentArg.toString().trim());
                currentArg = new StringBuilder();
            } else {
                currentArg.append(c);
            }
        }

        if (currentArg.length() > 0) {
            args.add(currentArg.toString().trim());
        }

        return args;
    }

    /**
     * 获取已定义的宏列表
     */
    public Set<String> getDefinedMacros() {
        return macros.keySet();
    }

    /**
     * 检查宏是否已定义
     */
    public boolean isMacroDefined(String name) {
        return macros.containsKey(name);
    }
}