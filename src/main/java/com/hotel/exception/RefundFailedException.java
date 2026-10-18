package com.hotel.exception;

public class RefundFailedException extends HotelException {

    public RefundFailedException(String transactionId) {
        super("Refund failed: unknown or already refunded transaction " + transactionId);
    }
}
