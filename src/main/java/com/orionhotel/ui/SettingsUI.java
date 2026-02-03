package com.orionhotel.ui;

import com.orionhotel.controller.SettingsController;
import com.orionhotel.model.SystemSettings;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;

public class SettingsUI {

    private SettingsController controller;
    private BorderPane rootPane;
    private VBox contentArea;

    public SettingsUI(SettingsController controller) {
        this.controller = controller;
        initializeUI();
    }

    public BorderPane getRootPane() {
        return rootPane;
    }

    private void initializeUI() {
        rootPane = new BorderPane();

        // Sidebar
        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(10));
        sidebar.setStyle("-fx-background-color: #e0e0e0; -fx-pref-width: 200px;");

        Button hotelInfoBtn = new Button("Hotel Information");
        hotelInfoBtn.setMaxWidth(Double.MAX_VALUE);
        hotelInfoBtn.setOnAction(e -> showHotelInfo());

        Button financialsBtn = new Button("Financials & Rooms");
        financialsBtn.setMaxWidth(Double.MAX_VALUE);
        financialsBtn.setOnAction(e -> showFinancials());

        Button securityBtn = new Button("Security & Alerts");
        securityBtn.setMaxWidth(Double.MAX_VALUE);
        securityBtn.setOnAction(e -> showSecurity());

        Button backupBtn = new Button("Backup & Data");
        backupBtn.setMaxWidth(Double.MAX_VALUE);
        backupBtn.setOnAction(e -> showBackup());

        sidebar.getChildren().addAll(hotelInfoBtn, financialsBtn, securityBtn, backupBtn);
        rootPane.setLeft(sidebar);

        // Content Area
        contentArea = new VBox(10);
        contentArea.setPadding(new Insets(20));
        rootPane.setCenter(contentArea);

