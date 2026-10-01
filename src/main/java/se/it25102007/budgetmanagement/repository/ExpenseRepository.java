package se.it25102007.budgetmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import se.it25102007.budgetmanagement.model.Expense;

import java.math.BigDecimal;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByBudgetIdOrderByExpenseDateDesc(Long budgetId);

    List<Expense> findByBudgetIdAndPaidOrderByExpenseDateAsc(Long budgetId, boolean paid);

    List<Expense> findByBudgetIdAndCategoryOrderByExpenseDateDesc(Long budgetId, String category);

    List<Expense> findByBudgetIdAndDescriptionContainingIgnoreCaseOrderByExpenseDateDesc(
            Long budgetId, String keyword);

    long countByBudgetIdAndPaid(Long budgetId, boolean paid);

    @Query("select coalesce(sum(e.amount), 0) from Expense e where e.budget.id = :budgetId")
    BigDecimal sumAmountByBudgetId(@Param("budgetId") Long budgetId);

    @Query("select coalesce(sum(e.amount), 0) from Expense e where e.budget.id = :budgetId and e.paid = :paid")
    BigDecimal sumAmountByBudgetIdAndPaid(@Param("budgetId") Long budgetId, @Param("paid") boolean paid);

    @Query("select e.category, coalesce(sum(e.amount), 0) from Expense e where e.budget.id = :budgetId group by e.category")
    List<Object[]> sumAmountGroupedByCategory(@Param("budgetId") Long budgetId);
}
