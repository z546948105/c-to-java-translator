package com.translator.controller;

import com.translator.Translator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 翻译控制器（TranslatorController）
 * <p>
 * 提供 REST API 接口，用于 C 代码到 Java 代码的翻译
 * <p>
 * 主要接口：
 * - GET /api/ - 健康检查和接口说明
 * - POST /api/translate - 翻译 C 代码为 Java 代码
 * - POST /api/translate/file - 翻译文件（同 translate）
 * - GET /api/health - 服务健康状态检查
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class TranslatorController {

    private static final Logger log = LoggerFactory.getLogger(TranslatorController.class);

    @GetMapping("/")
    public ResponseEntity<String> index() {
        log.info("API index endpoint accessed");
        return ResponseEntity.ok("C to Java Translator API");
    }

    @PostMapping("/translate")
    public ResponseEntity<Map<String, Object>> translate(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String cCode = request.get("code");
            String className = request.getOrDefault("className", "TranslatedCode");
            
            log.info("Received translation request for class: {}, code length: {}", className, cCode != null ? cCode.length() : 0);
            
            if (cCode == null || cCode.isEmpty()) {
                log.warn("Translation request with empty code");
                response.put("success", false);
                response.put("error", "代码不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            
            String javaCode = Translator.translateCode(cCode, className);
            
            log.info("Translation successful, output length: {}", javaCode != null ? javaCode.length() : 0);
            
            response.put("success", true);
            response.put("javaCode", javaCode);
            response.put("className", className);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Translation failed: {}", e.getMessage(), e);
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