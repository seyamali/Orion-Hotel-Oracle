package com.orionhotel.controller;

import com.orionhotel.database.DatabaseConnection;
import com.orionhotel.model.Reservation;
import com.orionhotel.model.Room;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BookingController {

    private RoomController roomController;
    private NotificationController notificationController;

    public BookingController(RoomController roomController) {
        this.roomController = roomController;
    }

    public void setNotificationController(NotificationController nc) {
        this.notificationController = nc;
    }

    public void addReservation(Reservation res) {
        String sql = "INSERT INTO reservations (guest_name, phone, email, room_type, room_number, check_in, check_out, num_guests, status) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, res.getGuestName());
            pstmt.setString(2, res.getPhone());
            pstmt.setString(3, res.getEmail());
            pstmt.setString(4, res.getRoomType());
            if (res.getRoomNumber() != null)
                pstmt.setInt(5, res.getRoomNumber());
            else
                pstmt.setNull(5, Types.INTEGER);
            pstmt.setDate(6, Date.valueOf(res.getCheckInDate()));
            pstmt.setDate(7, Date.valueOf(res.getCheckOutDate()));
            pstmt.setInt(8, res.getNumberOfGuests());
            pstmt.setString(9, res.getStatus().name());

            pstmt.executeUpdate();

            if (notificationController != null) {
                notificationController.addNotification("New Reservation: " + res.getGuestName(),
                        com.orionhotel.model.Notification.TargetRole.RECEPTIONIST);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Reservation> getAllReservations() {
        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT * FROM reservations";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToReservation(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean isRoomAvailable(String roomType, LocalDate checkIn, LocalDate checkOut) {
        // Find if there's any room of this type that isn't reserved during this period
        List<Room> roomsOfType = roomController.getAllRooms().stream()
                .filter(r -> r.getType().equalsIgnoreCase(roomType))
                .toList();

        for (Room room : roomsOfType) {
            if (isSpecificRoomAvailable(room.getRoomNumber(), checkIn, checkOut)) {
                return true;
            }
        }
        return false;
    }

    public boolean isSpecificRoomAvailable(int roomNumber, LocalDate checkIn, LocalDate checkOut) {
        String sql = "SELECT COUNT(*) FROM reservations WHERE room_number = ? " +
                "AND status NOT IN ('CANCELLED', 'COMPLETED') " +
                "AND NOT (check_out <= ? OR check_in >= ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, roomNumber);
            pstmt.setDate(2, Date.valueOf(checkIn));
            pstmt.setDate(3, Date.valueOf(checkOut));

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void confirmReservation(int reservationId) {
        updateStatus(reservationId, Reservation.Status.CONFIRMED);
    }

    public void assignRoom(int reservationId, int roomNumber) {
        String sql = "UPDATE reservations SET room_number = ? WHERE reservation_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, roomNumber);
            pstmt.setInt(2, reservationId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void modifyReservation(int reservationId, LocalDate newCheckIn, LocalDate newCheckOut, String newRoomType) {
        String sql = "UPDATE reservations SET check_in = ?, check_out = ?, room_type = ? WHERE reservation_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, Date.valueOf(newCheckIn));
            pstmt.setDate(2, Date.valueOf(newCheckOut));
            pstmt.setString(3, newRoomType);
            pstmt.setInt(4, reservationId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void cancelReservation(int reservationId) {
        updateStatus(reservationId, Reservation.Status.CANCELLED);
        if (notificationController != null) {
            notificationController.addNotification("Reservation Cancelled: " + reservationId,
                    com.orionhotel.model.Notification.TargetRole.RECEPTIONIST);
        }
    }

    public void checkInFromReservation(int reservationId) {
        updateStatus(reservationId, Reservation.Status.COMPLETED);
    }

    private void updateStatus(int id, Reservation.Status status) {
        String sql = "UPDATE reservations SET status = ? WHERE reservation_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status.name());
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Reservation> getUpcomingReservations() {
        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT * FROM reservations WHERE status != 'CANCELLED' AND check_in > ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, Date.valueOf(LocalDate.now()));
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next())
                    list.add(mapResultSetToReservation(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Reservation> getCancelledReservations() {
        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT * FROM reservations WHERE status = 'CANCELLED'";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next())
                list.add(mapResultSetToReservation(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private Reservation mapResultSetToReservation(ResultSet rs) throws SQLException {
        Reservation res = new Reservation();
        res.setReservationId(rs.getInt("reservation_id"));
        res.setGuestName(rs.getString("guest_name"));
        res.setPhone(rs.getString("phone"));
        res.setEmail(rs.getString("email"));
        res.setRoomType(rs.getString("room_type"));

        int roomNum = rs.getInt("room_number");
        if (!rs.wasNull())
            res.setRoomNumber(roomNum);

        res.setCheckInDate(rs.getDate("check_in").toLocalDate());
        res.setCheckOutDate(rs.getDate("check_out").toLocalDate());
        res.setNumberOfGuests(rs.getInt("num_guests"));
        res.setStatus(Reservation.Status.valueOf(rs.getString("status")));
        return res;
    }
}