        // Load default view
        showHotelInfo();
    }

    private void showHotelInfo() {
        contentArea.getChildren().clear();
        SystemSettings settings = controller.getSettings();

        Label header = new Label("Hotel Information Configuration");
        header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TextField nameField = new TextField(settings.getHotelName());
        TextField addrField = new TextField(settings.getHotelAddress());
        TextField phoneField = new TextField(settings.getHotelPhone());
        TextField emailField = new TextField(settings.getHotelEmail());

        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);
        grid.add(new Label("Hotel Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Address:"), 0, 1);
        grid.add(addrField, 1, 1);
        grid.add(new Label("Phone:"), 0, 2);
        grid.add(phoneField, 1, 2);
        grid.add(new Label("Email:"), 0, 3);
        grid.add(emailField, 1, 3);

        Button saveBtn = new Button("Save Changes");
        saveBtn.setOnAction(e -> {
            settings.setHotelName(nameField.getText());
            settings.setHotelAddress(addrField.getText());
            settings.setHotelPhone(phoneField.getText());
            settings.setHotelEmail(emailField.getText());
            controller.updateSettings(settings);
            showAlert("Information saved successfully.");
        });

        contentArea.getChildren().addAll(header, grid, saveBtn);
    }

    private void showFinancials() {
        contentArea.getChildren().clear();
        SystemSettings settings = controller.getSettings();

        Label header = new Label("Financials & Room Pricing");
        header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TextField taxField = new TextField(String.valueOf(settings.getTaxRate()));
        TextField serviceField = new TextField(String.valueOf(settings.getServiceChargeRate()));

        TextField singlePrice = new TextField(String.valueOf(settings.getRoomPrice("Single")));
        TextField doublePrice = new TextField(String.valueOf(settings.getRoomPrice("Double")));
        TextField suitePrice = new TextField(String.valueOf(settings.getRoomPrice("Suite")));

        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);
        grid.add(new Label("Tax Rate (0.10 = 10%):"), 0, 0);
        grid.add(taxField, 1, 0);
        grid.add(new Label("Service Charge Rate:"), 0, 1);
        grid.add(serviceField, 1, 1);

        grid.add(new Separator(), 0, 2, 2, 1);
        grid.add(new Label("Base Room Prices:"), 0, 3);
        grid.add(new Label("Single:"), 0, 4);
        grid.add(singlePrice, 1, 4);
        grid.add(new Label("Double:"), 0, 5);
        grid.add(doublePrice, 1, 5);
        grid.add(new Label("Suite:"), 0, 6);
        grid.add(suitePrice, 1, 6);

        Button saveBtn = new Button("Save Changes");
        saveBtn.setOnAction(e -> {
            try {
                settings.setTaxRate(Double.parseDouble(taxField.getText()));
                settings.setServiceChargeRate(Double.parseDouble(serviceField.getText()));
                settings.setRoomPrice("Single", Double.parseDouble(singlePrice.getText()));
                settings.setRoomPrice("Double", Double.parseDouble(doublePrice.getText()));
                settings.setRoomPrice("Suite", Double.parseDouble(suitePrice.getText()));
                controller.updateSettings(settings);
                showAlert("Financial settings saved.");
            } catch (NumberFormatException ex) {
                showAlert("Please enter valid numbers.");
            }
        });

        contentArea.getChildren().addAll(header, grid, saveBtn);
    }

    private void showSecurity() {
        contentArea.getChildren().clear();
        SystemSettings settings = controller.getSettings();

        Label header = new Label("Security & Notifications");
        header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TextField passLenField = new TextField(String.valueOf(settings.getPasswordMinLength()));
        CheckBox emailNotif = new CheckBox("Enable Email Notifications");
        emailNotif.setSelected(settings.isEmailNotificationsEnabled());
        CheckBox sysAlert = new CheckBox("Enable System Alerts");
        sysAlert.setSelected(settings.isSystemAlertsEnabled());

        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);
        grid.add(new Label("Min Password Length:"), 0, 0);
        grid.add(passLenField, 1, 0);
        grid.add(emailNotif, 0, 1, 2, 1);
        grid.add(sysAlert, 0, 2, 2, 1);

        Button saveBtn = new Button("Save Changes");
        saveBtn.setOnAction(e -> {
            try {
                settings.setPasswordMinLength(Integer.parseInt(passLenField.getText()));
                settings.setEmailNotificationsEnabled(emailNotif.isSelected());
                settings.setSystemAlertsEnabled(sysAlert.isSelected());
                controller.updateSettings(settings);
                showAlert("Security settings saved.");
            } catch (NumberFormatException ex) {
                showAlert("Invalid number format.");
            }
        });

        contentArea.getChildren().addAll(header, grid, saveBtn);
    }

    private void showBackup() {
        contentArea.getChildren().clear();

        Label header = new Label("Backup & Data Management");
        header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Backup section
        VBox backupSection = new VBox(10);
        backupSection.setPadding(new Insets(10));
        backupSection.setStyle("-fx-border-color: #ddd; -fx-border-width: 1; -fx-background-color: #f9f9f9;");

        Label backupTitle = new Label("Create Backup");
        backupTitle.setStyle("-fx-font-weight: bold;");

        Label backupDesc = new Label("Create a secure, encrypted backup of all system data including:");
        Label backupItems = new Label("• Guest information\n• Reservations\n• Billing records\n• Inventory data\n• Staff details\n• System settings");

        Button manualBackupBtn = new Button("Create Manual Backup");
        Label backupStatusLabel = new Label();

        manualBackupBtn.setOnAction(e -> {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Select Backup Location");
            File result = chooser.showDialog(rootPane.getScene().getWindow());
            if (result != null) {
                String path = controller.createBackup(result.getAbsolutePath());
                if (path != null) {
                    backupStatusLabel.setText("✅ Backup created successfully at: " + path);
                    backupStatusLabel.setStyle("-fx-text-fill: green;");
                } else {
                    backupStatusLabel.setText("❌ Backup failed. Please try again.");
                    backupStatusLabel.setStyle("-fx-text-fill: red;");
                }
            }
        });

        backupSection.getChildren().addAll(backupTitle, backupDesc, backupItems, manualBackupBtn, backupStatusLabel);

        // Restore section
        VBox restoreSection = new VBox(10);
        restoreSection.setPadding(new Insets(10));
        restoreSection.setStyle("-fx-border-color: #ddd; -fx-border-width: 1; -fx-background-color: #f9f9f9;");

        Label restoreTitle = new Label("Restore from Backup");
        restoreTitle.setStyle("-fx-font-weight: bold;");

        Label restoreDesc = new Label("⚠️ Warning: Restoring will overwrite current data. A backup of current data will be created automatically.");

        Button restoreBtn = new Button("Select Backup File to Restore");
        Label restoreStatusLabel = new Label();

        restoreBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Backup File");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Encrypted Backup Files", "*.enc")
            );
            File selectedFile = fileChooser.showOpenDialog(rootPane.getScene().getWindow());
            if (selectedFile != null) {
                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmAlert.setTitle("Confirm Restore");
                confirmAlert.setHeaderText("Restore from Backup");
                confirmAlert.setContentText("This will overwrite all current data. Are you sure you want to continue?");

                confirmAlert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        boolean success = controller.restoreBackup(selectedFile.getAbsolutePath());
                        if (success) {
                            restoreStatusLabel.setText("✅ Data restored successfully. Please restart the application.");
                            restoreStatusLabel.setStyle("-fx-text-fill: green;");
                        } else {
                            restoreStatusLabel.setText("❌ Restore failed. Please check the backup file and try again.");
                            restoreStatusLabel.setStyle("-fx-text-fill: red;");
                        }
                    }
                });
            }
        });

        restoreSection.getChildren().addAll(restoreTitle, restoreDesc, restoreBtn, restoreStatusLabel);

        // Automatic backup status
        VBox autoBackupSection = new VBox(10);
        autoBackupSection.setPadding(new Insets(10));
        autoBackupSection.setStyle("-fx-border-color: #ddd; -fx-border-width: 1; -fx-background-color: #f0f8ff;");

        Label autoTitle = new Label("Automatic Backup Status");
        autoTitle.setStyle("-fx-font-weight: bold;");

        Label autoDesc = new Label("🕐 Daily backups run automatically at 2:00 AM\n🗓️ Weekly full backups run every Sunday at 3:00 AM\n🔐 All backups are encrypted and stored in the 'backups' folder");

        // Show recent backups
        Label recentTitle = new Label("Recent Backups:");
        recentTitle.setStyle("-fx-font-weight: bold;");

        VBox recentBackups = new VBox(5);
        try {
            var dailyBackups = controller.getBackupHistory("backups/daily");
            var weeklyBackups = controller.getBackupHistory("backups/weekly");

            if (!dailyBackups.isEmpty()) {
                Label dailyLabel = new Label("📅 Daily: " + dailyBackups.get(dailyBackups.size() - 1));
                recentBackups.getChildren().add(dailyLabel);
            }

            if (!weeklyBackups.isEmpty()) {
                Label weeklyLabel = new Label("📆 Weekly: " + weeklyBackups.get(weeklyBackups.size() - 1));
                recentBackups.getChildren().add(weeklyLabel);
            }

            if (dailyBackups.isEmpty() && weeklyBackups.isEmpty()) {
                recentBackups.getChildren().add(new Label("No backups found yet."));
            }
        } catch (Exception e) {
            recentBackups.getChildren().add(new Label("Unable to load backup history."));
        }

        autoBackupSection.getChildren().addAll(autoTitle, autoDesc, recentTitle, recentBackups);

        contentArea.getChildren().addAll(header, backupSection, restoreSection, autoBackupSection);
    }

    private void showAlert(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg).show();
    }
}
