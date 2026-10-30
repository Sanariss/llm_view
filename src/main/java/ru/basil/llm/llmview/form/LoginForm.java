package ru.basil.llm.llmview.form;

import jakarta.validation.constraints.NotBlank;

/** Форма авторизации (логин + пароль проверяются по таблице siteauth). */
public class LoginForm {

    @NotBlank(message = "Логин обязателен для заполнения")
    private String login;

    @NotBlank(message = "Пароль обязателен для заполнения")
    private String password;

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
