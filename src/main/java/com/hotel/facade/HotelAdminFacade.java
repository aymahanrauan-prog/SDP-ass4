package com.hotel.facade;

import com.hotel.subsystem.BookingRepository;
import com.hotel.subsystem.RoomInventory;

import java.time.LocalDate;
import java.util.List;


public class HotelAdminFacade {

    private final BookingRepository bookings;
    private final RoomInventory inventory;

    HotelAdminFacade(BookingRepository bookings, RoomInventory inventory) {
        this.bookings = bookings;
        this.inventory = inventory;
    }

    public List<BookingSummary> findBookingsOf(String guestEmail) {
        return bookings.findByGuestEmail(guestEmail).stream()
                .map(BookingSummary::from)
                .toList();
    }

    public String occupancyReport(LocalDate night) {
        return String.format("%s: %d of %d rooms occupied",
                night, inventory.occupiedRoomsOn(night), inventory.totalRooms());
    }
}
