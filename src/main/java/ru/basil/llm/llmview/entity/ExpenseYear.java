package ru.basil.llm.llmview.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Таблица expense_year — годы, доступные для ведения трат.
 * Пользователь может добавлять и удалять годы на странице /main.
 */
@Entity
@Table(name = "expense_year")
public class ExpenseYear {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Номер года, например 2026. Уникален. */
    @Column(name = "year_num", nullable = false, unique = true)
    private int year;

    public ExpenseYear() {
    }

    public ExpenseYear(int year) {
        this.year = year;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
}
