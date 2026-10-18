package com.hotel.exception;

public class BookingAlreadyCancelledException extends HotelException {

    public BookingAlreadyCancelledException(String bookingId) {
        super("Booking is already cancelled: " + bookingId);
    }
}
