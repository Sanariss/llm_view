package ru.basil.llm.llmview.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Контроллер главной страницы /main.
 * Открывается после успешной авторизации, доступен только авторизованным.
 */
@Controller
public class MainController {

    @GetMapping("/main")
    public String main() {
        return "main";
    }
}
