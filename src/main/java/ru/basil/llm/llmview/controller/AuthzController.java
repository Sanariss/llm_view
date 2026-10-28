package ru.basil.llm.llmview.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Контроллер страницы авторизации /authz.
 * Логин и пароль берутся из application.properties (authz.login / authz.pwd).
 */
@Controller
public class AuthzController {

    @Value("${authz.login}")
    private String expectedLogin;

    @Value("${authz.pwd}")
    private String expectedPwd;

    /**
     * Открывает страницу авторизации.
     */
    @GetMapping("/authz")
    public String authz() {
        return "authz";
    }

    /**
     * Проверка введённых логина и пароля:
     * - если значения совпадают с authz.login / authz.pwd — редирект на /index;
     * - если не совпадают — возврат на страницу authz с флагом ошибки,
     *   по которому на странице показывается всплывающее окно
     *   «авторизация не удалась».
     */
    @PostMapping("/authz")
    public String login(@RequestParam("login") String login,
                        @RequestParam("pwd") String pwd,
                        Model model,
                        HttpSession session) {
        if (expectedLogin.equals(login) && expectedPwd.equals(pwd)) {
            session.setAttribute("authz.authenticated", Boolean.TRUE);
            return "redirect:/index";
        }
        model.addAttribute("authFailed", true);
        return "authz";
    }
}
