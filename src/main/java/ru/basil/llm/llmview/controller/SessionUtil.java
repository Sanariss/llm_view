package ru.basil.llm.llmview.controller;

import jakarta.servlet.http.HttpSession;

/** Общая работа с сессией (флаг авторизации и ФИО пользователя). */
final class SessionUtil {

    static final String ATTR_AUTHED = "authedUser";
    static final String ATTR_FULL_NAME = "authedFullName";

    private SessionUtil() {
    }

    static boolean isAuthed(HttpSession session) {
        return session.getAttribute(ATTR_AUTHED) != null;
    }

    /**
     * @return true, если доступ разрешён; иначе редирект на страницу авторизации.
     */
    static String requireAuth(HttpSession session) {
        return isAuthed(session) ? null : "redirect:/auth";
    }
}
