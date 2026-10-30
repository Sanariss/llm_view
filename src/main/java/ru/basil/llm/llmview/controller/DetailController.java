package ru.basil.llm.llmview.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.basil.llm.llmview.service.ExpenseService;

import java.time.Year;
import java.time.YearMonth;
import java.util.List;

/**
 * Вкладка «Детализация» (/detail): сколько раз вносились траты по каждой
 * категории за выбранный месяц и за выбранный год. Доступна после авторизации.
 */
@Controller
public class DetailController {

    private final ExpenseService expenseService;

    public DetailController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @GetMapping("/detail")
    public String detail(@RequestParam(value = "year", required = false) Integer year,
                         @RequestParam(value = "month", required = false) Integer month,
                         HttpSession session,
                         Model model) {
        String redirect = SessionUtil.requireAuth(session);
        if (redirect != null) {
            return redirect;
        }

        List<Integer> years = expenseService.getAvailableYears();
        int y = (year != null && years.contains(year)) ? year : expenseService.resolveDefaultYear();
        int m = (month != null && month >= 1 && month <= 12)
                ? month : YearMonth.now().getMonthValue();

        model.addAttribute("tab", "detail");
        model.addAttribute("year", y);
        model.addAttribute("years", years);
        model.addAttribute("month", m);
        model.addAttribute("monthNames", List.of(ExpenseService.MONTH_NAMES));
        model.addAttribute("rows", expenseService.buildDetail(y, m));
        model.addAttribute("fullName", session.getAttribute(SessionUtil.ATTR_FULL_NAME));
        return "detail";
    }
}
