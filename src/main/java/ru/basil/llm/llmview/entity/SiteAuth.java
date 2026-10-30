package ru.basil.llm.llmview.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Таблица siteauth — пользователи приложения (регистрация/авторизация).
 * Хранится в in-memory базе H2.
 */
@Entity
@Table(name = "siteauth")
public class SiteAuth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ФИО пользователя (обязательное поле). */
    @Column(name = "full_name", nullable = false, length = 200)
    private String fullName;

    /** Логин (обязательное поле, уникальный). */
    @Column(name = "login", nullable = false, unique = true, length = 100)
    private String login;

    /** Пароль (обязательное поле). */
    @Column(name = "password", nullable = false, length = 100)
    private String password;

    public SiteAuth() {
    }

    public SiteAuth(String fullName, String login, String password) {
        this.fullName = fullName;
        this.login = login;
        this.password = password;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
