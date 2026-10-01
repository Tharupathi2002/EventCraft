package se.it25102007.budgetmanagement.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.it25102007.budgetmanagement.dto.PaymentResult;
import se.it25102007.budgetmanagement.exception.ResourceNotFoundException;
import se.it25102007.budgetmanagement.model.Budget;
import se.it25102007.budgetmanagement.model.Expense;
import se.it25102007.budgetmanagement.repository.ExpenseRepository;

import java.time.LocalDate;
import java.util.List;

/** Business logic for recording, editing, deleting and listing expenses under a budget. */
@Service
@Transactional
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final BudgetService budgetService;

    public ExpenseService(ExpenseRepository expenseRepository, BudgetService budgetService) {
        this.expenseRepository = expenseRepository;
        this.budgetService = budgetService;
    }

    @Transactional(readOnly = true)
    public List<Expense> findByBudget(Long budgetId) {
        return expenseRepository.findByBudgetIdOrderByExpenseDateDesc(budgetId);
    }

    @Transactional(readOnly = true)
    public List<Expense> findUnpaid(Long budgetId) {
        return expenseRepository.findByBudgetIdAndPaidOrderByExpenseDateAsc(budgetId, false);
    }

    @Transactional(readOnly = true)
    public List<Expense> findByBudgetAndCategory(Long budgetId, String category) {
        if (category == null || category.isBlank()) {
            return findByBudget(budgetId);
        }
        return expenseRepository.findByBudgetIdAndCategoryOrderByExpenseDateDesc(budgetId, category);
    }

    @Transactional(readOnly = true)
    public List<Expense> search(Long budgetId, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return findByBudget(budgetId);
        }
        return expenseRepository.findByBudgetIdAndDescriptionContainingIgnoreCaseOrderByExpenseDateDesc(
                budgetId, keyword.trim());
    }

    @Transactional(readOnly = true)
    public Expense findByIdOrThrow(Long expenseId) {
        return expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id " + expenseId));
    }

    public Expense create(Long budgetId, Expense expense) {
        Budget budget = budgetService.findByIdOrThrow(budgetId);
        expense.setBudget(budget);
        return expenseRepository.save(expense);
    }

    public Expense update(Long expenseId, Expense changes) {
        Expense existing = findByIdOrThrow(expenseId);
        existing.setDescription(changes.getDescription());
        existing.setCategory(changes.getCategory());
        existing.setAmount(changes.getAmount());
        existing.setExpenseDate(changes.getExpenseDate());
        return expenseRepository.save(existing);
    }

    public void delete(Long expenseId) {
        Expense expense = findByIdOrThrow(expenseId);
        expenseRepository.delete(expense);
    }

    public void markPaid(Long expenseId, PaymentResult result) {
        if (!result.approved()) {
            throw new IllegalStateException("Cannot mark paid: payment was not approved");
        }
        Expense expense = findByIdOrThrow(expenseId);
        expense.setPaid(true);
        expense.setPaymentReference(result.reference());
        expense.setCardLast4(result.cardLast4());
        expense.setPaidDate(LocalDate.now());
        expenseRepository.save(expense);
    }

    /**
     * Pays every currently-unpaid expense in a budget in one go, using a
     * single sandbox payment result (one reference/card applies to all of
     * them) -- backs the dashboard's single "Pay All" button, instead of
     * requiring one payment per expense.
     */
    public void markAllUnpaidAsPaid(Long budgetId, PaymentResult result) {
        if (!result.approved()) {
            throw new IllegalStateException("Cannot mark paid: payment was not approved");
        }
        List<Expense> unpaid = findUnpaid(budgetId);
        LocalDate today = LocalDate.now();
        for (Expense expense : unpaid) {
            expense.setPaid(true);
            expense.setPaymentReference(result.reference());
            expense.setCardLast4(result.cardLast4());
            expense.setPaidDate(today);
        }
        expenseRepository.saveAll(unpaid);
    }

    public void markUnpaid(Long expenseId) {
        Expense expense = findByIdOrThrow(expenseId);
        expense.setPaid(false);
        expense.setPaymentReference(null);
        expense.setCardLast4(null);
        expense.setPaidDate(null);
        expenseRepository.save(expense);
    }
}
