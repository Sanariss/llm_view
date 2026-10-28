package ru.basil.llm.llmview.controller;

import jakarta.servlet.http.HttpSession;
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
    public String index(Model model, HttpSession session) {
        // После запуска приложение открывается на странице авторизации /authz;
        // без успешного входа на /index редиректим обратно на /authz
        Object authenticated = session.getAttribute("authz.authenticated");
        if (!Boolean.TRUE.equals(authenticated)) {
            return "redirect:/authz";
        }
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
