package com.translator.transform;

import com.translator.ast.AstNode;
import com.translator.ast.FunctionCall;
import com.translator.ast.Identifier;
import com.translator.ast.Literal;

import java.util.ArrayList;
import java.util.List;

public class StdlibMapper {
    public static boolean isStdlibFunction(String name) {
        return stdlibMap.containsKey(name);
    }

    public static String mapFunctionName(String name) {
        return stdlibMap.getOrDefault(name, name);
    }

    public static FunctionCall mapFunctionCall(FunctionCall call) {
        String name = call.getName().getName();
        if (!stdlibMap.containsKey(name)) {
            return call;
        }

        String javaName = stdlibMap.get(name);
        List<AstNode> newArgs = new ArrayList<>(call.getArguments());

        switch (name) {
            case "printf":
                if (!newArgs.isEmpty()) {
                    AstNode formatArg = newArgs.get(0);
                    if (formatArg instanceof Literal) {
                        String format = ((Literal) formatArg).getValue();
                        format = format.replace("%d", "%d")
                                       .replace("%s", "%s")
                                       .replace("%c", "%c")
                                       .replace("%f", "%f")
                                       .replace("%x", "%x");
                        newArgs.set(0, new Literal(format, Literal.LiteralType.STRING));
                    }
                }
                return new FunctionCall(new Identifier(javaName), newArgs);

            case "scanf":
                return new FunctionCall(new Identifier(javaName), newArgs);

            case "malloc":
                if (!newArgs.isEmpty() && newArgs.get(0) instanceof FunctionCall) {
                    FunctionCall arg = (FunctionCall) newArgs.get(0);
                    if ("sizeof".equals(arg.getName().getName()) && !arg.getArguments().isEmpty()) {
                        AstNode typeArg = arg.getArguments().get(0);
                        if (typeArg instanceof Identifier) {
                            String typeName = ((Identifier) typeArg).getName();
                            if (typeName.startsWith("struct ")) {
                                typeName = typeName.substring("struct ".length());
                            }
                            return new FunctionCall(new Identifier("new " + typeName), new ArrayList<>());
                        }
                    }
                }
                return new FunctionCall(new Identifier("new"), newArgs);

            case "free":
                return new FunctionCall(new Identifier("/* free removed */"), new ArrayList<>());

            case "strlen":
                return new FunctionCall(new Identifier(javaName), newArgs);

            case "strcpy":
                return new FunctionCall(new Identifier("copyValue"), newArgs);

            case "strcmp":
                return new FunctionCall(new Identifier(javaName), newArgs);

            case "puts":
            case "puts_s":
                return new FunctionCall(new Identifier(javaName), newArgs);

            case "gets":
            case "gets_s":
                return new FunctionCall(new Identifier(javaName), newArgs);

            case "atoi":
                return new FunctionCall(new Identifier(javaName), newArgs);

            case "atof":
                return new FunctionCall(new Identifier(javaName), newArgs);

            case "itoa":
                return new FunctionCall(new Identifier(javaName), newArgs);

            default:
                return new FunctionCall(new Identifier(javaName), newArgs);
        }
    }

    private static final java.util.Map<String, String> stdlibMap = new java.util.HashMap<>();

    static {
        stdlibMap.put("printf", "System.out.printf");
        stdlibMap.put("scanf", "ScannerInput.scan");
        stdlibMap.put("malloc", "new");
        stdlibMap.put("free", "/* free */");
        stdlibMap.put("calloc", "new");
        stdlibMap.put("realloc", "new");
        stdlibMap.put("strlen", "String.length");
        stdlibMap.put("strcpy", "String.copyValueOf");
        stdlibMap.put("strncpy", "String.copyValueOf");
        stdlibMap.put("strcmp", "String.compareTo");
        stdlibMap.put("strncmp", "String.compareTo");
        stdlibMap.put("strcat", "String.concat");
        stdlibMap.put("strncat", "String.concat");
        stdlibMap.put("strchr", "String.indexOf");
        stdlibMap.put("strstr", "String.indexOf");
        stdlibMap.put("puts", "System.out.println");
        stdlibMap.put("puts_s", "System.out.println");
        stdlibMap.put("gets", "ScannerInput.nextLine");
        stdlibMap.put("gets_s", "ScannerInput.nextLine");
        stdlibMap.put("fgets", "ScannerInput.nextLine");
        stdlibMap.put("fputs", "System.out.println");
        stdlibMap.put("atoi", "Integer.parseInt");
        stdlibMap.put("atof", "Double.parseDouble");
        stdlibMap.put("itoa", "Integer.toString");
        stdlibMap.put("ltoa", "Long.toString");
        stdlibMap.put("abs", "Math.abs");
        stdlibMap.put("fabs", "Math.abs");
        stdlibMap.put("sqrt", "Math.sqrt");
        stdlibMap.put("pow", "Math.pow");
        stdlibMap.put("sin", "Math.sin");
        stdlibMap.put("cos", "Math.cos");
        stdlibMap.put("tan", "Math.tan");
        stdlibMap.put("log", "Math.log");
        stdlibMap.put("log10", "Math.log10");
        stdlibMap.put("exp", "Math.exp");
        stdlibMap.put("ceil", "Math.ceil");
        stdlibMap.put("floor", "Math.floor");
        stdlibMap.put("round", "Math.round");
        stdlibMap.put("rand", "Math.random");
        stdlibMap.put("srand", "/* srand removed */");
        stdlibMap.put("time", "System.currentTimeMillis");
        stdlibMap.put("memcpy", "System.arraycopy");
        stdlibMap.put("memset", "Arrays.fill");
    }
}