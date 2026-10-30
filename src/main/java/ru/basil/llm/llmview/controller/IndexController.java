package ru.basil.llm.llmview.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Главная страница /index — меню с кнопками «Регистрация» и «Авторизация».
 */
@Controller
public class IndexController {

    @GetMapping({"/", "/index"})
    public String index(HttpSession session) {
        // Если пользователь уже вошёл — сразу на страницу трат
        if (SessionUtil.isAuthed(session)) {
            return "redirect:/main";
        }
        return "index";
    }
}
