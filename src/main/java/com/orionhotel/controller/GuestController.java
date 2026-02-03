package com.orionhotel.controller;

import com.orionhotel.database.DatabaseConnection;
import com.orionhotel.model.Guest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class GuestController {

    private RoomController roomController;

    public GuestController(RoomController roomController) {
        this.roomController = roomController;
    }

    public void addGuest(Guest guest) {
        String sql = "INSERT INTO guests (full_name, phone, email, id_number, address, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, guest.getFullName());
            pstmt.setString(2, guest.getPhoneNumber());
            pstmt.setString(3, guest.getEmail());
            pstmt.setString(4, guest.getNationalId());
            pstmt.setString(5, guest.getAddress());
            pstmt.setString(6, Guest.GuestStatus.REGISTERED.name());

            pstmt.executeUpdate();
            System.out.println("Guest added to DB: " + guest.getFullName());

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Guest> getAllGuests() {
        List<Guest> list = new ArrayList<>();
        String sql = "SELECT * FROM guests";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapResultSetToGuest(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Guest> searchGuests(String query) {
        List<Guest> list = new ArrayList<>();
        String sql = "SELECT * FROM guests WHERE LOWER(full_name) LIKE ? OR phone LIKE ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + query.toLowerCase() + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToGuest(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Guest> getGuestsByStatus(Guest.GuestStatus status) {
        List<Guest> list = new ArrayList<>();
        String sql = "SELECT * FROM guests WHERE status = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status.name());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToGuest(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean checkInGuest(int guestId, int roomNumber) {
        // 1. Check if guest is valid and registered
        // 2. Book room via RoomController (need to update RoomController to SQL first
        // for transactional safety, but currently just calling method)
        if (roomController.bookRoom(roomNumber)) {
            String sql = "UPDATE guests SET status = 'CHECKED_IN', room_number = ?, check_in_date = ? WHERE guest_id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, roomNumber);
                pstmt.setDate(2, java.sql.Date.valueOf(LocalDate.now())); // Current date
                pstmt.setInt(3, guestId);

                int rows = pstmt.executeUpdate();
                return rows > 0;

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public boolean checkOutGuest(int guestId) {
        // Get guest to find room number
        Guest guest = findGuestById(guestId);
        if (guest != null && guest.getStatus() == Guest.GuestStatus.CHECKED_IN) {

            // Checkout room
            if (guest.getRoomNumber() != null) {
                roomController.checkoutRoom(guest.getRoomNumber());
            }

            // Update guest status
            String sql = "UPDATE guests SET status = 'CHECKED_OUT', check_out_date = ?, room_number = NULL WHERE guest_id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setDate(1, java.sql.Date.valueOf(LocalDate.now()));
                pstmt.setInt(2, guestId);

                int rows = pstmt.executeUpdate();
                return rows > 0;

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    // Find Guest By ID helper
    public Guest findGuestById(int guestId) {
        String sql = "SELECT * FROM guests WHERE guest_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, guestId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToGuest(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Guest mapResultSetToGuest(ResultSet rs) throws SQLException {
        Guest g = new Guest();
        g.setGuestId(rs.getInt("guest_id"));
        g.setFullName(rs.getString("full_name"));
        g.setPhoneNumber(rs.getString("phone"));
        g.setEmail(rs.getString("email"));
        g.setNationalId(rs.getString("id_number"));
        g.setAddress(rs.getString("address"));

        int roomNum = rs.getInt("room_number");
        if (!rs.wasNull()) {
            g.setRoomNumber(roomNum);
        }

        java.sql.Date checkIn = rs.getDate("check_in_date");
        if (checkIn != null)
            g.setCheckInDate(checkIn.toLocalDate());

        java.sql.Date checkOut = rs.getDate("check_out_date");
        if (checkOut != null)
            g.setCheckOutDate(checkOut.toLocalDate());

        String statusStr = rs.getString("status");
        if (statusStr != null) {
            try {
                g.setStatus(Guest.GuestStatus.valueOf(statusStr));
            } catch (IllegalArgumentException e) {
                g.setStatus(Guest.GuestStatus.REGISTERED);
            }
        } else {
            g.setStatus(Guest.GuestStatus.REGISTERED);
        }

        return g;
    }
}