package ru.basil.llm.llmview.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.basil.llm.llmview.entity.ExpenseAmount;

import java.util.List;
import java.util.Optional;

/** Репозиторий сумм трат по категориям за месяц/год. */
public interface ExpenseAmountRepository extends JpaRepository<ExpenseAmount, Long> {

    Optional<ExpenseAmount> findByCategoryIdAndYearAndMonth(Long categoryId, int year, int month);

    List<ExpenseAmount> findByYear(int year);

    void deleteByCategoryId(Long categoryId);
}
