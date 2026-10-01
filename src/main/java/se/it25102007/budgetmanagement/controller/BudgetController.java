package se.it25102007.budgetmanagement.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import se.it25102007.budgetmanagement.model.Budget;
import se.it25102007.budgetmanagement.model.Expense;
import se.it25102007.budgetmanagement.service.BudgetService;
import se.it25102007.budgetmanagement.service.ExpenseService;

import java.util.List;

@Controller
@RequestMapping("/budgets")
public class BudgetController {

    private final BudgetService budgetService;
    private final ExpenseService expenseService;

    public BudgetController(BudgetService budgetService, ExpenseService expenseService) {
        this.budgetService = budgetService;
        this.expenseService = expenseService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("budgets", budgetService.findAll());
        return "budgets/list";
    }

    @GetMapping("/new")
    public String newBudgetForm(Model model) {
        model.addAttribute("budget", new Budget());
        return "budgets/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("budget") Budget budget,
                          BindingResult result,
                          RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "budgets/form";
        }
        Budget saved = budgetService.create(budget);
        redirectAttributes.addFlashAttribute("success", "Budget created for " + saved.getEventName() + ".");
        return "redirect:/budgets/" + saved.getId();
    }

    @GetMapping("/{id}")
    public String dashboard(@PathVariable Long id, Model model) {
        model.addAttribute("summary", budgetService.buildSummary(id));
        model.addAttribute("categoryTotals", budgetService.buildCategoryTotals(id));
        model.addAttribute("paymentSummary", budgetService.buildPaymentSummary(id));
        model.addAttribute("categories", Expense.CATEGORIES);

        List<Expense> expenses = expenseService.findByBudget(id);
        model.addAttribute("expenses", expenses);
        model.addAttribute("unpaidExpenses", expenseService.findUnpaid(id));
        return "budgets/dashboard";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("budget", budgetService.findByIdOrThrow(id));
        return "budgets/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                          @Valid @ModelAttribute("budget") Budget budget,
                          BindingResult result,
                          RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "budgets/form";
        }
        budgetService.update(id, budget);
        redirectAttributes.addFlashAttribute("success", "Budget updated.");
        return "redirect:/budgets/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        budgetService.delete(id);
        redirectAttributes.addFlashAttribute("success", "Budget deleted.");
        return "redirect:/budgets";
    }
}
