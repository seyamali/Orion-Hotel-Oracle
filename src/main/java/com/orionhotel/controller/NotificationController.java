package com.orionhotel.controller;

import com.orionhotel.database.DatabaseConnection;
import com.orionhotel.model.Notification;
import com.orionhotel.model.Role;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationController {

    public NotificationController() {
    }

    public void addNotification(String message, Notification.TargetRole target) {
        String sql = "INSERT INTO notifications (message, target_role, is_read) VALUES (?, ?, FALSE)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, message);
            pstmt.setString(2, target.name());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Notification> getUnreadNotifications(Role userRole) {
        return fetchNotifications(userRole, true);
    }

    public List<Notification> getNotifications(Role userRole) {
        return fetchNotifications(userRole, false);
    }

    private List<Notification> fetchNotifications(Role userRole, boolean onlyUnread) {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE (target_role = 'ALL' OR target_role = ?)";
        if (onlyUnread)
            sql += " AND is_read = FALSE";
        sql += " ORDER BY created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userRole.name());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToNotification(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void markAsRead(int notificationId) {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE notif_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, notificationId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void markAllAsRead(Role userRole) {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE (target_role = 'ALL' OR target_role = ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userRole.name());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void clearAll(Role userRole) {
        String sql = "DELETE FROM notifications WHERE (target_role = 'ALL' OR target_role = ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userRole.name());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Notification mapResultSetToNotification(ResultSet rs) throws SQLException {
        Notification n = new Notification(
                rs.getInt("notif_id"),
                rs.getString("message"),
                Notification.TargetRole.valueOf(rs.getString("target_role")));
        n.setRead(rs.getBoolean("is_read"));
        return n;
    }
}
