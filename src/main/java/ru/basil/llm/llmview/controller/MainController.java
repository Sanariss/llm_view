package ru.basil.llm.llmview.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.basil.llm.llmview.service.ExpenseService;

import java.time.Year;
import java.util.List;

/**
 * Страница трат /main: слева меню с кнопками, справа таблица
 * «категории × месяцы» (структура как в salary.xlsx).
 * Доступна только после авторизации.
 */
@Controller
public class MainController {

    private final ExpenseService expenseService;

    public MainController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @GetMapping("/main")
    public String main(@RequestParam(value = "year", required = false) Integer year,
                       HttpSession session,
                       Model model) {
        String redirect = SessionUtil.requireAuth(session);
        if (redirect != null) {
            return redirect;
        }
        prepareModel(model, session, year);
        return "main";
    }

    /** Добавление категории трат. */
    @PostMapping("/main/category/add")
    public String addCategory(@RequestParam String name,
                              @RequestParam(required = false) Integer year,
                              HttpSession session,
                              Model model) {
        String redirect = SessionUtil.requireAuth(session);
        if (redirect != null) {
            return redirect;
        }
        String error = expenseService.addCategory(name);
        if (error != null) {
            model.addAttribute("actionError", error);
        } else {
            model.addAttribute("actionMessage", "Категория «" + name.trim() + "» добавлена");
        }
        prepareModel(model, session, year);
        return "main";
    }

    /** Удаление категории трат (вместе со всеми её суммами). */
    @PostMapping("/main/category/delete")
    public String deleteCategory(@RequestParam Long categoryId,
                                 @RequestParam(required = false) Integer year,
                                 HttpSession session) {
        if (SessionUtil.isAuthed(session)) {
            expenseService.deleteCategory(categoryId);
        }
        return redirectToMain(year, "");
    }

    /** Добавление года. */
    @PostMapping("/main/year/add")
    public String addYear(@RequestParam int year,
                          HttpSession session) {
        if (!SessionUtil.isAuthed(session)) {
            return "redirect:/auth";
        }
        String error = expenseService.addYear(year);
        if (error != null) {
            return redirectToMain(null, "yearError=1&msg=" + java.net.URLEncoder.encode(error,
                    java.nio.charset.StandardCharsets.UTF_8));
        }
        // после добавления переключаемся на новый год
        return "redirect:/main?year=" + year;
    }

    /** Удаление года вместе со всеми суммами трат за этот год. */
    @PostMapping("/main/year/delete")
    public String deleteYear(@RequestParam int year,
                             HttpSession session) {
        if (!SessionUtil.isAuthed(session)) {
            return "redirect:/auth";
        }
        String error = expenseService.deleteYear(year);
        if (error != null) {
            return redirectToMain(null, "yearError=1&msg=" + java.net.URLEncoder.encode(error,
                    java.nio.charset.StandardCharsets.UTF_8));
        }
        return redirectToMain(null, "yearDeleted=" + year);
    }

    /** Прибавление целого числа к сумме категории за выбранный месяц. */
    @PostMapping("/main/amount/add")
    public String addAmount(@RequestParam Long categoryId,
                            @RequestParam int month,
                            @RequestParam long value,
                            @RequestParam(required = false) Integer year,
                            HttpSession session) {
        if (!SessionUtil.isAuthed(session)) {
            return "redirect:/auth";
        }
        if (value <= 0) {
            return redirectToMain(year, "sumError=1");
        }
        int y = year != null ? year : expenseService.resolveDefaultYear();
        expenseService.addAmount(categoryId, y, month, value);
        return redirectToMain(y, "added=1");
    }

    /** Обнуление суммы категории за конкретный месяц. */
    @PostMapping("/main/amount/reset")
    public String resetAmount(@RequestParam Long categoryId,
                              @RequestParam int month,
                              @RequestParam(required = false) Integer year,
                              HttpSession session) {
        if (!SessionUtil.isAuthed(session)) {
            return "redirect:/auth";
        }
        int y = year != null ? year : expenseService.resolveDefaultYear();
        expenseService.resetAmount(categoryId, y, month);
        return redirectToMain(y, "reset=1");
    }

    private String redirectToMain(Integer year, String param) {
        int y = year != null ? year : expenseService.resolveDefaultYear();
        return "redirect:/main?year=" + y + (param.isEmpty() ? "" : "&" + param);
    }

    private void prepareModel(Model model, HttpSession session, Integer year) {
        int y = (year != null && expenseService.getAvailableYears().contains(year))
                ? year : expenseService.resolveDefaultYear();
        model.addAttribute("year", y);
        model.addAttribute("years", expenseService.getAvailableYears());
        model.addAttribute("monthNames", List.of(ExpenseService.MONTH_NAMES));
        model.addAttribute("rows", expenseService.buildTable(y));
        model.addAttribute("fullName", session.getAttribute(SessionUtil.ATTR_FULL_NAME));
    }
}
