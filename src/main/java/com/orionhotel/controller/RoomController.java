package com.orionhotel.controller;

import com.orionhotel.model.Room;
import java.util.ArrayList;
import java.util.List;

public class RoomController {
    private List<Room> rooms;

    public RoomController() {
        rooms = new ArrayList<>();
    }

    // Add a room to the system
    public void addRoom(Room room) {
        rooms.add(room);
    }

    // Find available rooms
    public List<Room> getAvailableRooms() {
        List<Room> available = new ArrayList<>();
        for(Room r : rooms) {
            if(!r.isOccupied() && r.isClean() && !r.needsMaintenance())
                available.add(r);
        }
        return available;
    }

    // Book a room
    public boolean bookRoom(int roomNumber) {
        for(Room r : rooms) {
            if(r.getRoomNumber() == roomNumber && !r.isOccupied() && r.isClean()) {
                r.setOccupied(true);
                return true;
            }
        }
        return false; // Room not available
    }

    // Checkout a room
    public boolean checkoutRoom(int roomNumber) {
        for(Room r : rooms) {
            if(r.getRoomNumber() == roomNumber && r.isOccupied()) {
                r.setOccupied(false);
                r.setClean(false); // Needs cleaning
                return true;
            }
        }
        return false; // Room not occupied
    }

    // Mark room as cleaned
    public void markCleaned(int roomNumber) {
        for(Room r : rooms) {
            if(r.getRoomNumber() == roomNumber) {
                r.setClean(true);
                break;
            }
        }
    }

    // Mark room for maintenance
    public void markMaintenance(int roomNumber) {
        for(Room r : rooms) {
            if(r.getRoomNumber() == roomNumber) {
                r.setNeedsMaintenance(true);
                break;
            }
        }
    }

    // Get all rooms
    public List<Room> getAllRooms() {
        return rooms;
    }
}