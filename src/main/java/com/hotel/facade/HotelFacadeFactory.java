package com.hotel.facade;

import com.hotel.model.Room;
import com.hotel.model.RoomType;
import com.hotel.subsystem.BookingRepository;
import com.hotel.subsystem.LoyaltyService;
import com.hotel.subsystem.NotificationService;
import com.hotel.subsystem.PaymentProcessor;
import com.hotel.subsystem.PricingService;
import com.hotel.subsystem.RoomInventory;

import java.util.List;

public final class HotelFacadeFactory {

    private HotelFacadeFactory() {
    }

    public static HotelFacades createDemoHotel() {
        RoomInventory inventory = new RoomInventory(List.of(
                new Room(101, RoomType.STANDARD),
                new Room(102, RoomType.STANDARD),
                new Room(201, RoomType.DELUXE),
                new Room(301, RoomType.SUITE)));
        BookingRepository bookings = new BookingRepository();

        HotelBookingFacade bookingFacade = new HotelBookingFacade(
                inventory, new PricingService(), new PaymentProcessor(),
                bookings, new NotificationService(), new LoyaltyService());
        HotelAdminFacade adminFacade = new HotelAdminFacade(bookings, inventory);
        return new HotelFacades(bookingFacade, adminFacade);
    }
}
