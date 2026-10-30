package ru.basil.llm.llmview.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.basil.llm.llmview.form.RegisterForm;
import ru.basil.llm.llmview.service.AuthService;

/**
 * Страница регистрации /reg. Данные сохраняются в in-memory таблицу siteauth.
 * При успешной регистрации — переход на страницу авторизации /auth.
 */
@Controller
public class RegController {

    private final AuthService authService;

    public RegController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/reg")
    public String regForm(Model model) {
        if (!model.containsAttribute("registerForm")) {
            model.addAttribute("registerForm", new RegisterForm());
        }
        return "reg";
    }

    @PostMapping("/reg")
    public String regSubmit(@Valid @ModelAttribute("registerForm") RegisterForm form,
                            BindingResult bindingResult,
                            Model model) {
        // Ошибки валидации — остаёмся на странице регистрации
        if (bindingResult.hasErrors()) {
            return "reg";
        }
        String error = authService.register(form.getFullName(), form.getLogin(), form.getPassword());
        if (error != null) {
            model.addAttribute("regError", error);
            return "reg";
        }
        // Успешная регистрация — на страницу авторизации
        return "redirect:/auth?registered";
    }
}
