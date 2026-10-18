package com.hotel.exception;

import com.hotel.model.RoomType;
import com.hotel.model.StayPeriod;

public class NoRoomAvailableException extends HotelException {

    public NoRoomAvailableException(RoomType type, StayPeriod period) {
        super("No " + type + " room available from " + period.checkIn() + " to " + period.checkOut());
    }
}
