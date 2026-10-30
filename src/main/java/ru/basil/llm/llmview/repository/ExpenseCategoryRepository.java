package ru.basil.llm.llmview.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.basil.llm.llmview.entity.ExpenseCategory;

import java.util.List;

/** Репозиторий категорий трат. */
public interface ExpenseCategoryRepository extends JpaRepository<ExpenseCategory, Long> {

    List<ExpenseCategory> findAllByOrderBySortOrderAscIdAsc();

    boolean existsByNameIgnoreCase(String name);
}
