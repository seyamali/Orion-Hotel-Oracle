package com.orionhotel.model;

public class Room {
    private int roomNumber;
    private String type; // Single, Double, Suite
    private boolean isOccupied;
    private boolean isClean;
    private boolean needsMaintenance;

    // Constructor
    public Room(int roomNumber, String type) {
        this.roomNumber = roomNumber;
        this.type = type;
        this.isOccupied = false;
        this.isClean = true;
        this.needsMaintenance = false;
    }

    // Getters and Setters
    public int getRoomNumber() { return roomNumber; }
    public String getType() { return type; }
    public boolean isOccupied() { return isOccupied; }
    public boolean isClean() { return isClean; }
    public boolean needsMaintenance() { return needsMaintenance; }

    public void setOccupied(boolean occupied) { isOccupied = occupied; }
    public void setClean(boolean clean) { isClean = clean; }
    public void setNeedsMaintenance(boolean needsMaintenance) { this.needsMaintenance = needsMaintenance; }
}