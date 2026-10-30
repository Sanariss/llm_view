package ru.basil.llm.llmview;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

/**
 * Приложение «Контроль трат».
 * Java 17 + Spring Boot, UI на Thymeleaf, in-memory база H2 (таблицы
 * siteauth / expense_category / expense_amount).
 */
@SpringBootApplication
public class LlmViewApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(LlmViewApplication.class, args);
        openStartPage(context.getEnvironment());
    }

    /** После запуска открываем главную страницу приложения. */
    private static void openStartPage(Environment env) {
        String port = env.getProperty("server.port", "8080");
        String url = "http://localhost:" + port + "/";
        try {
            if (java.awt.Desktop.isDesktopSupported()
                    && java.awt.Desktop.getDesktop().isSupported(java.awt.Desktop.Action.BROWSE)) {
                java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
            } else {
                System.out.println("Откройте приложение в браузере: " + url);
            }
        } catch (Exception e) {
            System.out.println("Не удалось открыть браузер автоматически. Откройте вручную: " + url);
        }
    }
}
