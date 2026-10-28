package ru.basil.llm.llmview.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Контроллер страницы /index.
 * Формирует данные (Hello World) и отдаёт HTML-шаблон Thymeleaf.
 */
@Controller
public class IndexController {

    @GetMapping("/index")
    public String index(Model model) {
        // Текст "Hello World" передаётся в модель и подставляется в шаблон Thymeleaf
        model.addAttribute("message", helloWorld());
        return "index";
    }

    /**
     * Текст страницы, возвращаемый контроллером.
     */
    static String helloWorld() {
        return "Hello World";
    }
}
