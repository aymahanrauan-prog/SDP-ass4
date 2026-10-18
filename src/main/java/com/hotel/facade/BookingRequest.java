package com.hotel.facade;

import com.hotel.model.RoomType;

import java.time.LocalDate;

public record BookingRequest(
        String guestName,
        String guestEmail,
        RoomType roomType,
        LocalDate checkIn,
        LocalDate checkOut,
        String paymentToken) {
}
