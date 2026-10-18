package com.hotel.subsystem;

import com.hotel.exception.NotificationFailedException;
import com.hotel.model.Booking;

public class NotificationService {

    public void sendConfirmation(Booking booking) {
        requireValidEmail(booking.guestEmail());
        System.out.printf("  [Notification] Confirmation for %s sent to %s%n",
                booking.id(), booking.guestEmail());
    }

    public void sendCancellation(Booking booking) {
        requireValidEmail(booking.guestEmail());
        System.out.printf("  [Notification] Cancellation notice for %s sent to %s%n",
                booking.id(), booking.guestEmail());
    }

    private void requireValidEmail(String email) {
        if (email == null || !email.contains("@")) {
            throw new NotificationFailedException("Invalid e-mail address: " + email);
        }
    }
}
