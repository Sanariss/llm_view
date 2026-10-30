package ru.basil.llm.llmview.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.basil.llm.llmview.entity.SiteAuth;
import ru.basil.llm.llmview.repository.SiteAuthRepository;

import java.util.Optional;

/**
 * Сервис регистрации и авторизации. Данные читаются/пишутся
 * в in-memory таблицу siteauth.
 */
@Service
public class AuthService {

    private final SiteAuthRepository siteAuthRepository;

    public AuthService(SiteAuthRepository siteAuthRepository) {
        this.siteAuthRepository = siteAuthRepository;
    }

    /**
     * Регистрирует пользователя.
     *
     * @return текст ошибки или null, если регистрация успешна
     */
    @Transactional
    public String register(String fullName, String login, String password) {
        if (siteAuthRepository.existsByLogin(login.trim())) {
            return "Пользователь с логином «" + login.trim() + "» уже существует";
        }
        siteAuthRepository.save(new SiteAuth(fullName.trim(), login.trim(), password));
        return null;
    }

    /** Проверяет логин/пароль по таблице siteauth. */
    public Optional<SiteAuth> authenticate(String login, String password) {
        return siteAuthRepository.findByLogin(login == null ? "" : login.trim())
                .filter(u -> u.getPassword().equals(password));
    }
}
