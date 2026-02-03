package com.orionhotel;

public class Main {
    public static void main(String[] args) {
        // Initialize Database and Seed Data
        com.orionhotel.database.DatabaseConnection.initializeDatabase();

        // Launch UI
        com.orionhotel.ui.LoginUI.main(args);
    }
}