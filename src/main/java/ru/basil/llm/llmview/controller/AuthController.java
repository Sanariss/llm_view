package ru.basil.llm.llmview.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.basil.llm.llmview.form.LoginForm;
import ru.basil.llm.llmview.service.AuthService;

/**
 * Страница авторизации /auth. Логин и пароль проверяются по in-memory
 * таблице siteauth. При успехе — страница трат /main.
 */
@Controller
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/auth")
    public String authForm(@RequestParam(value = "registered", required = false) String registered,
                           Model model) {
        if (registered != null) {
            model.addAttribute("registeredOk", true);
        }
        if (!model.containsAttribute("loginForm")) {
            model.addAttribute("loginForm", new LoginForm());
        }
        return "auth";
    }

    @PostMapping("/auth")
    public String authSubmit(@Valid @ModelAttribute("loginForm") LoginForm form,
                             BindingResult bindingResult,
                             HttpSession session,
                             Model model) {
        if (bindingResult.hasErrors()) {
            return "auth";
        }
        var user = authService.authenticate(form.getLogin(), form.getPassword());
        if (user.isEmpty()) {
            model.addAttribute("authError", "Неверный логин или пароль");
            return "auth";
        }
        session.setAttribute(SessionUtil.ATTR_AUTHED, user.get().getLogin());
        session.setAttribute(SessionUtil.ATTR_FULL_NAME, user.get().getFullName());
        return "redirect:/main";
    }

    /** Выход — завершает сессию и возвращает на страницу авторизации. */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/auth?logout";
    }
}
