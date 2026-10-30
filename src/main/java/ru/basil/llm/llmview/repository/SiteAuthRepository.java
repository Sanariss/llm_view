package ru.basil.llm.llmview.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.basil.llm.llmview.entity.SiteAuth;

import java.util.Optional;

/** Репозиторий таблицы siteauth (in-memory база). */
public interface SiteAuthRepository extends JpaRepository<SiteAuth, Long> {

    Optional<SiteAuth> findByLogin(String login);

    boolean existsByLogin(String login);
}
