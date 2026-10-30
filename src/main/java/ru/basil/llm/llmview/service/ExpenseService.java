package ru.basil.llm.llmview.service;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.basil.llm.llmview.entity.ExpenseAmount;
import ru.basil.llm.llmview.entity.ExpenseCategory;
import ru.basil.llm.llmview.repository.ExpenseAmountRepository;
import ru.basil.llm.llmview.repository.ExpenseCategoryRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Сервис трат: категории, суммы по месяцам, удаление категорий, обнуление сумм.
 * Все данные хранятся в in-memory базе (таблицы expense_category и expense_amount).
 */
@Service
public class ExpenseService {

    /** Названия 12 месяцев для шапки таблицы (как в salary.xlsx). */
    public static final String[] MONTH_NAMES = {
            "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
            "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"
    };

    private final ExpenseCategoryRepository categoryRepository;
    private final ExpenseAmountRepository amountRepository;

    public ExpenseService(ExpenseCategoryRepository categoryRepository,
                          ExpenseAmountRepository amountRepository) {
        this.categoryRepository = categoryRepository;
        this.amountRepository = amountRepository;
    }

    /**
     * Стартовые категории — как строки расхода в листе «New Траты» файла salary.xlsx.
     * Выполняется один раз при старте приложения (in-memory база пуста).
     */
    @PostConstruct
    @Transactional
    public void initDefaultCategories() {
        if (categoryRepository.count() > 0) {
            return;
        }
        List<String> defaults = List.of(
                "ТГК-1 Декабристов",
                "ТГК-1 Пражская",
                "Комм + Электричество Декабристов и Пражская",
                "Телефон Пражская + Декабристов",
                "Газ Декабристов",
                "Мобильный телефон",
                "Интернет",
                "Всё что не подходит в другие категории",
                "Билеты (РЖД/Самолеты)",
                "Подарки родным/друзьям",
                "Продукты",
                "Траты на дачу",
                "Такси",
                "Кофе",
                "Массаж",
                "Медицина",
                "Одежда",
                "Рестораны",
                "Машина (ремонт + бенз + парковка + мойка + штрафы)",
                "Театры и Музеи + Кино + Баня + отели + стрижка"
        );
        int order = 0;
        for (String name : defaults) {
            categoryRepository.save(new ExpenseCategory(name, ++order));
        }
    }

    public List<ExpenseCategory> getCategories() {
        return categoryRepository.findAllByOrderBySortOrderAscIdAsc();
    }

    /** Добавляет категорию трат. Возвращает текст ошибки или null при успехе. */
    @Transactional
    public String addCategory(String name) {
        if (name == null || name.isBlank()) {
            return "Название категории не может быть пустым";
        }
        String trimmed = name.trim();
        if (categoryRepository.existsByNameIgnoreCase(trimmed)) {
            return "Категория «" + trimmed + "» уже существует";
        }
        int nextOrder = categoryRepository.findAllByOrderBySortOrderAscIdAsc().size() + 1;
        categoryRepository.save(new ExpenseCategory(trimmed, nextOrder));
        return null;
    }

    /** Удаляет категорию вместе со всеми её суммами по всем месяцам. */
    @Transactional
    public void deleteCategory(Long categoryId) {
        amountRepository.deleteByCategoryId(categoryId);
        categoryRepository.deleteById(categoryId);
    }

    /**
     * Прибавляет введённое целое число к уже существующему значению
     * суммы категории за указанный месяц и сохраняет в базу.
     */
    @Transactional
    public void addAmount(Long categoryId, int year, int month, long value) {
        ExpenseCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Категория не найдена: " + categoryId));
        ExpenseAmount amount = amountRepository
                .findByCategoryIdAndYearAndMonth(categoryId, year, month)
                .orElseGet(() -> new ExpenseAmount(category, year, month, BigDecimal.ZERO));
        amount.setAmount(amount.getAmount().add(BigDecimal.valueOf(value)));
        amountRepository.save(amount);
    }

    /** Обнуляет сумму категории за конкретный месяц. */
    @Transactional
    public void resetAmount(Long categoryId, int year, int month) {
        amountRepository.findByCategoryIdAndYearAndMonth(categoryId, year, month)
                .ifPresent(a -> {
                    a.setAmount(BigDecimal.ZERO);
                    amountRepository.save(a);
                });
    }

    /**
     * Формирует структуру данных для таблицы: список строк (категорий),
     * в каждой — значения по месяцам 1..12 и итог за год.
     */
    public List<Map<String, Object>> buildTable(int year) {
        Map<Long, BigDecimal[]> byCategory = new HashMap<>();
        for (ExpenseAmount a : amountRepository.findByYear(year)) {
            byCategory.computeIfAbsent(a.getCategory().getId(),
                    k -> new BigDecimal[]{
                            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO})
                    [a.getMonth() - 1] = a.getAmount();
        }

        List<Map<String, Object>> rows = new ArrayList<>();
        BigDecimal[] monthTotals = new BigDecimal[12];
        for (int i = 0; i < 12; i++) {
            monthTotals[i] = BigDecimal.ZERO;
        }
        BigDecimal yearTotal = BigDecimal.ZERO;

        for (ExpenseCategory c : getCategories()) {
            BigDecimal[] values = byCategory.getOrDefault(c.getId(), new BigDecimal[0]);
            List<BigDecimal> months = new ArrayList<>(12);
            BigDecimal rowTotal = BigDecimal.ZERO;
            for (int m = 0; m < 12; m++) {
                BigDecimal v = m < values.length ? values[m] : BigDecimal.ZERO;
                months.add(v);
                rowTotal = rowTotal.add(v);
                monthTotals[m] = monthTotals[m].add(v);
            }
            yearTotal = yearTotal.add(rowTotal);

            Map<String, Object> row = new HashMap<>();
            row.put("id", c.getId());
            row.put("name", c.getName());
            row.put("months", months);
            row.put("total", rowTotal);
            rows.add(row);
        }

        Map<String, Object> totalRow = new HashMap<>();
        totalRow.put("id", -1L);
        totalRow.put("name", "ИТОГО за каждый месяц");
        List<BigDecimal> totals = new ArrayList<>(List.of(monthTotals));
        totalRow.put("months", totals);
        totalRow.put("total", yearTotal);
        rows.add(totalRow);

        return rows;
    }

    public List<Integer> getAvailableYears() {
        // Текущий и предыдущие 4 года — переключатель периода
        int current = java.time.Year.now().getValue();
        List<Integer> years = new ArrayList<>();
        for (int y = current; y >= current - 4; y--) {
            years.add(y);
        }
        return years;
    }
}
