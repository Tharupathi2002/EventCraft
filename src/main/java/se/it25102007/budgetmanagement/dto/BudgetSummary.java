package se.it25102007.budgetmanagement.dto;

import se.it25102007.budgetmanagement.model.Budget;

import java.math.BigDecimal;
import java.math.RoundingMode;


public class BudgetSummary {

    private final Budget budget;
    private final BigDecimal totalSpent;
    private final BigDecimal remainingBalance;
    private final int percentUsed;
    private final boolean overBudget;

    public BudgetSummary(Budget budget, BigDecimal totalSpent) {
        this.budget = budget;
        this.totalSpent = totalSpent;
        this.remainingBalance = budget.getTotalBudget().subtract(totalSpent);
        this.overBudget = remainingBalance.compareTo(BigDecimal.ZERO) < 0;

        if (budget.getTotalBudget().compareTo(BigDecimal.ZERO) > 0) {
            this.percentUsed = totalSpent
                    .multiply(BigDecimal.valueOf(100))
                    .divide(budget.getTotalBudget(), 0, RoundingMode.HALF_UP)
                    .intValue();
        } else {
            this.percentUsed = totalSpent.compareTo(BigDecimal.ZERO) > 0 ? 100 : 0;
        }
    }

    public Budget getBudget() {
        return budget;
    }

    public BigDecimal getTotalSpent() {
        return totalSpent;
    }

    public BigDecimal getRemainingBalance() {
        return remainingBalance;
    }

    public int getPercentUsed() {
        return percentUsed;
    }

    public int getPercentUsedCapped() {
        return Math.min(percentUsed, 100);
    }

    public boolean isOverBudget() {
        return overBudget;
    }
}
