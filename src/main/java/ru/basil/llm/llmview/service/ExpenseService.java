package ru.basil.llm.llmview.service;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.basil.llm.llmview.entity.ExpenseAmount;
import ru.basil.llm.llmview.entity.ExpenseCategory;
import ru.basil.llm.llmview.entity.ExpenseYear;
import ru.basil.llm.llmview.repository.ExpenseAmountRepository;
import ru.basil.llm.llmview.repository.ExpenseCategoryRepository;
import ru.basil.llm.llmview.repository.ExpenseYearRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Сервис трат: категории, суммы по месяцам, удаление категорий, обнуление сумм,
 * добавление/удаление лет и детализация по месяцам и годам.
 * Все данные хранятся в in-memory базе (таблицы expense_category, expense_amount, expense_year).
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
    private final ExpenseYearRepository yearRepository;

    public ExpenseService(ExpenseCategoryRepository categoryRepository,
                          ExpenseAmountRepository amountRepository,
                          ExpenseYearRepository yearRepository) {
        this.categoryRepository = categoryRepository;
        this.amountRepository = amountRepository;
        this.yearRepository = yearRepository;
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

    /**
     * Стартовые годы: текущий и предыдущие 4 — чтобы переключатель периода
     * работал сразу после запуска приложения.
     */
    @PostConstruct
    @Transactional
    public void initDefaultYears() {
        if (yearRepository.count() > 0) {
            return;
        }
        int current = java.time.Year.now().getValue();
        for (int y = current; y >= current - 4; y--) {
            yearRepository.save(new ExpenseYear(y));
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

    /** Список годов для переключателя периода (по убыванию). */
    public List<Integer> getAvailableYears() {
        List<Integer> years = new ArrayList<>();
        for (ExpenseYear y : yearRepository.findAllByOrderByYearDesc()) {
            years.add(y.getYear());
        }
        // Текущий год всегда доступен, даже если все добавленные годы удалены
        int current = java.time.Year.now().getValue();
        if (!years.contains(current)) {
            years.add(0, current);
        }
        return years;
    }

    /** Год по умолчанию: текущий, либо самый новый из добавленных. */
    public int resolveDefaultYear() {
        List<Integer> years = getAvailableYears();
        int current = java.time.Year.now().getValue();
        if (years.contains(current)) {
            return current;
        }
        return years.isEmpty() ? current : years.get(0);
    }

    /** Добавляет год. Возвращает текст ошибки или null при успехе. */
    @Transactional
    public String addYear(int year) {
        if (year < 2000 || year > 2100) {
            return "Год должен быть в диапазоне 2000–2100";
        }
        if (yearRepository.existsByYear(year)) {
            return "Год " + year + " уже существует";
        }
        yearRepository.save(new ExpenseYear(year));
        return null;
    }

    /** Удаляет год вместе со всеми суммами трат за этот год. */
    @Transactional
    public String deleteYear(int year) {
        ExpenseYear existing = yearRepository.findByYear(year);
        if (existing == null) {
            return "Год " + year + " не найден";
        }
        if (yearRepository.findAllByOrderByYearDesc().size() <= 1) {
            return "Нельзя удалить единственный оставшийся год";
        }
        amountRepository.deleteByYear(year);
        yearRepository.delete(existing);
        return null;
    }

    /**
     * Формирует структуру данных для вкладки «Детализация»:
     * по каждой категории — сколько раз вносились траты (количество непустых
     * записей с суммой больше нуля) за выбранный месяц и за весь год.
     */
    public List<Map<String, Object>> buildDetail(int year, int month) {
        Map<Long, Integer> monthCount = new HashMap<>();
        Map<Long, Integer> yearCount = new HashMap<>();
        Map<Long, BigDecimal> monthSum = new HashMap<>();
        Map<Long, BigDecimal> yearSum = new HashMap<>();

        for (ExpenseAmount a : amountRepository.findByYear(year)) {
            Long catId = a.getCategory().getId();
            boolean hasValue = a.getAmount() != null && a.getAmount().compareTo(BigDecimal.ZERO) != 0;
            if (!hasValue) {
                continue;
            }
            yearCount.merge(catId, 1, Integer::sum);
            yearSum.merge(catId, a.getAmount(), BigDecimal::add);
            if (a.getMonth() == month) {
                monthCount.merge(catId, 1, Integer::sum);
                monthSum.merge(catId, a.getAmount(), BigDecimal::add);
            }
        }

        List<Map<String, Object>> rows = new ArrayList<>();
        int totalMonth = 0;
        int totalYear = 0;
        BigDecimal totalMonthSum = BigDecimal.ZERO;
        BigDecimal totalYearSum = BigDecimal.ZERO;

        for (ExpenseCategory c : getCategories()) {
            int mCnt = monthCount.getOrDefault(c.getId(), 0);
            int yCnt = yearCount.getOrDefault(c.getId(), 0);
            BigDecimal mSum = monthSum.getOrDefault(c.getId(), BigDecimal.ZERO);
            BigDecimal ySum = yearSum.getOrDefault(c.getId(), BigDecimal.ZERO);
            totalMonth += mCnt;
            totalYear += yCnt;
            totalMonthSum = totalMonthSum.add(mSum);
            totalYearSum = totalYearSum.add(ySum);

            Map<String, Object> row = new HashMap<>();
            row.put("id", c.getId());
            row.put("name", c.getName());
            row.put("monthCount", mCnt);
            row.put("yearCount", yCnt);
            row.put("monthSum", mSum);
            row.put("yearSum", ySum);
            rows.add(row);
        }

        Map<String, Object> totalRow = new HashMap<>();
        totalRow.put("id", -1L);
        totalRow.put("name", "ИТОГО");
        totalRow.put("monthCount", totalMonth);
        totalRow.put("yearCount", totalYear);
        totalRow.put("monthSum", totalMonthSum);
        totalRow.put("yearSum", totalYearSum);
        rows.add(totalRow);

        return rows;
    }
}
