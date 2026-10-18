package com.hotel;

import com.hotel.facade.BookingConfirmation;
import com.hotel.facade.BookingFailedException;
import com.hotel.facade.BookingRequest;
import com.hotel.facade.HotelAdminFacade;
import com.hotel.facade.HotelBookingFacade;
import com.hotel.facade.HotelFacadeFactory;
import com.hotel.facade.HotelFacades;
import com.hotel.model.RoomType;

import java.time.LocalDate;
import java.util.Optional;

public class Main {

    private static final String VALID_CARD = "tok_valid_4242";
    private static final String DECLINED_CARD = "DECLINED_0002";

    public static void main(String[] args) {
        HotelFacades hotel = HotelFacadeFactory.createDemoHotel();
        HotelBookingFacade booking = hotel.booking();
        HotelAdminFacade admin = hotel.admin();

        LocalDate arrival = LocalDate.now().plusDays(14);
        LocalDate departure = arrival.plusDays(4);

        section("1. Check availability");
        System.out.println("Suite available: " + booking.hasAvailability(RoomType.SUITE, arrival, departure));

        section("2. Successful booking (suite)");
        BookingConfirmation daniyarsSuite = book(booking, new BookingRequest(
                "Daniyar Sarsenov", "daniyar@example.com", RoomType.SUITE, arrival, departure, VALID_CARD))
                .orElseThrow();

        section("3. Same suite, same dates -> no room left");
        book(booking, new BookingRequest(
                "Madina Omar", "madina@example.com", RoomType.SUITE, arrival, departure, VALID_CARD));

        section("4. Declined card -> room must be released again");
        book(booking, new BookingRequest(
                "Aigerim Nurlan", "aigerim@example.com", RoomType.STANDARD, arrival, departure, DECLINED_CARD));
        System.out.println("Occupancy: " + admin.occupancyReport(arrival) + "  (only the suite)");

        section("5. Loyalty: two bookings earn points, the third gets a discount");
        book(booking, new BookingRequest(
                "Aigerim Nurlan", "aigerim@example.com", RoomType.STANDARD, arrival, departure, VALID_CARD));
        book(booking, new BookingRequest(
                "Aigerim Nurlan", "aigerim@example.com", RoomType.DELUXE, arrival, departure, VALID_CARD));
        book(booking, new BookingRequest(
                "Aigerim Nurlan", "aigerim@example.com", RoomType.STANDARD,
                departure.plusDays(1), departure.plusDays(5), VALID_CARD));

        section("6. Bad e-mail address -> booking still succeeds, e-mail is not sent");
        book(booking, new BookingRequest(
                "Test Guest", "not-an-email", RoomType.STANDARD, arrival, departure, VALID_CARD));

        section("7. Invalid dates");
        book(booking, new BookingRequest(
                "Test Guest", "test@example.com", RoomType.STANDARD, departure, arrival, VALID_CARD));

        section("8. Cancel the suite booking");
        cancel(booking, daniyarsSuite.bookingId());
        System.out.println("Suite available again: " + booking.hasAvailability(RoomType.SUITE, arrival, departure));
        cancel(booking, daniyarsSuite.bookingId());

        section("9. Admin facade");
        System.out.println(admin.occupancyReport(arrival));
        admin.findBookingsOf("aigerim@example.com").forEach(System.out::println);
    }

    private static Optional<BookingConfirmation> book(HotelBookingFacade facade, BookingRequest request) {
        System.out.println("-> " + request.guestName() + " books " + request.roomType());
        try {
            BookingConfirmation confirmation = facade.bookRoom(request);
            System.out.println("OK: " + confirmation);
            return Optional.of(confirmation);
        } catch (BookingFailedException e) {
            System.out.println("FAILED: " + e.getMessage());
            return Optional.empty();
        }
    }

    private static void cancel(HotelBookingFacade facade, String bookingId) {
        System.out.println("-> cancel " + bookingId);
        try {
            facade.cancelBooking(bookingId);
            System.out.println("OK: cancelled");
        } catch (BookingFailedException e) {
            System.out.println("FAILED: " + e.getMessage());
        }
    }

    private static void section(String title) {
        System.out.println("\n=== " + title + " ===");
    }
}
