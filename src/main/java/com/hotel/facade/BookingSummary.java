package com.hotel.facade;

import com.hotel.model.Booking;
import com.hotel.model.BookingStatus;
import com.hotel.model.RoomType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BookingSummary(
        String bookingId,
        String guestName,
        int roomNumber,
        RoomType roomType,
        LocalDate checkIn,
        LocalDate checkOut,
        BigDecimal totalPrice,
        BookingStatus status) {

    static BookingSummary from(Booking booking) {
        return new BookingSummary(
                booking.id(),
                booking.guestName(),
                booking.room().number(),
                booking.room().type(),
                booking.period().checkIn(),
                booking.period().checkOut(),
                booking.totalPrice(),
                booking.status());
    }
}
