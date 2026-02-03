package com.orionhotel.controller;

import com.orionhotel.database.DatabaseConnection;
import com.orionhotel.model.SystemSettings;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SettingsController {

    private SystemSettings settings;
    private ScheduledExecutorService scheduler;

    public SettingsController() {
        loadSettings();
        startAutomaticBackup();
    }

    private void startAutomaticBackup() {
        scheduler = Executors.newScheduledThreadPool(1);
        // Daily at 2 AM
        long initialDelay = calculateInitialDelay(2, 0);
        scheduler.scheduleAtFixedRate(this::performDailyBackup, initialDelay, 24 * 60 * 60, TimeUnit.SECONDS);
    }

    private long calculateInitialDelay(int hour, int min) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime target = now.toLocalDate().atTime(hour, min);
        if (now.isAfter(target))
            target = target.plusDays(1);
        return java.time.Duration.between(now, target).getSeconds();
    }

    private void performDailyBackup() {
        createBackup("backups/daily");
    }

    public SystemSettings getSettings() {
        return settings;
    }

    public void updateSettings(SystemSettings newSettings) {
        this.settings = newSettings;
        saveSettings();
    }

    public String createBackup(String targetDir) {
        try {
            File dir = new File(targetDir);
            if (!dir.exists())
                dir.mkdirs();

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String backupPath = dir.getAbsolutePath() + File.separator + "db_backup_" + timestamp + ".zip";

            // H2 Database Backup Command
            String sql = "BACKUP TO '" + backupPath + "'";
            try (Connection conn = DatabaseConnection.getConnection();
                    Statement stmt = conn.createStatement()) {
                stmt.execute(sql);
                return backupPath;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean restoreBackup(String backupFilePath) {
        // H2 Restore is usually done by replacing the file while connection is closed.
        // For a live app, this is tricky. We'll provide instructions or use SCRIPT
        // TO/RUNSCRIPT.
        // But for simplicity, we tell user to restart after replacement.
        System.out.println("Restore requested for: " + backupFilePath);
        return false; // Complex for live DB in same process
    }

    public java.util.List<String> getBackupHistory(String backupDir) {
        java.util.List<String> backups = new java.util.ArrayList<>();
        try {
            File dir = new File(backupDir);
            if (dir.exists() && dir.isDirectory()) {
                File[] files = dir.listFiles((d, name) -> name.endsWith(".zip"));
                if (files != null) {
                    for (File file : files) {
                        backups.add(file.getName());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return backups;
    }

    private void loadSettings() {
        settings = new SystemSettings();
        String sql = "SELECT * FROM system_settings";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String key = rs.getString("setting_key");
                String val = rs.getString("setting_value");
                applySetting(key, val);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void applySetting(String key, String val) {
        switch (key) {
            case "hotel_name":
                settings.setHotelName(val);
                break;
            case "hotel_address":
                settings.setHotelAddress(val);
                break;
            case "hotel_phone":
                settings.setHotelPhone(val);
                break;
            case "hotel_email":
                settings.setHotelEmail(val);
                break;
            case "currency":
                settings.setCurrencySymbol(val);
                break;
            case "tax_rate":
                settings.setTaxRate(Double.parseDouble(val));
                break;
            case "service_charge":
                settings.setServiceChargeRate(Double.parseDouble(val));
                break;
        }
    }

    private void saveSettings() {
        updateSetting("hotel_name", settings.getHotelName());
        updateSetting("hotel_address", settings.getHotelAddress());
        updateSetting("hotel_phone", settings.getHotelPhone());
        updateSetting("hotel_email", settings.getHotelEmail());
        updateSetting("currency", settings.getCurrencySymbol());
        updateSetting("tax_rate", String.valueOf(settings.getTaxRate()));
        updateSetting("service_charge", String.valueOf(settings.getServiceChargeRate()));
    }

    private void updateSetting(String key, String val) {
        String sql = "MERGE INTO system_settings (setting_key, setting_value) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, key);
            pstmt.setString(2, val);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
