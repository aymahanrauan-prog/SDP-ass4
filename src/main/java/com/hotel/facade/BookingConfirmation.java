package com.hotel.facade;

import java.math.BigDecimal;

public record BookingConfirmation(
        String bookingId,
        int roomNumber,
        BigDecimal totalPrice,
        boolean confirmationEmailSent) {
}
