package com.orionhotel.controller;

import com.orionhotel.model.Guest;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GuestController {

    private static final String DATA_FILE = "guest_db.ser";

    // Past stay record for history
    public static class PastStay implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        public final int guestId;
        public final String guestName;
        public final int roomNumber;
        public final LocalDate checkInDate;
        public final LocalDate checkOutDate;

        public PastStay(int guestId, String guestName, int roomNumber, LocalDate checkInDate, LocalDate checkOutDate) {
            this.guestId = guestId;
            this.guestName = guestName;
            this.roomNumber = roomNumber;
            this.checkInDate = checkInDate;
            this.checkOutDate = checkOutDate;
        }
    }

    private List<Guest> guests = new ArrayList<>();
    private List<PastStay> pastStays = new ArrayList<>();
    private RoomController roomController;

    public GuestController(RoomController roomController) {
        this.roomController = roomController;
        loadData();
    }

    // Add new guest
    public void addGuest(Guest guest) {
        guests.add(guest);
        saveData();
    }

    // Get all guests
    public List<Guest> getAllGuests() {
        return new ArrayList<>(guests);
    }

    // Search guests by name or phone
    public List<Guest> searchGuests(String query) {
        String lowerQuery = query.toLowerCase();
        return guests.stream()
                .filter(g -> g.getFullName().toLowerCase().contains(lowerQuery) ||
                             g.getPhoneNumber().contains(lowerQuery))
                .collect(Collectors.toList());
    }

    // Filter by status
    public List<Guest> getGuestsByStatus(Guest.GuestStatus status) {
        return guests.stream()
                .filter(g -> g.getStatus() == status)
                .collect(Collectors.toList());
    }

    // Check-in guest
    public boolean checkInGuest(int guestId, int roomNumber) {
        Guest guest = findGuestById(guestId);
        if (guest != null && guest.getStatus() == Guest.GuestStatus.REGISTERED) {
            if (roomController.bookRoom(roomNumber)) {
                guest.setStatus(Guest.GuestStatus.CHECKED_IN);
                guest.setRoomNumber(roomNumber);
                guest.setCheckInDate(LocalDate.now());
                saveData();
                return true;
            }
        }
        return false;
    }

    // Check-out guest
    public boolean checkOutGuest(int guestId) {
        Guest guest = findGuestById(guestId);
        if (guest != null && guest.getStatus() == Guest.GuestStatus.CHECKED_IN) {
            guest.setStatus(Guest.GuestStatus.CHECKED_OUT);
            guest.setCheckOutDate(LocalDate.now());
            // Record past stay
            pastStays.add(new PastStay(guest.getGuestId(), guest.getFullName(), guest.getRoomNumber(),
                    guest.getCheckInDate(), guest.getCheckOutDate()));
            // Checkout room
            roomController.checkoutRoom(guest.getRoomNumber());
            guest.setRoomNumber(null); // Clear room assignment
            saveData();
            return true;
        }
        return false;
    }

    // Get guest history
    public List<PastStay> getGuestHistory(int guestId) {
        return pastStays.stream()
                .filter(s -> s.guestId == guestId)
                .collect(Collectors.toList());
    }

    // Reports
    public List<Guest> getCheckedInGuests() {
        return getGuestsByStatus(Guest.GuestStatus.CHECKED_IN);
    }

    public List<Guest> getTodaysArrivals() {
        LocalDate today = LocalDate.now();
        return guests.stream()
                .filter(g -> g.getCheckInDate() != null && g.getCheckInDate().equals(today))
                .collect(Collectors.toList());
    }

    public List<Guest> getTodaysDepartures() {
        LocalDate today = LocalDate.now();
        return guests.stream()
                .filter(g -> g.getCheckOutDate() != null && g.getCheckOutDate().equals(today))
                .collect(Collectors.toList());
    }

    // Frequent guests (most stays)
    public List<GuestFrequency> getFrequentGuests() {
        java.util.Map<Integer, Integer> stayCounts = new java.util.HashMap<>();
        for (PastStay stay : pastStays) {
            stayCounts.put(stay.guestId, stayCounts.getOrDefault(stay.guestId, 0) + 1);
        }
        List<GuestFrequency> frequencies = new ArrayList<>();
        for (Guest guest : guests) {
            int count = stayCounts.getOrDefault(guest.getGuestId(), 0);
            frequencies.add(new GuestFrequency(guest, count));
        }
        frequencies.sort((a, b) -> Integer.compare(b.stayCount, a.stayCount));
        return frequencies;
    }

    public static class GuestFrequency {
        public final Guest guest;
        public final int stayCount;

        public GuestFrequency(Guest guest, int stayCount) {
            this.guest = guest;
            this.stayCount = stayCount;
        }
    }

    private Guest findGuestById(int guestId) {
        return guests.stream()
                .filter(g -> g.getGuestId() == guestId)
                .findFirst()
                .orElse(null);
    }

    @SuppressWarnings("unchecked")
    private void loadData() {
        java.io.File file = new java.io.File(DATA_FILE);
        if (file.exists()) {
            try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(new java.io.FileInputStream(file))) {
                guests = (List<Guest>) ois.readObject();
                pastStays = (List<PastStay>) ois.readObject();
                System.out.println("Guest data loaded successfully from " + DATA_FILE);
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Failed to load guest data: " + e.getMessage());
            }
        } else {
            System.out.println("No existing guest data file found. Starting fresh.");
        }
    }

    private void saveData() {
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(new java.io.FileOutputStream(DATA_FILE))) {
            oos.writeObject(guests);
            oos.writeObject(pastStays);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to save guest data: " + e.getMessage());
        }
    }
}