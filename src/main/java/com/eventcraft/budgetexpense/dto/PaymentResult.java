package com.eventcraft.budgetexpense.dto;

/** Outcome of a call to the sandbox payment gateway. */
public record PaymentResult(boolean approved, String message, String reference, String cardLast4) {

    public static PaymentResult approved(String reference, String cardLast4) {
        return new PaymentResult(true, "Payment approved.", reference, cardLast4);
    }

    public static PaymentResult declined(String reason) {
        return new PaymentResult(false, reason, null, null);
    }
}
