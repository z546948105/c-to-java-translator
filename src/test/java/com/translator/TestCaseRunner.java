package com.translator;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestCaseRunner {

    @Test
    void runAllTestCases() throws IOException {
        TestCaseReader reader = new TestCaseReader();
        List<TestCaseReader.TestCase> testCases = reader.readAllTestCases();
        
        assertNotNull(testCases, "Test cases should not be null");
        assertTrue(testCases.size() > 0, "At least one test case should exist");
        
        System.out.println("=== Running " + testCases.size() + " test cases ===");
        
        for (TestCaseReader.TestCase testCase : testCases) {
            System.out.println("\n--- Test Case: " + testCase.getName() + " ---");
            
            String cCode = testCase.getCCode();
            String expectedJava = testCase.getExpectedJava();
            
            System.out.println("Input C code:");
            System.out.println(cCode);
            
            Translator translator = new Translator(cCode);
            String actualJava = translator.translate();
            
            System.out.println("\nGenerated Java code:");
            System.out.println(actualJava);
            
            if (testCase.hasExpectedOutput()) {
                System.out.println("\nExpected Java code:");
                System.out.println(expectedJava);
                
                String actualTrimmed = actualJava.replaceAll("\\s+", "");
                String expectedTrimmed = expectedJava.replaceAll("\\s+", "");
                
                assertEquals(expectedTrimmed, actualTrimmed, 
                    "Generated Java code does not match expected output for test case: " + testCase.getName());
                System.out.println("\n✅ Test PASSED");
            } else {
                System.out.println("\n⚠️ No expected output, skipping comparison");
            }
        }
        
        System.out.println("\n=== All test cases completed ===");
    }
}