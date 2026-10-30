package ru.basil.llm.llmview.form;

import jakarta.validation.constraints.NotBlank;

/** Форма регистрации (ФИО, логин, пароль — все поля обязательны). */
public class RegisterForm {

    @NotBlank(message = "ФИО обязательно для заполнения")
    private String fullName;

    @NotBlank(message = "Логин обязателен для заполнения")
    private String login;

    @NotBlank(message = "Пароль обязателен для заполнения")
    private String password;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
