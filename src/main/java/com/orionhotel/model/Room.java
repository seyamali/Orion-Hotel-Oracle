package com.orionhotel.model;

import java.io.Serializable;

public class Room implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum RoomStatus {
        AVAILABLE, OCCUPIED, DIRTY, MAINTENANCE
    }

    private int roomNumber;
    private String type; // Single, Double, Suite
    private double price; // Added to match DB
    private RoomStatus status;

    public Room() {
    }

    public Room(int roomNumber, String type, double price) {
        this.roomNumber = roomNumber;
        this.type = type;
        this.price = price;
        this.status = RoomStatus.AVAILABLE;
    }

    // Adapters for legacy byte code calling boolean methods
    public boolean isOccupied() {
        return status == RoomStatus.OCCUPIED;
    }

    public boolean isClean() {
        return status == RoomStatus.AVAILABLE;
    }
    // Logic note: Dirty room is not clean. Occupied room might be clean? Assumed
    // occupied.
    // Legacy isClean meant "Ready for checkin". AVAILABLE is ready.

    public boolean needsMaintenance() {
        return status == RoomStatus.MAINTENANCE;
    }

    public void setOccupied(boolean occupied) {
        if (occupied)
            status = RoomStatus.OCCUPIED;
        else
            status = RoomStatus.DIRTY; // usually free -> dirty
    }

    public void setClean(boolean clean) {
        if (clean)
            status = RoomStatus.AVAILABLE;
        else
            status = RoomStatus.DIRTY;
    }

    public void setNeedsMaintenance(boolean maintenance) {
        if (maintenance)
            status = RoomStatus.MAINTENANCE;
        else
            status = RoomStatus.AVAILABLE; // fixed -> available
    }

    // Getters and Setters
    public int getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(int roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public RoomStatus getStatus() {
        return status;
    }

    public void setStatus(RoomStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return String.format("Room %d (%s) - $%.2f", roomNumber, type, price);
    }
}