package com.orionhotel.model;

import java.time.LocalDate;

import java.io.Serializable;

public class Reservation implements Serializable {
    private static final long serialVersionUID = 1L;
    private int reservationId;
    private String guestName;
    private String phone;
    private String email;
    private String roomType;
    private Integer roomNumber; // null if not assigned
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private int numberOfGuests;
    private Status status;

    public enum Status {
        PENDING, CONFIRMED, CANCELLED, COMPLETED
    }

    public Reservation() {
    }

    public Reservation(int reservationId, String guestName, String phone, String email,
            String roomType, LocalDate checkInDate, LocalDate checkOutDate, int numberOfGuests) {
        this.reservationId = reservationId;
        this.guestName = guestName;
        this.phone = phone;
        this.email = email;
        this.roomType = roomType;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.numberOfGuests = numberOfGuests;
        this.status = Status.PENDING;
        this.roomNumber = null;
    }

    // Getters and Setters
    public int getReservationId() {
        return reservationId;
    }

    public void setReservationId(int reservationId) {
        this.reservationId = reservationId;
    }

    public String getGuestName() {
        return guestName;
    }

    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public Integer getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(Integer roomNumber) {
        this.roomNumber = roomNumber;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDate checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public int getNumberOfGuests() {
        return numberOfGuests;
    }

    public void setNumberOfGuests(int numberOfGuests) {
        this.numberOfGuests = numberOfGuests;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}