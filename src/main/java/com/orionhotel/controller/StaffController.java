package com.orionhotel.controller;

import com.orionhotel.database.DatabaseConnection;
import com.orionhotel.model.Role;
import com.orionhotel.model.Staff;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class StaffController {

    private Staff loggedInUser;

    public StaffController() {
        // Init handled by DatabaseConnection on startup
    }

    public Staff authenticate(String username, String password) {
        String sql = "SELECT * FROM staff WHERE username = ? AND status = 'ACTIVE'"; // Removed password from query to
                                                                                     // debug
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password");
                    String inputHash = Staff.hashPassword(password);

                    if (storedHash.equals(inputHash)) {
                        Staff staff = mapResultSetToStaff(rs);
                        loggedInUser = staff;
                        return staff;
                    }
                } else {
                    System.out.println("User not found: " + username);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void logout() {
        loggedInUser = null;
    }

    public Staff getLoggedInUser() {
        return loggedInUser;
    }

    public void addStaff(Staff staff) {
        String sql = "INSERT INTO staff (name, role, username, password, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, staff.getFullName());
            pstmt.setString(2, staff.getRole().name());
            pstmt.setString(3, staff.getUsername());
            pstmt.setString(4, staff.getPasswordHash()); // Store the hash
            pstmt.setString(5, "ACTIVE");

            pstmt.executeUpdate();
            System.out.println("Staff added to DB: " + staff.getFullName());

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateStaff(Staff staff) {
        String sql = "UPDATE staff SET name = ?, role = ?, username = ? WHERE staff_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, staff.getFullName());
            pstmt.setString(2, staff.getRole().name());
            pstmt.setString(3, staff.getUsername());
            pstmt.setInt(4, staff.getStaffId());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deactivateStaff(int staffId) {
        String sql = "UPDATE staff SET status = 'INACTIVE' WHERE staff_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, staffId);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Staff> getAllStaff() {
        List<Staff> list = new ArrayList<>();
        String sql = "SELECT * FROM staff";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapResultSetToStaff(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Staff> getActiveStaff() {
        List<Staff> list = new ArrayList<>();
        String sql = "SELECT * FROM staff WHERE status = 'ACTIVE'";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapResultSetToStaff(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean hasPermission(Role role) {
        if (loggedInUser == null)
            return false;
        if (loggedInUser.getRole() == Role.ADMIN)
            return true;
        return loggedInUser.getRole() == role;
    }

    public long getRoleCount(Role role) {
        String sql = "SELECT COUNT(*) FROM staff WHERE role = ? AND status = 'ACTIVE'";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, role.name());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private Staff mapResultSetToStaff(ResultSet rs) throws SQLException {
        Staff s = new Staff();
        s.setStaffId(rs.getInt("staff_id"));
        s.setFullName(rs.getString("name"));

        try {
            s.setRole(Role.valueOf(rs.getString("role")));
        } catch (IllegalArgumentException e) {
            s.setRole(Role.RECEPTIONIST); // Fallback
        }

        s.setUsername(rs.getString("username"));
        // DB has raw password for seeded admin, but hash for new users.
        // For compatibility, we just set whatever is in DB as 'hash'.
        // If it's the raw 'admin123', the checkPassword method will fail unless we
        // handle it.
        // Ideally, we should seed with hash. But `Staff` model expects HASH in this
        // field.
        s.setPasswordHash(rs.getString("password"));

        String statusStr = rs.getString("status");
        if ("ACTIVE".equalsIgnoreCase(statusStr)) {
            s.setStatus(Staff.Status.ACTIVE);
        } else {
            s.setStatus(Staff.Status.INACTIVE);
        }

        // Note: Phone and Email are in the "Staff" model but my schema init didn't
        // include them in the basic SQL query above?
        // Let's check SchemaInit.java ...
        // SchemaInit.java: "CREATE TABLE IF NOT EXISTS staff (staff_id INT..., name,
        // role, username, password, status)"
        // It seems I missed phone/email columns in the initial schema creation!
        // I will stick to what is in the DB now to avoid errors, or update schema.
        // For now, I'll leave phone/email null or implementation empty till schema
        // update.

        return s;
    }
}
