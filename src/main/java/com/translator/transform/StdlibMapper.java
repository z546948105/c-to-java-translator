package com.translator.transform;

import com.translator.ast.AstNode;
import com.translator.ast.Assignment;
import com.translator.ast.BinaryExpression;
import com.translator.ast.FunctionCall;
import com.translator.ast.Identifier;
import com.translator.ast.Literal;

import java.util.ArrayList;
import java.util.List;

/**
 * 标准库函数映射器（StdlibMapper）
 * <p>
 * 将 C 标准库函数调用映射为 Java 标准库调用
 * <p>
 * 主要映射：
 * - printf → System.out.printf
 * - malloc → new
 * - free → 移除（Java 自动垃圾回收）
 * - strlen → String.length()
 * - strcpy → dst = src
 * - strcat → dst = dst + src
 * - strcmp → String.compareTo()
 * - abs → Math.abs
 * - sqrt → Math.sqrt
 * - 文件 I/O：fopen/fclose/fread/fwrite/fprintf/fscanf/fgets/fputs/fseek/ftell/rewind/feof/ferror
 */
public class StdlibMapper {
    public static boolean isStdlibFunction(String name) {
        return stdlibMap.containsKey(name);
    }

    public static String mapFunctionName(String name) {
        return stdlibMap.getOrDefault(name, name);
    }

