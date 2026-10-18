package com.hotel.model;

import java.math.BigDecimal;

public record Booking(
        String id,
        String guestName,
        String guestEmail,
        Room room,
        StayPeriod period,
        BigDecimal totalPrice,
        String transactionId,
        BookingStatus status) {

    public boolean isActive() {
        return status == BookingStatus.ACTIVE;
    }

    public Booking cancelled() {
        return new Booking(id, guestName, guestEmail, room, period, totalPrice, transactionId,
                BookingStatus.CANCELLED);
    }
}
