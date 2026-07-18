package com.translator.controller;

import com.translator.Translator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class TranslatorController {

    @GetMapping("/")
    public ResponseEntity<String> index() {
        return ResponseEntity.ok("C to Java Translator API");
    }

    @PostMapping("/translate")
    public ResponseEntity<Map<String, Object>> translate(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String cCode = request.get("code");
            String className = request.getOrDefault("className", "TranslatedCode");
            
            if (cCode == null || cCode.isEmpty()) {
                response.put("success", false);
                response.put("error", "代码不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            
            String javaCode = Translator.translateCode(cCode, className);
            
            response.put("success", true);
            response.put("javaCode", javaCode);
            response.put("className", className);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/translate/file")
    public ResponseEntity<Map<String, Object>> translateFile(@RequestBody Map<String, String> request) {
        return translate(request);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "C to Java Translator");
        return ResponseEntity.ok(response);
    }
}