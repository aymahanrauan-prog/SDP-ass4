package com.hotel.exception;

public class PaymentDeclinedException extends HotelException {

    public PaymentDeclinedException(String reason) {
        super("Payment declined: " + reason);
    }
}
