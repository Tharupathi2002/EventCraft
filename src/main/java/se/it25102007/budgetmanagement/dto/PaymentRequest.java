package se.it25102007.budgetmanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** Form-backing object for the sandbox payment page. Card details are never persisted. */
public class PaymentRequest {

    @NotBlank(message = "Cardholder name is required")
    private String cardHolderName;

    @NotBlank(message = "Card number is required")
    @Pattern(regexp = "^[0-9 ]{13,19}$", message = "Enter a 13-19 digit card number")
    private String cardNumber;

    @NotBlank(message = "Expiry month is required")
    @Pattern(regexp = "^(0[1-9]|1[0-2])$", message = "Use MM format, e.g. 09")
    private String expiryMonth;

    @NotBlank(message = "Expiry year is required")
    @Pattern(regexp = "^[0-9]{2}$", message = "Use YY format, e.g. 27")
    private String expiryYear;

    @NotBlank(message = "CVV is required")
    @Pattern(regexp = "^[0-9]{3,4}$", message = "Enter a 3-4 digit CVV")
    private String cvv;

    public String getCardHolderName() {
        return cardHolderName;
    }

    public void setCardHolderName(String cardHolderName) {
        this.cardHolderName = cardHolderName;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getExpiryMonth() {
        return expiryMonth;
    }

    public void setExpiryMonth(String expiryMonth) {
        this.expiryMonth = expiryMonth;
    }

    public String getExpiryYear() {
        return expiryYear;
    }

    public void setExpiryYear(String expiryYear) {
        this.expiryYear = expiryYear;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }
}
