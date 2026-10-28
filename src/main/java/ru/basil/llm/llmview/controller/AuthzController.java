package ru.basil.llm.llmview.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Контроллер страницы авторизации /authz.
 * Обработка POST-запроса входа выполняется Spring Security
 * (loginProcessingUrl = /authz, см. SecurityConfig).
 */
@Controller
public class AuthzController {

    /**
     * Открывает страницу авторизации.
     * Если в параметрах есть flag ошибки (?error) — на странице
     * показывается всплывающее окно «авторизация не удалась».
     */
    @GetMapping("/authz")
    public String authz(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        Model model) {
        if (error != null) {
            model.addAttribute("authFailed", true);
        }
        if (logout != null) {
            model.addAttribute("loggedOut", true);
        }
        return "authz";
    }
}
