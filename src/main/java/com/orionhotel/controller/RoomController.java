package com.orionhotel.controller;

import com.orionhotel.database.DatabaseConnection;
import com.orionhotel.model.Room;
import com.orionhotel.model.Room.RoomStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class RoomController {

    public RoomController() {
        // No sample data seeding
    }

    // Add a room to the system
    public void addRoom(Room room) {
        String sql = "INSERT INTO rooms (room_number, type, price, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, room.getRoomNumber());
            pstmt.setString(2, room.getType());
            pstmt.setDouble(3, room.getPrice());
            pstmt.setString(4, room.getStatus().name());

            pstmt.executeUpdate();
            System.out.println("Room added to DB: " + room.getRoomNumber());

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Find available rooms
    public List<Room> getAvailableRooms() {
        List<Room> list = new ArrayList<>();
        String sql = "SELECT * FROM rooms WHERE status = 'AVAILABLE'";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapResultSetToRoom(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Book a room
    public boolean bookRoom(int roomNumber) {
        // Check if available first
        String query = "SELECT status FROM rooms WHERE room_number = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, roomNumber);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String status = rs.getString("status");
                    if ("AVAILABLE".equals(status)) {
                        updateRoomStatus(roomNumber, RoomStatus.OCCUPIED);
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Checkout a room
    public boolean checkoutRoom(int roomNumber) {
        // Should mark as DIRTY after checkout
        updateRoomStatus(roomNumber, RoomStatus.DIRTY);
        return true;
    }

    // Mark room as cleaned
    public void markCleaned(int roomNumber) {
        updateRoomStatus(roomNumber, RoomStatus.AVAILABLE);
    }

    // Mark room for maintenance
    public void markMaintenance(int roomNumber) {
        updateRoomStatus(roomNumber, RoomStatus.MAINTENANCE);
    }

    // Get all rooms
    public List<Room> getAllRooms() {
        List<Room> list = new ArrayList<>();
        String sql = "SELECT * FROM rooms";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapResultSetToRoom(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Room getRoom(int roomNumber) {
        String sql = "SELECT * FROM rooms WHERE room_number = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, roomNumber);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToRoom(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void updateRoomStatus(int roomNumber, RoomStatus status) {
        String sql = "UPDATE rooms SET status = ? WHERE room_number = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status.name());
            pstmt.setInt(2, roomNumber);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public java.util.Map<RoomStatus, Long> getRoomStatusCounts() {
        return getAllRooms().stream()
                .collect(java.util.stream.Collectors.groupingBy(Room::getStatus,
                        java.util.stream.Collectors.counting()));
    }

    private Room mapResultSetToRoom(ResultSet rs) throws SQLException {
        Room r = new Room();
        r.setRoomNumber(rs.getInt("room_number"));
        r.setType(rs.getString("type"));
        r.setPrice(rs.getDouble("price"));

        String statusStr = rs.getString("status");
        try {
            r.setStatus(RoomStatus.valueOf(statusStr));
        } catch (Exception e) {
            r.setStatus(RoomStatus.AVAILABLE);
        }
        return r;
    }
}