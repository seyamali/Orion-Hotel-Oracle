package com.orionhotel.model;

import java.time.LocalDate;

public class Guest implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private int guestId;
    private String fullName;
    private String phoneNumber;
    private String email;
    private String nationalId; // Sensitive - mask when displaying
    private String address;
    private Integer roomNumber; // Nullable for registered but not checked-in
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private GuestStatus status;

    public enum GuestStatus {
        REGISTERED, CHECKED_IN, CHECKED_OUT
    }

    // Constructor for new guest
    public Guest(int guestId, String fullName, String phoneNumber, String email, String nationalId, String address) {
        this.guestId = guestId;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.nationalId = nationalId;
        this.address = address;
        this.status = GuestStatus.REGISTERED;
    }

    // Getters and Setters
    public int getGuestId() { return guestId; }
    public void setGuestId(int guestId) { this.guestId = guestId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNationalId() { return nationalId; }
    public void setNationalId(String nationalId) { this.nationalId = nationalId; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Integer getRoomNumber() { return roomNumber; }
    public void setRoomNumber(Integer roomNumber) { this.roomNumber = roomNumber; }

    public LocalDate getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDate checkInDate) { this.checkInDate = checkInDate; }

    public LocalDate getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(LocalDate checkOutDate) { this.checkOutDate = checkOutDate; }

    public GuestStatus getStatus() { return status; }
    public void setStatus(GuestStatus status) { this.status = status; }

    // Utility method to get masked national ID
    public String getMaskedNationalId() {
        if (nationalId == null || nationalId.length() < 4) return "****";
        return "****" + nationalId.substring(nationalId.length() - 4);
    }

    @Override
    public String toString() {
        return "Guest{" +
                "guestId=" + guestId +
                ", fullName='" + fullName + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", email='" + email + '\'' +
                ", nationalId='" + getMaskedNationalId() + '\'' +
                ", address='" + address + '\'' +
                ", roomNumber=" + roomNumber +
                ", checkInDate=" + checkInDate +
                ", checkOutDate=" + checkOutDate +
                ", status=" + status +
                '}';
    }
}