package com.orionhotel.controller;

import com.orionhotel.database.DatabaseConnection;
import com.orionhotel.model.InventoryItem;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class InventoryController {

    private NotificationController notificationController;

    // Inner classes for Reports (mapped from DB now)
    public static class ConsumptionEvent {
        public final int itemId;
        public final String itemName;
        public final int amount;
        public final LocalDateTime timestamp;

        public ConsumptionEvent(int itemId, String itemName, int amount, LocalDateTime timestamp) {
            this.itemId = itemId;
            this.itemName = itemName;
            this.amount = amount;
            this.timestamp = timestamp;
        }
    }

    public static class RestockEvent {
        public final int itemId;
        public final String itemName;
        public final int amount;
        public final LocalDateTime timestamp;

        public RestockEvent(int itemId, String itemName, int amount, LocalDateTime timestamp) {
            this.itemId = itemId;
            this.itemName = itemName;
            this.amount = amount;
            this.timestamp = timestamp;
        }
    }

    // Simplified LowStock event derived from current state or logs
    public static class LowStockEvent {
        public final int itemId;
        public final String itemName;
        public final int currentQty;
        public final int threshold;
        public final LocalDateTime timestamp;

        public LowStockEvent(int itemId, String itemName, int currentQty, int threshold, LocalDateTime timestamp) {
            this.itemId = itemId;
            this.itemName = itemName;
            this.currentQty = currentQty;
            this.threshold = threshold;
            this.timestamp = timestamp;
        }
    }

    public void setNotificationController(NotificationController nc) {
        this.notificationController = nc;
    }

    public InventoryController() {
    }

    public void addItem(InventoryItem item) {
        String sql = "INSERT INTO inventory (name, category, quantity, min_level, supplier) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, item.getItemName());
            pstmt.setString(2, item.getCategory());
            pstmt.setInt(3, item.getQuantity());
            pstmt.setInt(4, item.getMinimumThreshold());
            pstmt.setString(5, item.getSupplier());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<InventoryItem> getAllItems() {
        List<InventoryItem> list = new ArrayList<>();
        String sql = "SELECT * FROM inventory";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapResultSetToItem(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean consumeItem(int itemId, int amount) {
        InventoryItem item = getItem(itemId);
        if (item != null && item.getQuantity() >= amount) {
            int newQty = item.getQuantity() - amount;
            updateItemQuantity(itemId, newQty);
            logTransaction(itemId, "CONSUME", amount);

            // Check low stock
            if (newQty <= item.getMinimumThreshold()) {
                if (notificationController != null) {
                    notificationController.addNotification("Low Stock Alert: " + item.getItemName(),
                            com.orionhotel.model.Notification.TargetRole.MANAGER);
                }
            }
            return true;
        }
        return false;
    }

    public void restockItem(int itemId, int amount) {
        InventoryItem item = getItem(itemId);
        if (item != null) {
            int newQty = item.getQuantity() + amount;
            updateItemQuantity(itemId, newQty);
            logTransaction(itemId, "RESTOCK", amount);
        }
    }

    public List<InventoryItem> getLowStockItems() {
        List<InventoryItem> list = new ArrayList<>();
        String sql = "SELECT * FROM inventory WHERE quantity <= min_level";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapResultSetToItem(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private InventoryItem getItem(int itemId) {
        String sql = "SELECT * FROM inventory WHERE item_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, itemId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next())
                    return mapResultSetToItem(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void updateItemQuantity(int itemId, int newQty) {
        String sql = "UPDATE inventory SET quantity = ? WHERE item_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, newQty);
            pstmt.setInt(2, itemId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void logTransaction(int itemId, String type, int amount) {
        String sql = "INSERT INTO inventory_logs (item_id, type, amount, timestamp) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, itemId);
            pstmt.setString(2, type);
            pstmt.setInt(3, amount);
            pstmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private InventoryItem mapResultSetToItem(ResultSet rs) throws SQLException {
        InventoryItem item = new InventoryItem();
        item.setItemId(rs.getInt("item_id"));
        item.setItemName(rs.getString("name"));
        item.setCategory(rs.getString("category"));
        item.setQuantity(rs.getInt("quantity"));
        item.setMinimumThreshold(rs.getInt("min_level"));
        item.setSupplier(rs.getString("supplier"));
        return item;
    }

    // Reporting Methods (Fetching from Logs)
    public List<ConsumptionEvent> getConsumptionEventsForDay(java.time.LocalDate date) {
        List<ConsumptionEvent> list = new ArrayList<>();
        String sql = "SELECT l.*, i.name FROM inventory_logs l JOIN inventory i ON l.item_id = i.item_id " +
                "WHERE l.type = 'CONSUME' AND CAST(l.timestamp AS DATE) = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, java.sql.Date.valueOf(date));
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new ConsumptionEvent(
                            rs.getInt("item_id"),
                            rs.getString("name"),
                            rs.getInt("amount"),
                            rs.getTimestamp("timestamp").toLocalDateTime()));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<RestockEvent> getRestockEventsForMonth(java.time.YearMonth month) {
        List<RestockEvent> list = new ArrayList<>();
        // H2/MySQL syntax for month check might differ slightly, using standard SQL for
        // now or filtering in java if complex
        // Let's use robust range check
        LocalDateTime start = month.atDay(1).atStartOfDay();
        LocalDateTime end = month.atEndOfMonth().atTime(23, 59, 59);

        String sql = "SELECT l.*, i.name FROM inventory_logs l JOIN inventory i ON l.item_id = i.item_id " +
                "WHERE l.type = 'RESTOCK' AND l.timestamp BETWEEN ? AND ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(start));
            pstmt.setTimestamp(2, Timestamp.valueOf(end));
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new RestockEvent(
                            rs.getInt("item_id"),
                            rs.getString("name"),
                            rs.getInt("amount"),
                            rs.getTimestamp("timestamp").toLocalDateTime()));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<LowStockEvent> getLowStockHistory() {
        // Placeholder: Since we just added logs, history is derived from logs or
        // current state.
        // For now, let's just return current low stock as "recent events" to match
        // interface
        // Real implementation would need a separate 'alerts_log' table or complex
        // query.
        List<LowStockEvent> list = new ArrayList<>();
        List<InventoryItem> lowItems = getLowStockItems();
        for (InventoryItem item : lowItems) {
            list.add(new LowStockEvent(item.getItemId(), item.getItemName(), item.getQuantity(),
                    item.getMinimumThreshold(), LocalDateTime.now()));
        }
        return list;
    }

    public static class ItemUsage {
        public final String itemName;
        public final int totalConsumed;

        public ItemUsage(String name, int total) {
            this.itemName = name;
            this.totalConsumed = total;
        }
    }

    public List<ItemUsage> getMostUsedItems() {
        List<ItemUsage> list = new ArrayList<>();
        String sql = "SELECT i.name, SUM(l.amount) as total FROM inventory_logs l JOIN inventory i ON l.item_id = i.item_id "
                +
                "WHERE l.type = 'CONSUME' GROUP BY i.name ORDER BY total DESC";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new ItemUsage(rs.getString("name"), rs.getInt("total")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}