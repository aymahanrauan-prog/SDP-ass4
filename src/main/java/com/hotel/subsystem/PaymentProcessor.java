package com.hotel.subsystem;

import com.hotel.exception.PaymentDeclinedException;
import com.hotel.exception.RefundFailedException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class PaymentProcessor {

    private final Map<String, BigDecimal> activeCharges = new HashMap<>();
    private int nextTransactionNumber = 5001;

    public String charge(String paymentToken, BigDecimal amount) {
        if (paymentToken == null || paymentToken.isBlank()) {
            throw new PaymentDeclinedException("payment token is missing");
        }
        if (paymentToken.startsWith("DECLINED")) {
            throw new PaymentDeclinedException("the bank rejected the card");
        }
        String transactionId = "TX-" + nextTransactionNumber++;
        activeCharges.put(transactionId, amount);
        System.out.printf("  [Payment]      Charged %s USD (%s)%n", amount, transactionId);
        return transactionId;
    }

    public void refund(String transactionId) {
        BigDecimal amount = activeCharges.remove(transactionId);
        if (amount == null) {
            throw new RefundFailedException(transactionId);
        }
        System.out.printf("  [Payment]      Refunded %s USD (%s)%n", amount, transactionId);
    }
}
