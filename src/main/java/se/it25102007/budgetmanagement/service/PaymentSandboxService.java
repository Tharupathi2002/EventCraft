package se.it25102007.budgetmanagement.service;

import org.springframework.stereotype.Service;
import se.it25102007.budgetmanagement.dto.PaymentRequest;
import se.it25102007.budgetmanagement.dto.PaymentResult;

import java.time.YearMonth;
import java.util.UUID;

/**
 * A fully self-contained SANDBOX / TEST-MODE payment gateway. No network
 * calls are made and no real payment processor is involved. This is a
 * bonus feature layered on top of the ER diagram (the diagram's own
 * Payment entity is attached to Booking, not Expenses).
 *
 * Test-card behaviour mirrors the convention real gateways like Stripe use:
 *   - card number ending in "0002" -> always DECLINED
 *   - any other well-formed card    -> always APPROVED
 *   - expiry date in the past       -> always DECLINED
 */
@Service
public class PaymentSandboxService {

    private static final String TEST_DECLINE_SUFFIX = "0002";

    public PaymentResult processPayment(PaymentRequest request) {
        String digitsOnly = request.getCardNumber().replaceAll("\\s", "");

        if (!passesLuhnCheck(digitsOnly)) {
            return PaymentResult.declined("Card number failed validation .");
        }

        if (isExpired(request.getExpiryMonth(), request.getExpiryYear())) {
            return PaymentResult.declined("Card has expired.");
        }

        if (digitsOnly.endsWith(TEST_DECLINE_SUFFIX)) {
            return PaymentResult.declined(
                    "Sandbox gateway declined this test card (ends in " + TEST_DECLINE_SUFFIX + ").");
        }

        String reference = "SANDBOX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String last4 = digitsOnly.substring(digitsOnly.length() - 4);
        return PaymentResult.approved(reference, last4);
    }

    private boolean isExpired(String month, String year) {
        try {
            int mm = Integer.parseInt(month);
            int yy = Integer.parseInt(year) + 2000;
            YearMonth expiry = YearMonth.of(yy, mm);
            return expiry.isBefore(YearMonth.now());
        } catch (NumberFormatException ex) {
            return true;
        }
    }

    private boolean passesLuhnCheck(String digitsOnly) {
        if (!digitsOnly.matches("\\d{13,19}")) {
            return false;
        }
        int sum = 0;
        boolean doubleDigit = false;
        for (int i = digitsOnly.length() - 1; i >= 0; i--) {
            int digit = digitsOnly.charAt(i) - '0';
            if (doubleDigit) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
            doubleDigit = !doubleDigit;
        }
        return sum % 10 == 0;
    }
}
