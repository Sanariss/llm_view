package ru.basil.llm.llmview.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Таблица expense_category — категории трат (строки таблицы на странице main).
 */
@Entity
@Table(name = "expense_category")
public class ExpenseCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Название категории, например «Продукты», «Кофе». */
    @Column(name = "name", nullable = false, unique = true, length = 200)
    private String name;

    /** Порядок вывода в таблице (как в исходном Excel). */
    @Column(name = "sort_order")
    private Integer sortOrder;

    public ExpenseCategory() {
    }

    public ExpenseCategory(String name, Integer sortOrder) {
        this.name = name;
        this.sortOrder = sortOrder;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
