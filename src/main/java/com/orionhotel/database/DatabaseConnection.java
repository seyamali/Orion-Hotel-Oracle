package com.orionhotel.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    // Configuration - Change this to switch between H2 and MySQL
    private static final DB_TYPE CURRENT_DB = DB_TYPE.H2;

    // H2 Configuration (Embedded - Creates file in project folder)
    private static final String H2_URL = "jdbc:h2:./orion_hotel_db;AUTO_SERVER=TRUE";
    private static final String H2_USER = "sa";
    private static final String H2_PASSWORD = "";

    // MySQL Configuration (Requires running MySQL server)
    private static final String MYSQL_URL = "jdbc:mysql://localhost:3306/orion_hotel_db";
    private static final String MYSQL_USER = "root";
    private static final String MYSQL_PASSWORD = "password";

    private static Connection connection;

    public enum DB_TYPE {
        H2, MYSQL
    }

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                if (CURRENT_DB == DB_TYPE.H2) {
                    // H2 Driver is auto-loaded in newer versions, but for safety:
                    Class.forName("org.h2.Driver");
                    connection = DriverManager.getConnection(H2_URL, H2_USER, H2_PASSWORD);
                    System.out.println("Connected to H2 Database.");
                } else {
                    Class.forName("com.mysql.cj.jdbc.Driver");
                    connection = DriverManager.getConnection(MYSQL_URL, MYSQL_USER, MYSQL_PASSWORD);
                    System.out.println("Connected to MySQL Database.");
                }
            } catch (ClassNotFoundException e) {
                throw new SQLException("Database Driver not found!", e);
            }
        }
        return connection;
    }

    // Initialize tables (Helper for setup)
    public static void initializeDatabase() {
        try (Connection conn = getConnection()) {
            SchemaInit.createTables(conn);
        } catch (SQLException e) {
            System.err.println("Database Initialization Failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
