package se.it25102007.budgetmanagement.dto;

import java.math.BigDecimal;

/** Paid-vs-unpaid totals for a budget (bonus sandbox payment feature). */
public class PaymentSummary {

    private final BigDecimal totalPaid;
    private final BigDecimal totalUnpaid;
    private final long paidCount;
    private final long unpaidCount;

    public PaymentSummary(BigDecimal totalPaid, BigDecimal totalUnpaid, long paidCount, long unpaidCount) {
        this.totalPaid = totalPaid;
        this.totalUnpaid = totalUnpaid;
        this.paidCount = paidCount;
        this.unpaidCount = unpaidCount;
    }

    public BigDecimal getTotalPaid() {
        return totalPaid;
    }

    public BigDecimal getTotalUnpaid() {
        return totalUnpaid;
    }

    public long getPaidCount() {
        return paidCount;
    }

    public long getUnpaidCount() {
        return unpaidCount;
    }
}
