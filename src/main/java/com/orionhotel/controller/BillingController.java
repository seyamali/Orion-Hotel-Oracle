package com.orionhotel.controller;

import com.orionhotel.database.DatabaseConnection;
import com.orionhotel.model.Bill;
import com.orionhotel.model.Guest;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class BillingController {

    private GuestController guestController;
    private SettingsController settingsController;
    private RoomController roomController;

    public BillingController(GuestController guestController) {
        this.guestController = guestController;
    }

    public void setSettingsController(SettingsController sc) {
        this.settingsController = sc;
    }

    public void setRoomController(RoomController rc) {
        this.roomController = rc;
    }

    public void generateBillForGuest(int guestId) {
        Guest guest = guestController.findGuestById(guestId);
        if (guest != null) {
            Bill existing = getBillForGuest(guestId);
            if (existing != null) {
                updateRoomCharges(existing, guest);
                saveBill(existing);
                return;
            }

            Bill bill = new Bill(0, guestId, guest.getFullName());
            bill.setBillDate(LocalDate.now());
            updateRoomCharges(bill, guest);
            insertBill(bill);
        }
    }

    private void updateRoomCharges(Bill bill, Guest guest) {
        long nights = 1;
        if (guest.getCheckInDate() != null) {
            LocalDate end = (guest.getStatus() == Guest.GuestStatus.CHECKED_OUT) ? guest.getCheckOutDate()
                    : LocalDate.now();
            nights = ChronoUnit.DAYS.between(guest.getCheckInDate(), end);
            if (nights <= 0)
                nights = 1;
        }

        double dailyRate = 100.0;
        if (settingsController != null && roomController != null && guest.getRoomNumber() != null) {
            com.orionhotel.model.Room room = roomController.getRoom(guest.getRoomNumber());
            if (room != null) {
                dailyRate = settingsController.getSettings().getRoomPrice(room.getType());
            }
        }
        bill.setRoomCharges(nights * dailyRate);
        bill.recalculateTotal(getTaxRate());
    }

    public Bill getBillForGuest(int guestId) {
        String sql = "SELECT * FROM bills WHERE guest_id = ? AND status != 'PAID'";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, guestId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Bill b = mapResultSetToBill(rs);
                    b.setServiceCharges(getServiceCharges(b.getBillId()));
                    return b;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<Bill.ServiceCharge> getServiceCharges(int billId) {
        List<Bill.ServiceCharge> list = new ArrayList<>();
        String sql = "SELECT * FROM service_charges WHERE bill_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, billId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Bill.ServiceCharge(
                            rs.getString("service_type"),
                            rs.getDouble("amount"),
                            rs.getDate("charge_date").toLocalDate()));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void addServiceCharge(int guestId, String serviceType, double amount) {
        Bill bill = getBillForGuest(guestId);
        if (bill != null) {
            String sql = "INSERT INTO service_charges (bill_id, service_type, amount, charge_date) VALUES (?, ?, ?, ?)";
            try (Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, bill.getBillId());
                pstmt.setString(2, serviceType);
                pstmt.setDouble(3, amount);
                pstmt.setDate(4, Date.valueOf(LocalDate.now()));
                pstmt.executeUpdate();

                bill.addServiceCharge(new Bill.ServiceCharge(serviceType, amount, LocalDate.now()));
                bill.recalculateTotal(getTaxRate());
                saveBill(bill);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void applyDiscount(int guestId, double discountAmount) {
        Bill bill = getBillForGuest(guestId);
        if (bill != null) {
            bill.setDiscount(discountAmount);
            bill.recalculateTotal(getTaxRate());
            saveBill(bill);
        }
    }

    public boolean processPayment(int guestId, double amount, Bill.PaymentMethod method) {
        Bill bill = getBillForGuest(guestId);
        if (bill != null && amount > 0) {
            if (amount >= bill.getTotalAmount()) {
                bill.setPaymentStatus(Bill.PaymentStatus.PAID);
                bill.setPaymentMethod(method);
                saveBill(bill);
                return true;
            } else {
                bill.setPaymentStatus(Bill.PaymentStatus.PARTIAL);
                bill.setPaymentMethod(method);
                saveBill(bill);
                return true;
            }
        }
        return false;
    }

    private double getTaxRate() {
        return (settingsController != null) ? settingsController.getSettings().getTaxRate() : 0.125;
    }

    private void insertBill(Bill b) {
        String sql = "INSERT INTO bills (guest_id, guest_name, room_charges, taxes, discount, total_amount, status, method, bill_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, b.getGuestId());
            pstmt.setString(2, b.getGuestName());
            pstmt.setDouble(3, b.getRoomCharges());
            pstmt.setDouble(4, b.getTaxes());
            pstmt.setDouble(5, b.getDiscount());
            pstmt.setDouble(6, b.getTotalAmount());
            pstmt.setString(7, b.getPaymentStatus().name());
            pstmt.setString(8, b.getPaymentMethod() != null ? b.getPaymentMethod().name() : "CASH");
            pstmt.setDate(9, Date.valueOf(b.getBillDate()));
            pstmt.executeUpdate();
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next())
                    b.setBillId(generatedKeys.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void saveBill(Bill b) {
        String sql = "UPDATE bills SET room_charges = ?, taxes = ?, discount = ?, total_amount = ?, status = ?, method = ? WHERE bill_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, b.getRoomCharges());
            pstmt.setDouble(2, b.getTaxes());
            pstmt.setDouble(3, b.getDiscount());
            pstmt.setDouble(4, b.getTotalAmount());
            pstmt.setString(5, b.getPaymentStatus().name());
            pstmt.setString(6, b.getPaymentMethod() != null ? b.getPaymentMethod().name() : "CASH");
            pstmt.setInt(7, b.getBillId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Bill> getAllBills() {
        List<Bill> list = new ArrayList<>();
        String sql = "SELECT * FROM bills";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next())
                list.add(mapResultSetToBill(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public double getDailyRevenue(LocalDate date) {
        String sql = "SELECT SUM(total_amount) FROM bills WHERE bill_date = ? AND status = 'PAID'";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, Date.valueOf(date));
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next())
                    return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public double getMonthlyRevenue(int year, int month) {
        String sql = "SELECT SUM(total_amount) FROM bills WHERE YEAR(bill_date) = ? AND MONTH(bill_date) = ? AND status = 'PAID'";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, year);
            pstmt.setInt(2, month);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next())
                    return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<Bill> getOutstandingBalances() {
        List<Bill> list = new ArrayList<>();
        String sql = "SELECT * FROM bills WHERE status != 'PAID'";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next())
                list.add(mapResultSetToBill(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private Bill mapResultSetToBill(ResultSet rs) throws SQLException {
        Bill b = new Bill(rs.getInt("bill_id"), rs.getInt("guest_id"), rs.getString("guest_name"));
        b.setRoomCharges(rs.getDouble("room_charges"));
        b.setTaxes(rs.getDouble("taxes"));
        b.setDiscount(rs.getDouble("discount"));
        b.setTotalAmount(rs.getDouble("total_amount"));
        b.setPaymentStatus(Bill.PaymentStatus.valueOf(rs.getString("status")));
        b.setPaymentMethod(Bill.PaymentMethod.valueOf(rs.getString("method")));
        b.setBillDate(rs.getDate("bill_date").toLocalDate());
        return b;
    }
}