package com.orionhotel.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class SchemaInit {

    public static void createTables(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {

            // 1. Staff Table
            stmt.execute("CREATE TABLE IF NOT EXISTS staff (" +
                    "staff_id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "name VARCHAR(100) NOT NULL, " +
                    "role VARCHAR(50) NOT NULL, " +
                    "username VARCHAR(50) UNIQUE NOT NULL, " +
                    "password VARCHAR(255) NOT NULL, " + // In real app, store hashed
                    "status VARCHAR(20) DEFAULT 'ACTIVE')");

            // 2. Guests Table
            stmt.execute("CREATE TABLE IF NOT EXISTS guests (" +
                    "guest_id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "full_name VARCHAR(100) NOT NULL, " +
                    "phone VARCHAR(20), " +
                    "email VARCHAR(100), " +
                    "id_number VARCHAR(50), " +
                    "address TEXT, " +
                    "room_number INT, " +
                    "check_in_date DATE, " +
                    "check_out_date DATE, " +
                    "status VARCHAR(20) DEFAULT 'REGISTERED')");

            // 3. Rooms Table
            stmt.execute("CREATE TABLE IF NOT EXISTS rooms (" +
                    "room_id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "room_number INT UNIQUE NOT NULL, " +
                    "type VARCHAR(50), " +
                    "price DECIMAL(10,2), " +
                    "status VARCHAR(20) DEFAULT 'AVAILABLE')");

            // 4. Reservations Table
            stmt.execute("CREATE TABLE IF NOT EXISTS reservations (" +
                    "reservation_id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "guest_name VARCHAR(100), " +
                    "phone VARCHAR(20), " +
                    "email VARCHAR(100), " +
                    "room_type VARCHAR(50), " +
                    "room_number INT, " +
                    "check_in DATE, " +
                    "check_out DATE, " +
                    "num_guests INT, " +
                    "status VARCHAR(20))");

            // 5. Inventory Table
            stmt.execute("CREATE TABLE IF NOT EXISTS inventory (" +
                    "item_id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "name VARCHAR(100) NOT NULL, " +
                    "category VARCHAR(50), " +
                    "quantity INT DEFAULT 0, " +
                    "min_level INT DEFAULT 10, " +
                    "supplier VARCHAR(100))");

            // 6. Inventory Logs Table (for reports)
            stmt.execute("CREATE TABLE IF NOT EXISTS inventory_logs (" +
                    "log_id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "item_id INT, " +
                    "type VARCHAR(20), " + // CONSUME or RESTOCK
                    "amount INT, " +
                    "timestamp TIMESTAMP, " +
                    "FOREIGN KEY (item_id) REFERENCES inventory(item_id))");

            // 7. Notifications Table
            stmt.execute("CREATE TABLE IF NOT EXISTS notifications (" +
                    "notif_id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "message TEXT, " +
                    "target_role VARCHAR(20), " +
                    "is_read BOOLEAN DEFAULT FALSE, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            // 8. Housekeeping Tasks Table
            stmt.execute("CREATE TABLE IF NOT EXISTS housekeeping_tasks (" +
                    "task_id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "room_number INT, " +
                    "type VARCHAR(20), " + // CLEANING, DEEP_CLEAN, REPAIR
                    "status VARCHAR(20), " + // PENDING, IN_PROGRESS, COMPLETED
                    "staff_id INT, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (room_number) REFERENCES rooms(room_number), " +
                    "FOREIGN KEY (staff_id) REFERENCES staff(staff_id))");

            // 9. Maintenance Requests Table
            stmt.execute("CREATE TABLE IF NOT EXISTS maintenance_requests (" +
                    "request_id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "room_number INT, " +
                    "issue_type VARCHAR(50), " +
                    "description TEXT, " +
                    "priority VARCHAR(20), " +
                    "status VARCHAR(20), " +
                    "tech_id INT, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (room_number) REFERENCES rooms(room_number), " +
                    "FOREIGN KEY (tech_id) REFERENCES staff(staff_id))");

            // 10. System Settings Table
            stmt.execute("CREATE TABLE IF NOT EXISTS system_settings (" +
                    "setting_key VARCHAR(50) PRIMARY KEY, " +
                    "setting_value TEXT)");

            // 11. Bills Table
            stmt.execute("CREATE TABLE IF NOT EXISTS bills (" +
                    "bill_id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "guest_id INT, " +
                    "guest_name VARCHAR(100), " +
                    "room_charges DOUBLE, " +
                    "taxes DOUBLE, " +
                    "discount DOUBLE, " +
                    "total_amount DOUBLE, " +
                    "status VARCHAR(20), " + // PAID, UNPAID, PARTIAL
                    "method VARCHAR(20), " + // CASH, CARD, etc.
                    "bill_date DATE, " +
                    "FOREIGN KEY (guest_id) REFERENCES guests(guest_id))");

            // 12. Service Charges Table
            stmt.execute("CREATE TABLE IF NOT EXISTS service_charges (" +
                    "charge_id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "bill_id INT, " +
                    "service_type VARCHAR(100), " +
                    "amount DOUBLE, " +
                    "charge_date DATE, " +
                    "FOREIGN KEY (bill_id) REFERENCES bills(bill_id))");

            // Seed default settings if empty
            var rsSettings = stmt.executeQuery("SELECT COUNT(*) FROM system_settings");
            if (rsSettings.next() && rsSettings.getInt(1) == 0) {
                stmt.execute("INSERT INTO system_settings (setting_key, setting_value) VALUES " +
                        "('hotel_name', 'Orion Hotel Oracle')," +
                        "('currency', 'USD')," +
                        "('checkout_time', '12:00')," +
                        "('tax_rate', '12.5')," +
                        "('service_charge', '5.0')");
            }
            // Seed default admin if table is empty
            var rs = stmt.executeQuery("SELECT COUNT(*) FROM staff");
            if (rs.next() && rs.getInt(1) == 0) {
                // Generate hash dynamically to ensure match
                String adminHash = com.orionhotel.model.Staff.hashPassword("admin123");
                stmt.execute("INSERT INTO staff (name, role, username, password) VALUES " +
                        "('System Admin', 'ADMIN', 'admin', '" + adminHash + "')");
                System.out.println("Seeded default admin user with hash: " + adminHash);
            }

            // Seed default rooms if table is empty
            var rsRooms = stmt.executeQuery("SELECT COUNT(*) FROM rooms");
            if (rsRooms.next() && rsRooms.getInt(1) == 0) {
                stmt.execute("INSERT INTO rooms (room_number, type, price, status) VALUES " +
                        "(101, 'Single', 100.00, 'AVAILABLE')," +
                        "(102, 'Double', 150.00, 'AVAILABLE')," +
                        "(103, 'Suite', 300.00, 'AVAILABLE')," +
                        "(104, 'Single', 100.00, 'AVAILABLE')," +
                        "(105, 'Double', 150.00, 'AVAILABLE')");
                System.out.println("Seeded default rooms.");
            }

            System.out.println("Database schema initialized successfully.");
        }
    }
}