    public static AstNode mapFunctionCall(FunctionCall call) {
        String name = call.getName().getName();
        if (!stdlibMap.containsKey(name)) {
            return call;
        }

        List<AstNode> newArgs = new ArrayList<>(call.getArguments());

        switch (name) {
            case "printf":
                if (!newArgs.isEmpty() && newArgs.get(0) instanceof Literal) {
                    String format = ((Literal) newArgs.get(0)).getValue();
                    format = format.replace("%d", "%d")
                                   .replace("%s", "%s")
                                   .replace("%c", "%c")
                                   .replace("%f", "%f")
                                   .replace("%x", "%x");
                    newArgs.set(0, new Literal(format, Literal.LiteralType.STRING));
                }
                return new FunctionCall(new Identifier("System.out.printf"), newArgs);

            case "scanf":
                return new FunctionCall(new Identifier("ScannerInput.scan"), newArgs);

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
                if (!newArgs.isEmpty()) {
                    AstNode arg = newArgs.get(0);
                    return new FunctionCall(new Identifier(arg.toString() + ".length"), new ArrayList<>());
                }
                return call;

            case "strcpy":
                if (newArgs.size() >= 2) {
                    return new Assignment(newArgs.get(0), newArgs.get(1));
                }
                return call;

            case "strncpy":
                if (newArgs.size() >= 3) {
                    FunctionCall substringCall = new FunctionCall(
                        new Identifier(newArgs.get(1).toString() + ".substring"),
                        java.util.Arrays.asList(new Literal("0", Literal.LiteralType.INTEGER), newArgs.get(2))
                    );
                    return new Assignment(newArgs.get(0), substringCall);
                }
                return call;

            case "strcmp":
                if (newArgs.size() >= 2) {
                    return new FunctionCall(
                        new Identifier(newArgs.get(0).toString() + ".compareTo"),
                        java.util.Arrays.asList(newArgs.get(1))
                    );
                }
                return call;

            case "strncmp":
                if (newArgs.size() >= 3) {
                    FunctionCall substringCall = new FunctionCall(
                        new Identifier(newArgs.get(0).toString() + ".substring"),
                        java.util.Arrays.asList(new Literal("0", Literal.LiteralType.INTEGER), newArgs.get(2))
                    );
                    return new FunctionCall(
                        new Identifier(substringCall.toString() + ".compareTo"),
                        java.util.Arrays.asList(newArgs.get(1))
                    );
                }
                return call;

            case "strcat":
                if (newArgs.size() >= 2) {
                    return new Assignment(
                        newArgs.get(0),
                        new BinaryExpression(newArgs.get(0), "+", newArgs.get(1))
                    );
                }
                return call;

            case "strncat":
                if (newArgs.size() >= 3) {
                    FunctionCall substringCall = new FunctionCall(
                        new Identifier(newArgs.get(1).toString() + ".substring"),
                        java.util.Arrays.asList(new Literal("0", Literal.LiteralType.INTEGER), newArgs.get(2))
                    );
                    return new Assignment(
                        newArgs.get(0),
                        new BinaryExpression(newArgs.get(0), "+", substringCall)
                    );
                }
                return call;

            case "strchr":
                if (newArgs.size() >= 2) {
                    return new FunctionCall(
                        new Identifier(newArgs.get(0).toString() + ".indexOf"),
                        java.util.Arrays.asList(newArgs.get(1))
                    );
                }
                return call;

            case "strstr":
                if (newArgs.size() >= 2) {
                    return new FunctionCall(
                        new Identifier(newArgs.get(0).toString() + ".indexOf"),
                        java.util.Arrays.asList(newArgs.get(1))
                    );
                }
                return call;

            case "puts":
            case "puts_s":
                return new FunctionCall(new Identifier("System.out.println"), newArgs);

            case "gets":
            case "gets_s":
                return new FunctionCall(new Identifier("ScannerInput.nextLine"), newArgs);

            case "atoi":
                return new FunctionCall(new Identifier("Integer.parseInt"), newArgs);

            case "atof":
                return new FunctionCall(new Identifier("Double.parseDouble"), newArgs);

            case "itoa":
                return new FunctionCall(new Identifier("Integer.toString"), newArgs);

            case "fopen":
                if (newArgs.size() >= 2 && newArgs.get(1) instanceof Literal) {
                    String mode = ((Literal) newArgs.get(1)).getValue();
                    String javaClass = mapFopenMode(mode);
                    List<AstNode> fopenArgs = new ArrayList<>();
                    fopenArgs.add(newArgs.get(0));
                    return new FunctionCall(new Identifier("new " + javaClass), fopenArgs);
                }
                return new FunctionCall(new Identifier("new FileInputStream"), newArgs.subList(0, 1));

            case "fclose":
                if (!newArgs.isEmpty()) {
                    return new FunctionCall(new Identifier(newArgs.get(0).toString() + ".close"), new ArrayList<>());
                }
                return call;

            case "fread":
                if (newArgs.size() >= 4) {
                    AstNode streamArg = newArgs.get(3);
                    return new FunctionCall(new Identifier(streamArg.toString() + ".read"), 
                        java.util.Arrays.asList(newArgs.get(0)));
                }
                return call;

            case "fwrite":
                if (newArgs.size() >= 4) {
                    AstNode streamArg = newArgs.get(3);
                    return new FunctionCall(new Identifier(streamArg.toString() + ".write"), 
                        java.util.Arrays.asList(newArgs.get(0)));
                }
                return call;

            case "fprintf":
                if (newArgs.size() >= 1) {
                    AstNode streamArg = newArgs.get(0);
                    List<AstNode> fmtArgs = new ArrayList<>(newArgs.subList(1, newArgs.size()));
                    if (!fmtArgs.isEmpty() && fmtArgs.get(0) instanceof Literal) {
                        String format = ((Literal) fmtArgs.get(0)).getValue();
                        format = format.replace("%d", "%d")
                                       .replace("%s", "%s")
                                       .replace("%c", "%c")
                                       .replace("%f", "%f")
                                       .replace("%x", "%x");
                        fmtArgs.set(0, new Literal(format, Literal.LiteralType.STRING));
                    }
                    return new FunctionCall(new Identifier(streamArg.toString() + ".printf"), fmtArgs);
                }
                return call;

            case "fgets":
                if (newArgs.size() >= 1) {
                    AstNode streamArg = newArgs.get(newArgs.size() - 1);
                    return new FunctionCall(new Identifier(streamArg.toString() + ".readLine"), new ArrayList<>());
                }
                return call;

            case "fputs":
                if (newArgs.size() >= 2) {
                    AstNode streamArg = newArgs.get(1);
                    return new FunctionCall(new Identifier(streamArg.toString() + ".println"), 
                        java.util.Arrays.asList(newArgs.get(0)));
                }
                return call;

            case "fseek":
                if (newArgs.size() >= 3) {
                    AstNode streamArg = newArgs.get(0);
                    AstNode offsetArg = newArgs.get(1);
                    return new FunctionCall(new Identifier(streamArg.toString() + ".skip"), 
                        java.util.Arrays.asList(offsetArg));
                }
                return call;

            case "ftell":
                if (!newArgs.isEmpty()) {
                    AstNode streamArg = newArgs.get(0);
                    return new FunctionCall(new Identifier(streamArg.toString() + ".available"), new ArrayList<>());
                }
                return call;

            case "rewind":
                if (!newArgs.isEmpty()) {
                    AstNode streamArg = newArgs.get(0);
                    return new FunctionCall(new Identifier(streamArg.toString() + ".reset"), new ArrayList<>());
                }
                return call;

            case "feof":
                if (!newArgs.isEmpty()) {
                    AstNode streamArg = newArgs.get(0);
                    return new FunctionCall(new Identifier(streamArg.toString() + ".isEOF"), new ArrayList<>());
                }
                return call;

            case "ferror":
                if (!newArgs.isEmpty()) {
                    AstNode streamArg = newArgs.get(0);
                    return new FunctionCall(new Identifier(streamArg.toString() + ".checkError"), new ArrayList<>());
                }
                return call;

            default:
                String javaName = stdlibMap.get(name);
                return new FunctionCall(new Identifier(javaName), newArgs);
        }
    }

    private static String mapFopenMode(String mode) {
        if (mode.contains("w")) {
            if (mode.contains("b")) {
                return "java.io.FileOutputStream";
            }
            return "java.io.FileWriter";
        } else if (mode.contains("r")) {
            if (mode.contains("b")) {
                return "java.io.FileInputStream";
            }
            return "java.io.FileReader";
        } else if (mode.contains("a")) {
            if (mode.contains("b")) {
                return "java.io.FileOutputStream";
            }
            return "java.io.FileWriter";
        }
        return "java.io.FileInputStream";
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

        stdlibMap.put("fopen", "FileIO.open");
        stdlibMap.put("fclose", "close");
        stdlibMap.put("fread", "read");
        stdlibMap.put("fwrite", "write");
        stdlibMap.put("fprintf", "printf");
        stdlibMap.put("fscanf", "read");
        stdlibMap.put("fgets", "readLine");
        stdlibMap.put("fputs", "println");
        stdlibMap.put("fseek", "skip");
        stdlibMap.put("ftell", "getFilePointer");
        stdlibMap.put("rewind", "seekToBegin");
        stdlibMap.put("feof", "isEOF");
        stdlibMap.put("ferror", "checkError");
    }
}
