package se.it25102007.budgetmanagement.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;


public class CategoryTotal {

    private final String category;
    private final BigDecimal spent;
    private final int percentOfBudget;

    public CategoryTotal(String category, BigDecimal spent, BigDecimal totalBudget) {
        this.category = category;
        this.spent = spent;
        if (totalBudget != null && totalBudget.compareTo(BigDecimal.ZERO) > 0) {
            this.percentOfBudget = spent
                    .multiply(BigDecimal.valueOf(100))
                    .divide(totalBudget, 0, RoundingMode.HALF_UP)
                    .intValue();
        } else {
            this.percentOfBudget = 0;
        }
    }

    public String getCategory() {
        return category;
    }

    public BigDecimal getSpent() {
        return spent;
    }

    public int getPercentOfBudget() {
        return percentOfBudget;
    }
}
