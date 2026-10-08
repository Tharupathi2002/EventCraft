package com.eventcraft.budgetexpense.service;

import com.eventcraft.budgetexpense.dto.PaymentResult;
import com.eventcraft.budgetexpense.entity.Budget;
import com.eventcraft.budgetexpense.entity.Expense;
import com.eventcraft.budgetexpense.exception.ResourceNotFoundException;
import com.eventcraft.budgetexpense.repository.ExpenseRepository;
import com.eventcraft.vendorvenue.entity.Vendor;
import com.eventcraft.vendorvenue.service.BookingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Business logic for recording, editing, deleting and listing expenses under a budget.
 * Controllers verify the budget belongs to the logged-in host first; every single-expense
 * method here additionally checks that the expense belongs to that budget.
 */
@Service
@Transactional
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
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

    /** Loads an expense only if it sits under the given budget; otherwise behaves as not found. */
    @Transactional(readOnly = true)
    public Expense findInBudgetOrThrow(Long budgetId, Long expenseId) {
        return expenseRepository.findById(expenseId)
                .filter(e -> e.getBudget() != null && budgetId.equals(e.getBudget().getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found."));
    }

    public Expense create(Budget budget, Expense expense) {
        // The form binds straight onto the entity, so never trust id / payment fields from it.
        expense.setId(null);
        expense.setPaid(false);
        expense.setPaymentReference(null);
        expense.setCardLast4(null);
        expense.setPaidDate(null);
        expense.setBudget(budget);
        return expenseRepository.save(expense);
    }

    /** Builds an expense from a vendor: name and category from the vendor, custom/base amount, date from the event. */
    public Expense createFromVendor(Budget budget, Vendor vendor, BigDecimal customAmount) {
        String description = "Vendor: " + vendor.getBusinessName();
        if (description.length() > 200) {
            description = description.substring(0, 200);
        }
        BigDecimal amount = (customAmount != null && customAmount.compareTo(BigDecimal.ZERO) > 0)
                ? customAmount
                : vendor.getBasePrice();
        LocalDate date = budget.getEventDate() != null ? budget.getEventDate() : LocalDate.now();
        Expense expense = new Expense(budget, BookingService.expenseCategoryFor(vendor),
                description, amount, date);
        return create(budget, expense);
    }

    public Expense createFromVendor(Budget budget, Vendor vendor) {
        return createFromVendor(budget, vendor, null);
    }

    public Expense update(Long budgetId, Long expenseId, Expense changes) {
        Expense existing = findInBudgetOrThrow(budgetId, expenseId);
        existing.setDescription(changes.getDescription());
        existing.setCategory(changes.getCategory());
        existing.setAmount(changes.getAmount());
        return expenseRepository.save(existing);
    }

    public void delete(Long budgetId, Long expenseId) {
        expenseRepository.delete(findInBudgetOrThrow(budgetId, expenseId));
    }

    public void markPaid(Long budgetId, Long expenseId, PaymentResult result) {
        if (!result.approved()) {
            throw new IllegalStateException("Cannot mark paid: payment was not approved");
        }
        Expense expense = findInBudgetOrThrow(budgetId, expenseId);
        expense.setPaid(true);
        expense.setPaymentReference(result.reference());
        expense.setCardLast4(result.cardLast4());
        expense.setPaidDate(LocalDate.now());
        expenseRepository.save(expense);
    }

    /**
     * Pays every currently-unpaid expense in a budget in one go, using a single sandbox payment
     * result (one reference/card applies to all of them) -- backs the dashboard's "Pay All" button.
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

    public void markUnpaid(Long budgetId, Long expenseId) {
        Expense expense = findInBudgetOrThrow(budgetId, expenseId);
        expense.setPaid(false);
        expense.setPaymentReference(null);
        expense.setCardLast4(null);
        expense.setPaidDate(null);
        expenseRepository.save(expense);
    }

    /**
     * Removes an expense by id if it still exists and has not been paid yet. Used by the
     * vendor/venue module when a booking is cancelled or deleted.
     *
     * @return true if the expense is gone afterwards (deleted now or already missing);
     *         false if it was kept because it has already been paid.
     */
    public boolean deleteIfUnpaid(Long expenseId) {
        return expenseRepository.findById(expenseId).map(e -> {
            if (e.isPaid()) {
                return false;
            }
            expenseRepository.delete(e);
            return true;
        }).orElse(true);
    }
}
