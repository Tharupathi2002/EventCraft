package se.it25102007.budgetmanagement.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.it25102007.budgetmanagement.dto.BudgetSummary;
import se.it25102007.budgetmanagement.dto.CategoryTotal;
import se.it25102007.budgetmanagement.dto.PaymentSummary;
import se.it25102007.budgetmanagement.exception.ResourceNotFoundException;
import se.it25102007.budgetmanagement.model.Budget;
import se.it25102007.budgetmanagement.repository.BudgetRepository;
import se.it25102007.budgetmanagement.repository.ExpenseRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Business logic for budgets: CRUD plus the calculations behind the
 * dashboard (remaining balance, spending by category, overspend warning,
 * paid/unpaid totals).
 */
@Service
@Transactional
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final ExpenseRepository expenseRepository;

    public BudgetService(BudgetRepository budgetRepository, ExpenseRepository expenseRepository) {
        this.budgetRepository = budgetRepository;
        this.expenseRepository = expenseRepository;
    }

    @Transactional(readOnly = true)
    public List<Budget> findAll() {
        return budgetRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Budget findByIdOrThrow(Long id) {
        return budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found with id " + id));
    }

    /**
     * Saves the new budget, then auto-assigns eventId to match the budget's
     * own database-generated id -- the host never provides this value.
     * Two saves are needed because the id doesn't exist until the first
     * insert completes.
     */
    public Budget create(Budget budget) {
        budget.setEventId(0L);
        Budget saved = budgetRepository.save(budget);
        saved.setEventId(saved.getId());
        return budgetRepository.save(saved);
    }

    public Budget update(Long id, Budget changes) {
        Budget existing = findByIdOrThrow(id);
        // eventId is intentionally left untouched here -- it's database-assigned
        // at creation time and never something the host edits afterward.
        existing.setEventName(changes.getEventName());
        existing.setTotalBudget(changes.getTotalBudget());
        return budgetRepository.save(existing);
    }

    public void delete(Long id) {
        Budget budget = findByIdOrThrow(id);
        budgetRepository.delete(budget);
    }

    @Transactional(readOnly = true)
    public BudgetSummary buildSummary(Long budgetId) {
        Budget budget = findByIdOrThrow(budgetId);
        BigDecimal totalSpent = expenseRepository.sumAmountByBudgetId(budgetId);
        return new BudgetSummary(budget, totalSpent);
    }

    @Transactional(readOnly = true)
    public List<CategoryTotal> buildCategoryTotals(Long budgetId) {
        Budget budget = findByIdOrThrow(budgetId);
        List<Object[]> rows = expenseRepository.sumAmountGroupedByCategory(budgetId);
        List<CategoryTotal> results = new ArrayList<>();
        for (Object[] row : rows) {
            String category = (String) row[0];
            BigDecimal spent = (BigDecimal) row[1];
            results.add(new CategoryTotal(category, spent, budget.getTotalBudget()));
        }
        results.sort((a, b) -> b.getSpent().compareTo(a.getSpent()));
        return results;
    }

    @Transactional(readOnly = true)
    public PaymentSummary buildPaymentSummary(Long budgetId) {
        BigDecimal totalPaid = expenseRepository.sumAmountByBudgetIdAndPaid(budgetId, true);
        BigDecimal totalUnpaid = expenseRepository.sumAmountByBudgetIdAndPaid(budgetId, false);
        long paidCount = expenseRepository.countByBudgetIdAndPaid(budgetId, true);
        long unpaidCount = expenseRepository.countByBudgetIdAndPaid(budgetId, false);
        return new PaymentSummary(totalPaid, totalUnpaid, paidCount, unpaidCount);
    }
}
