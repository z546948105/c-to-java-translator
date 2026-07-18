package com.translator;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            printUsage();
            return;
        }

        String option = args[0];

        switch (option) {
            case "-f":
                if (args.length >= 2) {
                    translateFile(args[1], args.length >= 3 ? args[2] : null);
                } else {
                    System.err.println("Error: Please provide input file path");
                    printUsage();
                }
                break;

            case "-s":
                if (args.length >= 2) {
                    String code = String.join(" ", args).substring(3);
                    System.out.println(Translator.translateCode(code));
                } else {
                    System.err.println("Error: Please provide C code");
                    printUsage();
                }
                break;

            case "-i":
                interactiveMode();
                break;

            default:
                System.err.println("Unknown option: " + option);
                printUsage();
        }
    }

    private static void printUsage() {
        System.out.println("C to Java Translator - Usage:");
        System.out.println("  -f <input_file> [output_file]  Translate C source file to Java");
        System.out.println("  -s <c_code>                     Translate C code string directly");
        System.out.println("  -i                              Interactive mode");
        System.out.println();
        System.out.println("Example:");
        System.out.println("  java -jar c-to-java-translator.jar -f input.c output.java");
        System.out.println("  java -jar c-to-java-translator.jar -s \"int add(int a, int b) { return a + b; }\"");
    }

    private static void translateFile(String inputPath, String outputPath) {
        try {
            String cCode = new String(Files.readAllBytes(Paths.get(inputPath)));
            String javaCode = Translator.translateCode(cCode);

            if (outputPath != null) {
                Files.write(Paths.get(outputPath), javaCode.getBytes());
                System.out.println("Successfully translated to: " + outputPath);
            } else {
                System.out.println(javaCode);
            }
        } catch (IOException e) {
            System.err.println("Error reading/writing file: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Translation error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void interactiveMode() {
        System.out.println("C to Java Translator - Interactive Mode");
        System.out.println("Enter C code (type 'exit' or 'quit' to exit):");
        System.out.println("------------------------------------------");

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder input = new StringBuilder();

        try {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.equalsIgnoreCase("exit") || line.equalsIgnoreCase("quit")) {
                    break;
                }
                input.append(line).append("\n");
            }

            if (input.length() > 0) {
                String javaCode = Translator.translateCode(input.toString());
                System.out.println("\n------------------------------------------");
                System.out.println("Generated Java code:");
                System.out.println("------------------------------------------");
                System.out.println(javaCode);
            }
        } catch (IOException e) {
            System.err.println("Error reading input: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Translation error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}