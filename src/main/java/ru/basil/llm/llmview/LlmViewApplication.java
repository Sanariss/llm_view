package ru.basil.llm.llmview;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import java.awt.Desktop;
import java.net.URI;

@SpringBootApplication
public class LlmViewApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(LlmViewApplication.class, args);

        // После запуска приложения автоматически открываем страницу авторизации /authz
        openAuthzPage(context.getEnvironment());
    }

    private static void openAuthzPage(Environment env) {
        String port = env.getProperty("server.port", "8080");
        String url = "http://localhost:" + port + "/authz";
        try {
            if (Desktop.isDesktopSupported()
                    && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                System.out.println("Откройте страницу авторизации в браузере: " + url);
            }
        } catch (Exception e) {
            System.out.println("Не удалось открыть браузер автоматически. Откройте вручную: " + url);
        }
    }
}
