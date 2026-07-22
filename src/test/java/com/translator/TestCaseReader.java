package com.translator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class TestCaseReader {
    private final String testCasesDir;

    public TestCaseReader() {
        this.testCasesDir = "src/test/resources/testcases";
    }

    public TestCaseReader(String testCasesDir) {
        this.testCasesDir = testCasesDir;
    }

    public List<TestCase> readAllTestCases() throws IOException {
        List<TestCase> testCases = new ArrayList<>();
        File dir = new File(testCasesDir);
        
        if (!dir.exists() || !dir.isDirectory()) {
            throw new IOException("Test cases directory not found: " + testCasesDir);
        }
        
        File[] files = dir.listFiles((d, name) -> name.endsWith(".c"));
        if (files == null) {
            return testCases;
        }
        
        for (File cFile : files) {
            String baseName = cFile.getName().replace(".c", "");
            File expectedFile = new File(testCasesDir, baseName + ".expected.java");
            
            String cCode = readFile(cFile);
            String expectedJava = expectedFile.exists() ? readFile(expectedFile) : null;
            
            TestCase testCase = new TestCase(baseName, cCode, expectedJava);
            testCases.add(testCase);
        }
        
        return testCases;
    }

    private String readFile(File file) throws IOException {
        byte[] bytes = new byte[(int) file.length()];
        try (FileInputStream fis = new FileInputStream(file)) {
            fis.read(bytes);
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static class TestCase {
        private final String name;
        private final String cCode;
        private final String expectedJava;

        public TestCase(String name, String cCode, String expectedJava) {
            this.name = name;
            this.cCode = cCode;
            this.expectedJava = expectedJava;
        }

        public String getName() {
            return name;
        }

        public String getCCode() {
            return cCode;
        }

        public String getExpectedJava() {
            return expectedJava;
        }

        public boolean hasExpectedOutput() {
            return expectedJava != null && !expectedJava.isEmpty();
        }
    }
}