package com.eventcraft.budgetexpense.controller;

import com.eventcraft.auth.AuthSession;
import com.eventcraft.budgetexpense.dto.ExpenseForm;
import com.eventcraft.budgetexpense.dto.PaymentRequest;
import com.eventcraft.budgetexpense.dto.PaymentResult;
import com.eventcraft.budgetexpense.entity.Budget;
import com.eventcraft.budgetexpense.entity.Expense;
import com.eventcraft.budgetexpense.service.BudgetService;
import com.eventcraft.budgetexpense.service.ExpenseService;
import com.eventcraft.budgetexpense.service.PaymentSandboxService;
import com.eventcraft.vendorvenue.entity.Vendor;
import com.eventcraft.vendorvenue.service.VendorService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Expense pages for one budget. Every handler first resolves the budget through
 * BudgetService.findOwnedOrThrow (host check); single-expense operations also verify the
 * expense belongs to that budget, so URLs can't be mixed to reach someone else's data.
 */
@Controller
@RequestMapping("/budgets/{budgetId}/expenses")
public class ExpenseController {

    private static final String FORM = "budgetexpense/expenses/form";
    private static final String ADD = "budgetexpense/expenses/add";
    private static final String PAY = "budgetexpense/expenses/pay";
    private static final String PAY_ALL = "budgetexpense/expenses/pay-all";

    private final ExpenseService expenseService;
    private final BudgetService budgetService;
    private final PaymentSandboxService paymentSandboxService;
    private final VendorService vendorService;

    public ExpenseController(ExpenseService expenseService, BudgetService budgetService,
                             PaymentSandboxService paymentSandboxService, VendorService vendorService) {
        this.expenseService = expenseService;
        this.budgetService = budgetService;
        this.paymentSandboxService = paymentSandboxService;
        this.vendorService = vendorService;
    }

    private Budget owned(Long budgetId, HttpSession session) {
        return budgetService.findOwnedOrThrow(budgetId, AuthSession.name(session));
    }

    @GetMapping("/new")
    public String newExpenseForm(@PathVariable Long budgetId, HttpSession session, Model model) {
        addAddModel(model, owned(budgetId, session));
        model.addAttribute("expenseForm", new ExpenseForm());
        return ADD;
    }

    /** Expenses are added by picking a vendor; name, category and price come from the vendor, date from the event. */
    @PostMapping
    public String create(@PathVariable Long budgetId,
                         @Valid @ModelAttribute("expenseForm") ExpenseForm form,
                         BindingResult result,
                         HttpSession session,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        Budget budget = owned(budgetId, session);
        Vendor vendor = null;
        if (!result.hasErrors()) {
            vendor = vendorService.getVendorById(form.getVendorId()).orElse(null);
            if (vendor == null || !vendorService.isAvailableOn(vendor, budget.getEventDate())) {
                result.rejectValue("vendorId", "unavailable", "That vendor is not available for this event.");
            }
        }
        if (result.hasErrors()) {
            addAddModel(model, budget);
            return ADD;
        }
        expenseService.createFromVendor(budget, vendor, form.getAmount());
        redirectAttributes.addFlashAttribute("success", "Expense added for " + vendor.getBusinessName() + ".");
        return "redirect:/budgets/" + budgetId;
    }

    private void addAddModel(Model model, Budget budget) {
        model.addAttribute("budget", budget);
        model.addAttribute("vendors", vendorService.findAvailableForDate(budget.getEventDate()));
    }

    @GetMapping("/{expenseId}/edit")
    public String editForm(@PathVariable Long budgetId, @PathVariable Long expenseId,
                           HttpSession session, Model model) {
        model.addAttribute("budget", owned(budgetId, session));
        model.addAttribute("categories", Expense.CATEGORIES);
        model.addAttribute("expense", expenseService.findInBudgetOrThrow(budgetId, expenseId));
        return FORM;
    }

    @PostMapping("/{expenseId}")
    public String update(@PathVariable Long budgetId,
                         @PathVariable Long expenseId,
                         @Valid @ModelAttribute("expense") Expense expense,
                         BindingResult result,
                         HttpSession session,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        Budget budget = owned(budgetId, session);
        if (result.hasErrors()) {
            model.addAttribute("budget", budget);
            model.addAttribute("categories", Expense.CATEGORIES);
            return FORM;
        }
        expenseService.update(budgetId, expenseId, expense);
        redirectAttributes.addFlashAttribute("success", "Expense updated.");
        return "redirect:/budgets/" + budgetId;
    }

    @PostMapping("/{expenseId}/delete")
    public String delete(@PathVariable Long budgetId,
                         @PathVariable Long expenseId,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        owned(budgetId, session);
        expenseService.delete(budgetId, expenseId);
        redirectAttributes.addFlashAttribute("success", "Expense deleted.");
        return "redirect:/budgets/" + budgetId;
    }

