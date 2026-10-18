package com.hotel.subsystem;

import com.hotel.exception.BookingAlreadyCancelledException;
import com.hotel.exception.BookingNotFoundException;
import com.hotel.model.Booking;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BookingRepository {

    private final Map<String, Booking> bookingsById = new LinkedHashMap<>();
    private int nextIdNumber = 1001;

    public String nextId() {
        return "BK-" + nextIdNumber++;
    }

    public Booking save(Booking booking) {
        bookingsById.put(booking.id(), booking);
        System.out.printf("  [Repository]   Saved %s (%s)%n", booking.id(), booking.status());
        return booking;
    }

    public Booking findActive(String bookingId) {
        Booking booking = bookingsById.get(bookingId);
        if (booking == null) {
            throw new BookingNotFoundException(bookingId);
        }
        if (!booking.isActive()) {
            throw new BookingAlreadyCancelledException(bookingId);
        }
        return booking;
    }

    public List<Booking> findByGuestEmail(String guestEmail) {
        return bookingsById.values().stream()
                .filter(booking -> booking.guestEmail().equalsIgnoreCase(guestEmail))
                .toList();
    }
}
