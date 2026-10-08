package com.eventcraft.budgetexpense.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * What the "Add Expense" form submits: vendor and an editable amount.
 * The description and category come from the vendor record and date from the event.
 */
public class ExpenseForm {

    @NotNull(message = "Please choose a vendor")
    private Long vendorId;

    @NotNull(message = "Please enter an amount")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    public Long getVendorId() { return vendorId; }
    public void setVendorId(Long vendorId) { this.vendorId = vendorId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
