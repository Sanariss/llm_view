package ru.basil.llm.llmview.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.basil.llm.llmview.entity.ExpenseYear;

import java.util.List;

/** Репозиторий лет, добавленных пользователем. */
public interface ExpenseYearRepository extends JpaRepository<ExpenseYear, Long> {

    boolean existsByYear(int year);

    ExpenseYear findByYear(int year);

    List<ExpenseYear> findAllByOrderByYearDesc();
}
