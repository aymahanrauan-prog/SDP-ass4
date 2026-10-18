package com.hotel.facade;

import com.hotel.exception.HotelException;
import com.hotel.exception.NoRoomAvailableException;
import com.hotel.exception.NotificationFailedException;
import com.hotel.model.Booking;
import com.hotel.model.BookingStatus;
import com.hotel.model.Room;
import com.hotel.model.RoomType;
import com.hotel.model.StayPeriod;
import com.hotel.subsystem.BookingRepository;
import com.hotel.subsystem.LoyaltyService;
import com.hotel.subsystem.NotificationService;
import com.hotel.subsystem.PaymentProcessor;
import com.hotel.subsystem.PricingService;
import com.hotel.subsystem.RoomInventory;

import java.math.BigDecimal;
import java.time.LocalDate;

public class HotelBookingFacade {

    private final RoomInventory inventory;
    private final PricingService pricing;
    private final PaymentProcessor payments;
    private final BookingRepository bookings;
    private final NotificationService notifications;
    private final LoyaltyService loyalty;

    // Package-private on purpose: subsystem types must not appear in the public API.
    HotelBookingFacade(RoomInventory inventory,
                       PricingService pricing,
                       PaymentProcessor payments,
                       BookingRepository bookings,
                       NotificationService notifications,
                       LoyaltyService loyalty) {
        this.inventory = inventory;
        this.pricing = pricing;
        this.payments = payments;
        this.bookings = bookings;
        this.notifications = notifications;
        this.loyalty = loyalty;
    }

    public boolean hasAvailability(RoomType roomType, LocalDate checkIn, LocalDate checkOut) {
        return inventory.isAvailable(roomType, toStayPeriod(checkIn, checkOut));
    }

    /**
     * Books a room: reserve -> apply loyalty discount -> price -> charge -> save -> award points -> notify.
     * If any critical step fails, the steps already done are undone.
     */
    public BookingConfirmation bookRoom(BookingRequest request) {
        StayPeriod period = toStayPeriod(request.checkIn(), request.checkOut());
        Room room = reserveRoom(request.roomType(), period);
        String transactionId = null;
        try {
            int discountPercent = loyalty.discountPercentFor(request.guestEmail());
            BigDecimal totalPrice = pricing.calculateTotal(request.roomType(), period, discountPercent);
            transactionId = payments.charge(request.paymentToken(), totalPrice);
            Booking booking = bookings.save(new Booking(
                    bookings.nextId(), request.guestName(), request.guestEmail(),
                    room, period, totalPrice, transactionId, BookingStatus.ACTIVE));
            loyalty.addPoints(request.guestEmail(), totalPrice);
            boolean emailSent = sendConfirmationQuietly(booking);
            return new BookingConfirmation(booking.id(), room.number(), totalPrice, emailSent);
        } catch (RuntimeException failure) {
            rollbackBooking(room, period, transactionId);
            throw new BookingFailedException("Booking failed: " + failure.getMessage(), failure);
        }
    }

    /** Cancels a booking: refund -> release room -> mark cancelled -> revoke points -> notify. */
    public void cancelBooking(String bookingId) {
        Booking booking = findActiveBooking(bookingId);
        try {
            payments.refund(booking.transactionId());
            inventory.releaseRoom(booking.room(), booking.period());
            bookings.save(booking.cancelled());
            loyalty.deductPoints(booking.guestEmail(), booking.totalPrice());
        } catch (HotelException failure) {
            throw new BookingFailedException("Cancellation failed: " + failure.getMessage(), failure);
        }
        sendCancellationQuietly(booking);
    }

    private StayPeriod toStayPeriod(LocalDate checkIn, LocalDate checkOut) {
        try {
            return new StayPeriod(checkIn, checkOut);
        } catch (IllegalArgumentException | NullPointerException invalidDates) {
            throw new BookingFailedException("Invalid stay dates: " + invalidDates.getMessage(), invalidDates);
        }
    }

    private Room reserveRoom(RoomType roomType, StayPeriod period) {
        try {
            return inventory.reserveRoom(roomType, period);
        } catch (NoRoomAvailableException noRoom) {
            throw new BookingFailedException("Booking failed: " + noRoom.getMessage(), noRoom);
        }
    }

    private Booking findActiveBooking(String bookingId) {
        try {
            return bookings.findActive(bookingId);
        } catch (HotelException notCancellable) {
            throw new BookingFailedException("Cancellation failed: " + notCancellable.getMessage(), notCancellable);
        }
    }

    private void rollbackBooking(Room room, StayPeriod period, String transactionId) {
        inventory.releaseRoom(room, period);
        if (transactionId != null) {
            payments.refund(transactionId);
        }
    }

    /** A lost e-mail must not undo a paid booking, so the failure is reported, not thrown. */
    private boolean sendConfirmationQuietly(Booking booking) {
        try {
            notifications.sendConfirmation(booking);
            return true;
        } catch (NotificationFailedException emailProblem) {
            return false;
        }
    }

    private void sendCancellationQuietly(Booking booking) {
        try {
            notifications.sendCancellation(booking);
        } catch (NotificationFailedException ignored) {
            // The cancellation itself already succeeded.
        }
    }
}
