package com.eventcraft.budgetexpense.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Form-backing object for creating/editing a budget. Using a DTO (rather than binding the
 * Budget entity directly) means a posted form can never change which event or host a budget
 * belongs to. eventId is only used on create; on edit the event is fixed.
 */
@Getter
@Setter
@NoArgsConstructor
public class BudgetForm {

    private Long eventId;

    @NotNull(message = "Total budget is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Total budget cannot be negative")
    private BigDecimal totalBudget;
}
