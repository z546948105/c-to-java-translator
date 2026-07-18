package com.translator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * C 到 Java 翻译器应用入口
 * <p>
 * Spring Boot 应用启动类，启动翻译服务
 */
@SpringBootApplication
public class TranslatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(TranslatorApplication.class, args);
    }
}