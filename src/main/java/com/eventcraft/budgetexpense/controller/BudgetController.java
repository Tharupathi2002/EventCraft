package com.eventcraft.budgetexpense.controller;

import com.eventcraft.auth.AuthSession;
import com.eventcraft.budgetexpense.dto.BudgetForm;
import com.eventcraft.budgetexpense.entity.Budget;
import com.eventcraft.budgetexpense.entity.Expense;
import com.eventcraft.budgetexpense.service.BudgetService;
import com.eventcraft.budgetexpense.service.ExpenseService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

/**
 * Organizer-only pages (the AuthInterceptor guards /budgets/**). Every budget is looked up
 * through BudgetService.findOwnedOrThrow, so a host can only see and change budgets of events
 * they host.
 */
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
    public String list(HttpSession session, Model model) {
        model.addAttribute("budgets", budgetService.findForHost(AuthSession.name(session)));
        return "budgetexpense/budgets/list";
    }

    @GetMapping("/new")
    public String newBudgetForm(@RequestParam(required = false) Long eventId,
                                HttpSession session, Model model) {
        BudgetForm form = new BudgetForm();
        form.setEventId(eventId);
        model.addAttribute("budgetForm", form);
        model.addAttribute("events", budgetService.findEventsAvailableForBudget(AuthSession.name(session)));
        return "budgetexpense/budgets/form";
    }

    /** Entry point from an event's page: open its budget, or start creating one. */
    @GetMapping("/event/{eventId}")
    public String forEvent(@PathVariable Long eventId, HttpSession session) {
        Optional<Budget> budget = budgetService.findOwnedByEvent(eventId, AuthSession.name(session));
        return budget.map(b -> "redirect:/budgets/" + b.getId())
                .orElse("redirect:/budgets/new?eventId=" + eventId);
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("budgetForm") BudgetForm form,
                         BindingResult result,
                         HttpSession session,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        String host = AuthSession.name(session);
        if (form.getEventId() == null) {
            result.rejectValue("eventId", "required", "Please choose an event.");
        }
        Budget saved = null;
        if (!result.hasErrors()) {
            try {
                saved = budgetService.create(form, host);
            } catch (IllegalStateException ex) {
                result.rejectValue("eventId", "invalid", ex.getMessage());
            }
        }
        if (saved == null) {
            model.addAttribute("events", budgetService.findEventsAvailableForBudget(host));
            return "budgetexpense/budgets/form";
        }
        redirectAttributes.addFlashAttribute("success", "Budget created for " + saved.getEventName() + ".");
        return "redirect:/budgets/" + saved.getId();
    }

    @GetMapping("/{id}")
    public String dashboard(@PathVariable Long id, HttpSession session, Model model) {
        budgetService.findOwnedOrThrow(id, AuthSession.name(session));
        model.addAttribute("summary", budgetService.buildSummary(id));
        model.addAttribute("categoryTotals", budgetService.buildCategoryTotals(id));
        model.addAttribute("paymentSummary", budgetService.buildPaymentSummary(id));
        model.addAttribute("categories", Expense.CATEGORIES);
        model.addAttribute("expenses", expenseService.findByBudget(id));
        model.addAttribute("unpaidExpenses", expenseService.findUnpaid(id));
        return "budgetexpense/budgets/dashboard";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, HttpSession session, Model model) {
        Budget budget = budgetService.findOwnedOrThrow(id, AuthSession.name(session));
        BudgetForm form = new BudgetForm();
        form.setEventId(budget.getEventId());
        form.setTotalBudget(budget.getTotalBudget());
        model.addAttribute("budget", budget);
        model.addAttribute("budgetForm", form);
        return "budgetexpense/budgets/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("budgetForm") BudgetForm form,
                         BindingResult result,
                         HttpSession session,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        String host = AuthSession.name(session);
        Budget budget = budgetService.findOwnedOrThrow(id, host);
        if (result.hasErrors()) {
            model.addAttribute("budget", budget);
            return "budgetexpense/budgets/form";
        }
        budgetService.update(id, host, form.getTotalBudget());
        redirectAttributes.addFlashAttribute("success", "Budget updated.");
        return "redirect:/budgets/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        budgetService.delete(id, AuthSession.name(session));
        redirectAttributes.addFlashAttribute("success", "Budget deleted.");
        return "redirect:/budgets";
    }
}
