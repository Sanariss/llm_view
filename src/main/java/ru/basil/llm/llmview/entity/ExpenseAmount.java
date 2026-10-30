package ru.basil.llm.llmview.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;

/**
 * Таблица expense_amount — сумма трат по категории за конкретный месяц конкретного года.
 */
@Entity
@Table(name = "expense_amount",
        uniqueConstraints = @UniqueConstraint(columnNames = {"category_id", "year_num", "month_num"}))
public class ExpenseAmount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "category_id")
    private ExpenseCategory category;

    /** Год, например 2026. */
    @Column(name = "year_num", nullable = false)
    private int year;

    /** Номер месяца от 1 (январь) до 12 (декабрь). */
    @Column(name = "month_num", nullable = false)
    private int month;

    /** Накопленная сумма трат. */
    @Column(name = "amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;

    public ExpenseAmount() {
    }

    public ExpenseAmount(ExpenseCategory category, int year, int month, BigDecimal amount) {
        this.category = category;
        this.year = year;
        this.month = month;
        this.amount = amount;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ExpenseCategory getCategory() { return category; }
    public void setCategory(ExpenseCategory category) { this.category = category; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
