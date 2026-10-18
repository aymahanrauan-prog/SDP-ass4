package com.hotel.subsystem;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class LoyaltyService {

    private static final int CURRENCY_UNITS_PER_POINT = 10;
    private static final int SILVER_THRESHOLD = 50;
    private static final int GOLD_THRESHOLD = 200;

    private final Map<String, Integer> pointsByGuest = new HashMap<>();

    public int discountPercentFor(String guestEmail) {
        int points = pointsOf(guestEmail);
        if (points >= GOLD_THRESHOLD) {
            return 10;
        }
        if (points >= SILVER_THRESHOLD) {
            return 5;
        }
        return 0;
    }

    public void addPoints(String guestEmail, BigDecimal amountPaid) {
        int earned = pointsFor(amountPaid);
        pointsByGuest.merge(key(guestEmail), earned, Integer::sum);
        System.out.printf("  [Loyalty]      +%d points for %s%n", earned, guestEmail);
    }

    public void deductPoints(String guestEmail, BigDecimal amountRefunded) {
        int revoked = pointsFor(amountRefunded);
        pointsByGuest.merge(key(guestEmail), -revoked, (current, delta) -> Math.max(0, current + delta));
        System.out.printf("  [Loyalty]      -%d points for %s%n", revoked, guestEmail);
    }

    public int pointsOf(String guestEmail) {
        return pointsByGuest.getOrDefault(key(guestEmail), 0);
    }

    private int pointsFor(BigDecimal amount) {
        return amount.intValue() / CURRENCY_UNITS_PER_POINT;
    }

    private String key(String guestEmail) {
        return guestEmail.toLowerCase();
    }
}