    @GetMapping("/{expenseId}/pay")
    public String payForm(@PathVariable Long budgetId, @PathVariable Long expenseId,
                          HttpSession session, Model model) {
        Budget budget = owned(budgetId, session);
        Expense expense = expenseService.findInBudgetOrThrow(budgetId, expenseId);
        if (expense.isPaid()) {
            return "redirect:/budgets/" + budgetId;
        }
        model.addAttribute("budget", budget);
        model.addAttribute("expense", expense);
        model.addAttribute("paymentRequest", new PaymentRequest());
        return PAY;
    }

    @PostMapping("/{expenseId}/pay")
    public String pay(@PathVariable Long budgetId,
                      @PathVariable Long expenseId,
                      @Valid @ModelAttribute("paymentRequest") PaymentRequest paymentRequest,
                      BindingResult result,
                      HttpSession session,
                      Model model,
                      RedirectAttributes redirectAttributes) {
        Budget budget = owned(budgetId, session);
        Expense expense = expenseService.findInBudgetOrThrow(budgetId, expenseId);

        if (result.hasErrors()) {
            model.addAttribute("budget", budget);
            model.addAttribute("expense", expense);
            return PAY;
        }

        PaymentResult paymentResult = paymentSandboxService.processPayment(paymentRequest);

        if (!paymentResult.approved()) {
            model.addAttribute("budget", budget);
            model.addAttribute("expense", expense);
            model.addAttribute("declineReason", paymentResult.message());
            return PAY;
        }

        expenseService.markPaid(budgetId, expenseId, paymentResult);
        redirectAttributes.addFlashAttribute("success",
                "Payment approved. Reference " + paymentResult.reference()
                        + ", card ending " + paymentResult.cardLast4() + ".");
        return "redirect:/budgets/" + budgetId;
    }

    @PostMapping("/{expenseId}/mark-unpaid")
    public String markUnpaid(@PathVariable Long budgetId,
                             @PathVariable Long expenseId,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        owned(budgetId, session);
        expenseService.markUnpaid(budgetId, expenseId);
        redirectAttributes.addFlashAttribute("success", "Expense marked as unpaid.");
        return "redirect:/budgets/" + budgetId;
    }

    /**
     * Consolidated payment: instead of one "Pay" button per unpaid expense, this pays every
     * currently-unpaid expense for the budget in a single sandbox transaction.
     */
    @GetMapping("/pay-all")
    public String payAllForm(@PathVariable Long budgetId, HttpSession session, Model model) {
        Budget budget = owned(budgetId, session);
        List<Expense> unpaid = expenseService.findUnpaid(budgetId);
        if (unpaid.isEmpty()) {
            return "redirect:/budgets/" + budgetId;
        }
        model.addAttribute("budget", budget);
        model.addAttribute("unpaidExpenses", unpaid);
        model.addAttribute("paymentSummary", budgetService.buildPaymentSummary(budgetId));
        model.addAttribute("paymentRequest", new PaymentRequest());
        return PAY_ALL;
    }

    @PostMapping("/pay-all")
    public String payAll(@PathVariable Long budgetId,
                         @Valid @ModelAttribute("paymentRequest") PaymentRequest paymentRequest,
                         BindingResult result,
                         HttpSession session,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        Budget budget = owned(budgetId, session);

        if (result.hasErrors()) {
            addPayAllModel(model, budget);
            return PAY_ALL;
        }

        PaymentResult paymentResult = paymentSandboxService.processPayment(paymentRequest);

        if (!paymentResult.approved()) {
            addPayAllModel(model, budget);
            model.addAttribute("declineReason", paymentResult.message());
            return PAY_ALL;
        }

        expenseService.markAllUnpaidAsPaid(budgetId, paymentResult);
        redirectAttributes.addFlashAttribute("success",
                "Payment approved for all pending expenses. Reference " + paymentResult.reference()
                        + ", card ending " + paymentResult.cardLast4() + ".");
        return "redirect:/budgets/" + budgetId;
    }

    private void addPayAllModel(Model model, Budget budget) {
        model.addAttribute("budget", budget);
        model.addAttribute("unpaidExpenses", expenseService.findUnpaid(budget.getId()));
        model.addAttribute("paymentSummary", budgetService.buildPaymentSummary(budget.getId()));
    }

    @GetMapping
    public String filter(@PathVariable Long budgetId,
                         @RequestParam(required = false) String category,
                         @RequestParam(required = false) String keyword,
                         HttpSession session,
                         Model model) {
        owned(budgetId, session);
        model.addAttribute("summary", budgetService.buildSummary(budgetId));
        model.addAttribute("categoryTotals", budgetService.buildCategoryTotals(budgetId));
        model.addAttribute("paymentSummary", budgetService.buildPaymentSummary(budgetId));
        model.addAttribute("categories", Expense.CATEGORIES);
        model.addAttribute("unpaidExpenses", expenseService.findUnpaid(budgetId));
        model.addAttribute("selectedCategory", category);
        model.addAttribute("keyword", keyword);

        if (keyword != null && !keyword.isBlank()) {
            model.addAttribute("expenses", expenseService.search(budgetId, keyword));
        } else {
            model.addAttribute("expenses", expenseService.findByBudgetAndCategory(budgetId, category));
        }
        return "budgetexpense/budgets/dashboard";
    }
}
