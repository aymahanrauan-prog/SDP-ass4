package com.hotel.subsystem;

import com.hotel.exception.NoRoomAvailableException;
import com.hotel.model.Room;
import com.hotel.model.RoomType;
import com.hotel.model.StayPeriod;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RoomInventory {

    private final List<Room> rooms;
    private final Map<Integer, List<StayPeriod>> reservationsByRoom = new HashMap<>();

    public RoomInventory(List<Room> rooms) {
        this.rooms = List.copyOf(rooms);
    }

    public boolean isAvailable(RoomType type, StayPeriod period) {
        return findFreeRoom(type, period).isPresent();
    }

    public Room reserveRoom(RoomType type, StayPeriod period) {
        Room room = findFreeRoom(type, period)
                .orElseThrow(() -> new NoRoomAvailableException(type, period));
        reservationsByRoom.computeIfAbsent(room.number(), number -> new ArrayList<>()).add(period);
        System.out.printf("  [Inventory]    Reserved room %d (%s) %s -> %s%n",
                room.number(), room.type(), period.checkIn(), period.checkOut());
        return room;
    }

    public void releaseRoom(Room room, StayPeriod period) {
        List<StayPeriod> reservations = reservationsByRoom.get(room.number());
        if (reservations != null && reservations.remove(period)) {
            System.out.printf("  [Inventory]    Released room %d (%s)%n", room.number(), room.type());
        }
    }

    public int occupiedRoomsOn(LocalDate night) {
        return (int) rooms.stream()
                .filter(room -> reservationsOf(room).stream().anyMatch(period -> period.includesNight(night)))
                .count();
    }

    public int totalRooms() {
        return rooms.size();
    }

    private Optional<Room> findFreeRoom(RoomType type, StayPeriod period) {
        return rooms.stream()
                .filter(room -> room.type() == type)
                .filter(room -> isFree(room, period))
                .findFirst();
    }

    private boolean isFree(Room room, StayPeriod period) {
        return reservationsOf(room).stream().noneMatch(period::overlaps);
    }

    private List<StayPeriod> reservationsOf(Room room) {
        return reservationsByRoom.getOrDefault(room.number(), List.of());
    }
}
