package ru.basil.llm.llmview;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class LlmViewApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(LlmViewApplication.class, args);
        // После запуска приложения сразу открываем страницу авторизации /authz в браузере
        openAuthzPage(context.getEnvironment());
    }

    private static void openAuthzPage(Environment env) {
        try {
            String port = env.getProperty("server.port", "8080");
            java.awt.Desktop.getDesktop()
                    .browse(java.net.URI.create("http://localhost:" + port + "/authz"));
        } catch (Throwable ignored) {
            // Если браузер открыть нельзя (нет GUI/десктоп-окружения) — просто печатаем адрес
            System.out.println("Откройте страницу авторизации: http://localhost:"
                    + env.getProperty("server.port", "8080") + "/authz");
        }
    }
}
