package com.hotel.subsystem;

import com.hotel.model.RoomType;
import com.hotel.model.StayPeriod;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.Map;

public class PricingService {

    private static final BigDecimal WEEKEND_MULTIPLIER = new BigDecimal("1.20");
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    private final Map<RoomType, BigDecimal> nightlyRates = new EnumMap<>(RoomType.class);

    public PricingService() {
        nightlyRates.put(RoomType.STANDARD, new BigDecimal("80.00"));
        nightlyRates.put(RoomType.DELUXE, new BigDecimal("130.00"));
        nightlyRates.put(RoomType.SUITE, new BigDecimal("250.00"));
    }

    public BigDecimal calculateTotal(RoomType type, StayPeriod period, int discountPercent) {
        if (discountPercent < 0 || discountPercent > 100) {
            throw new IllegalArgumentException("Discount must be between 0 and 100 percent");
        }
        BigDecimal baseRate = nightlyRates.get(type);
        BigDecimal undiscounted = period.checkIn().datesUntil(period.checkOut())
                .map(night -> rateForNight(baseRate, night))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return applyDiscount(undiscounted, discountPercent);
    }

    private BigDecimal rateForNight(BigDecimal baseRate, LocalDate night) {
        return isWeekendNight(night) ? baseRate.multiply(WEEKEND_MULTIPLIER) : baseRate;
    }

    private boolean isWeekendNight(LocalDate night) {
        DayOfWeek day = night.getDayOfWeek();
        return day == DayOfWeek.FRIDAY || day == DayOfWeek.SATURDAY;
    }

    private BigDecimal applyDiscount(BigDecimal amount, int discountPercent) {
        return amount.multiply(BigDecimal.valueOf(100L - discountPercent))
                .divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);
    }
}
