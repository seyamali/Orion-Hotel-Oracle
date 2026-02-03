package com.orionhotel.controller;

import com.orionhotel.database.DatabaseConnection;
import com.orionhotel.model.HousekeepingTask;
import com.orionhotel.model.MaintenanceRequest;
import com.orionhotel.model.Staff;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HousekeepingController {

    private RoomController roomController;
    private StaffController staffController;
    private NotificationController notificationController;

    public HousekeepingController(RoomController roomController, StaffController staffController) {
        this.roomController = roomController;
        this.staffController = staffController;
    }

    public void setNotificationController(NotificationController nc) {
        this.notificationController = nc;
    }

    // --- Housekeeping Tasks ---
    public void createCleaningTask(int roomNumber, HousekeepingTask.TaskType type, Integer staffId) {
        String sql = "INSERT INTO housekeeping_tasks (room_number, type, status, staff_id) VALUES (?, ?, 'PENDING', ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, roomNumber);
            pstmt.setString(2, type.name());
            if (staffId != null)
                pstmt.setInt(3, staffId);
            else
                pstmt.setNull(3, Types.INTEGER);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateTaskStatus(int taskId, HousekeepingTask.Startus newStatus) {
        String sql = "UPDATE housekeeping_tasks SET status = ? WHERE task_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newStatus.name());
            pstmt.setInt(2, taskId);
            pstmt.executeUpdate();

            if (newStatus == HousekeepingTask.Startus.COMPLETED) {
                // Auto mark room clean
                int roomNum = getRoomNumForTask(taskId);
                if (roomNum != -1)
                    roomController.markCleaned(roomNum);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int getRoomNumForTask(int taskId) {
        String sql = "SELECT room_number FROM housekeeping_tasks WHERE task_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, taskId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next())
                    return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public List<HousekeepingTask> getAllTasks() {
        List<HousekeepingTask> list = new ArrayList<>();
        String sql = "SELECT * FROM housekeeping_tasks";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToTask(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // --- Maintenance Requests ---
    public void createMaintenanceRequest(int roomNumber, MaintenanceRequest.IssueType type, String desc,
            MaintenanceRequest.Priority priority) {
        String sql = "INSERT INTO maintenance_requests (room_number, issue_type, description, priority, status) VALUES (?, ?, ?, ?, 'PENDING')";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, roomNumber);
            pstmt.setString(2, type.name());
            pstmt.setString(3, desc);
            pstmt.setString(4, priority.name());
            pstmt.executeUpdate();

            roomController.markMaintenance(roomNumber);

            if (notificationController != null) {
                notificationController.addNotification("Maintenance Needed: Room " + roomNumber,
                        com.orionhotel.model.Notification.TargetRole.MANAGER);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void assignTechnician(int reqId, int staffId) {
        String sql = "UPDATE maintenance_requests SET tech_id = ?, status = 'IN_PROGRESS' WHERE request_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, staffId);
            pstmt.setInt(2, reqId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void completeMaintenance(int reqId) {
        String sql = "UPDATE maintenance_requests SET status = 'FIXED' WHERE request_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, reqId);
            pstmt.executeUpdate();

            // Mark room DIRTY (ready for cleaning)
            int roomNum = getRoomNumForMaint(reqId);
            if (roomNum != -1) {
                // Technically Maintenance usually means it's dirty now
                // My RoomController markCleaned marks it AVAILABLE.
                // We need a 'markDirty' but I can just use a manual update or add it.
                // For now, I'll just clear maintenance state via markCleaned then markDirty if
                // possible
                // Actually, I'll just create a cleaning task.
                createCleaningTask(roomNum, HousekeepingTask.TaskType.CLEANING, null);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int getRoomNumForMaint(int reqId) {
        String sql = "SELECT room_number FROM maintenance_requests WHERE request_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, reqId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next())
                    return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public List<MaintenanceRequest> getAllMaintenance() {
        List<MaintenanceRequest> list = new ArrayList<>();
        String sql = "SELECT * FROM maintenance_requests";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToRequest(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private HousekeepingTask mapResultSetToTask(ResultSet rs) throws SQLException {
        int staffId = rs.getInt("staff_id");
        String staffName = rs.wasNull() ? "Unassigned" : getStaffName(staffId);

        HousekeepingTask t = new HousekeepingTask(
                rs.getInt("task_id"),
                rs.getInt("room_number"),
                HousekeepingTask.TaskType.valueOf(rs.getString("type")),
                rs.wasNull() ? null : staffId,
                staffName);
        t.setStatus(HousekeepingTask.Startus.valueOf(rs.getString("status")));
        return t;
    }

    private MaintenanceRequest mapResultSetToRequest(ResultSet rs) throws SQLException {
        int techId = rs.getInt("tech_id");
        String techName = rs.wasNull() ? "Unassigned" : getStaffName(techId);

        MaintenanceRequest r = new MaintenanceRequest(
                rs.getInt("request_id"),
                rs.getInt("room_number"),
                MaintenanceRequest.IssueType.valueOf(rs.getString("issue_type")),
                rs.getString("description"),
                MaintenanceRequest.Priority.valueOf(rs.getString("priority")));
        r.setStatus(MaintenanceRequest.Status.valueOf(rs.getString("status")));
        r.setAssignedTechnician(rs.wasNull() ? null : techId, techName);
        return r;
    }

    private String getStaffName(Integer id) {
        if (id == null)
            return "Unassigned";
        return staffController.getAllStaff().stream()
                .filter(s -> s.getStaffId() == id)
                .map(Staff::getFullName)
                .findFirst().orElse("Unknown");
    }
}
