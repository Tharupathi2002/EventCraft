package se.it25102007.budgetmanagement.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import se.it25102007.budgetmanagement.dto.PaymentRequest;
import se.it25102007.budgetmanagement.dto.PaymentResult;
import se.it25102007.budgetmanagement.model.Expense;
import se.it25102007.budgetmanagement.service.BudgetService;
import se.it25102007.budgetmanagement.service.ExpenseService;
import se.it25102007.budgetmanagement.service.PaymentSandboxService;

import java.util.List;

@Controller
@RequestMapping("/budgets/{budgetId}/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;
    private final BudgetService budgetService;
    private final PaymentSandboxService paymentSandboxService;

    public ExpenseController(ExpenseService expenseService, BudgetService budgetService,
                              PaymentSandboxService paymentSandboxService) {
        this.expenseService = expenseService;
        this.budgetService = budgetService;
        this.paymentSandboxService = paymentSandboxService;
    }

    @GetMapping("/new")
    public String newExpenseForm(@PathVariable Long budgetId, Model model) {
        model.addAttribute("budget", budgetService.findByIdOrThrow(budgetId));
        model.addAttribute("categories", Expense.CATEGORIES);
        model.addAttribute("expense", new Expense());
        return "expenses/form";
    }

    @PostMapping
    public String create(@PathVariable Long budgetId,
                          @Valid @ModelAttribute("expense") Expense expense,
                          BindingResult result,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("budget", budgetService.findByIdOrThrow(budgetId));
            model.addAttribute("categories", Expense.CATEGORIES);
            return "expenses/form";
        }
        expenseService.create(budgetId, expense);
        redirectAttributes.addFlashAttribute("success", "Expense added.");
        return "redirect:/budgets/" + budgetId;
    }

    @GetMapping("/{expenseId}/edit")
    public String editForm(@PathVariable Long budgetId, @PathVariable Long expenseId, Model model) {
        model.addAttribute("budget", budgetService.findByIdOrThrow(budgetId));
        model.addAttribute("categories", Expense.CATEGORIES);
        model.addAttribute("expense", expenseService.findByIdOrThrow(expenseId));
        return "expenses/form";
    }

    @PostMapping("/{expenseId}")
    public String update(@PathVariable Long budgetId,
                          @PathVariable Long expenseId,
                          @Valid @ModelAttribute("expense") Expense expense,
                          BindingResult result,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("budget", budgetService.findByIdOrThrow(budgetId));
            model.addAttribute("categories", Expense.CATEGORIES);
            return "expenses/form";
        }
        expenseService.update(expenseId, expense);
        redirectAttributes.addFlashAttribute("success", "Expense updated.");
        return "redirect:/budgets/" + budgetId;
    }

    @PostMapping("/{expenseId}/delete")
    public String delete(@PathVariable Long budgetId,
                          @PathVariable Long expenseId,
                          RedirectAttributes redirectAttributes) {
        expenseService.delete(expenseId);
        redirectAttributes.addFlashAttribute("success", "Expense deleted.");
        return "redirect:/budgets/" + budgetId;
    }

    @GetMapping("/{expenseId}/pay")
    public String payForm(@PathVariable Long budgetId, @PathVariable Long expenseId, Model model) {
        Expense expense = expenseService.findByIdOrThrow(expenseId);
        if (expense.isPaid()) {
            return "redirect:/budgets/" + budgetId;
        }
        model.addAttribute("budget", budgetService.findByIdOrThrow(budgetId));
        model.addAttribute("expense", expense);
        model.addAttribute("paymentRequest", new PaymentRequest());
        return "expenses/pay";
    }

    @PostMapping("/{expenseId}/pay")
    public String pay(@PathVariable Long budgetId,
                       @PathVariable Long expenseId,
                       @Valid @ModelAttribute("paymentRequest") PaymentRequest paymentRequest,
                       BindingResult result,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        Expense expense = expenseService.findByIdOrThrow(expenseId);

        if (result.hasErrors()) {
            model.addAttribute("budget", budgetService.findByIdOrThrow(budgetId));
            model.addAttribute("expense", expense);
            return "expenses/pay";
        }

        PaymentResult paymentResult = paymentSandboxService.processPayment(paymentRequest);

        if (!paymentResult.approved()) {
            model.addAttribute("budget", budgetService.findByIdOrThrow(budgetId));
            model.addAttribute("expense", expense);
            model.addAttribute("declineReason", paymentResult.message());
            return "expenses/pay";
        }

        expenseService.markPaid(expenseId, paymentResult);
        redirectAttributes.addFlashAttribute("success",
                "Payment approved. Reference " + paymentResult.reference()
                        + ", card ending " + paymentResult.cardLast4() + ".");
        return "redirect:/budgets/" + budgetId;
    }

    @PostMapping("/{expenseId}/mark-unpaid")
    public String markUnpaid(@PathVariable Long budgetId,
                              @PathVariable Long expenseId,
                              RedirectAttributes redirectAttributes) {
        expenseService.markUnpaid(expenseId);
        redirectAttributes.addFlashAttribute("success", "Expense marked as unpaid.");
        return "redirect:/budgets/" + budgetId;
    }

    /**
     * Consolidated payment: instead of one "Pay" button per unpaid expense,
     * this pays every currently-unpaid expense for the budget in a single
     * sandbox transaction covering their combined total.
     */
    @GetMapping("/pay-all")
    public String payAllForm(@PathVariable Long budgetId, Model model) {
        List<Expense> unpaid = expenseService.findUnpaid(budgetId);
        if (unpaid.isEmpty()) {
            return "redirect:/budgets/" + budgetId;
        }
        model.addAttribute("budget", budgetService.findByIdOrThrow(budgetId));
        model.addAttribute("unpaidExpenses", unpaid);
        model.addAttribute("paymentSummary", budgetService.buildPaymentSummary(budgetId));
        model.addAttribute("paymentRequest", new PaymentRequest());
        return "expenses/pay-all";
    }

    @PostMapping("/pay-all")
    public String payAll(@PathVariable Long budgetId,
                          @Valid @ModelAttribute("paymentRequest") PaymentRequest paymentRequest,
                          BindingResult result,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("budget", budgetService.findByIdOrThrow(budgetId));
            model.addAttribute("unpaidExpenses", expenseService.findUnpaid(budgetId));
            model.addAttribute("paymentSummary", budgetService.buildPaymentSummary(budgetId));
            return "expenses/pay-all";
        }

        PaymentResult paymentResult = paymentSandboxService.processPayment(paymentRequest);

        if (!paymentResult.approved()) {
            model.addAttribute("budget", budgetService.findByIdOrThrow(budgetId));
            model.addAttribute("unpaidExpenses", expenseService.findUnpaid(budgetId));
            model.addAttribute("paymentSummary", budgetService.buildPaymentSummary(budgetId));
            model.addAttribute("declineReason", paymentResult.message());
            return "expenses/pay-all";
        }

        expenseService.markAllUnpaidAsPaid(budgetId, paymentResult);
        redirectAttributes.addFlashAttribute("success",
                "Payment approved for all pending expenses. Reference " + paymentResult.reference()
                        + ", card ending " + paymentResult.cardLast4() + ".");
        return "redirect:/budgets/" + budgetId;
    }

    @GetMapping
    public String filter(@PathVariable Long budgetId,
                          @RequestParam(required = false) String category,
                          @RequestParam(required = false) String keyword,
                          Model model) {
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
        return "budgets/dashboard";
    }
}
