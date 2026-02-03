package com.orionhotel.ui;

import com.orionhotel.controller.StaffController;
import com.orionhotel.model.Role;
import com.orionhotel.model.Staff;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class StaffUI {

    private StaffController controller = new StaffController();
    private TableView<Staff> table = new TableView<>();
    private ObservableList<Staff> data = FXCollections.observableArrayList();
    private VBox rootPane;

    public StaffUI() {
        initializeUI();
    }

    public VBox getRootPane() {
        return rootPane;
    }

    private void initializeUI() {
        refreshTable();

        // Table Columns
        TableColumn<Staff, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getStaffId()));

        TableColumn<Staff, String> nameCol = new TableColumn<>("Full Name");
        nameCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFullName()));

        TableColumn<Staff, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRole().toString()));

        TableColumn<Staff, String> userCol = new TableColumn<>("Username");
        userCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getUsername()));

        TableColumn<Staff, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus().toString()));

        table.getColumns().add(idCol);
        table.getColumns().add(nameCol);
        table.getColumns().add(roleCol);
        table.getColumns().add(userCol);
        table.getColumns().add(statusCol);
        table.setItems(data);

        // Buttons
        Button addBtn = new Button("Add Staff");
        addBtn.setOnAction(e -> showStaffDialog(null));

        Button editBtn = new Button("Edit Staff");
        editBtn.setOnAction(e -> {
            Staff selected = table.getSelectionModel().getSelectedItem();
            if (selected != null)
                showStaffDialog(selected);
            else
                showAlert("Select a staff member first.");
        });

        Button deactivateBtn = new Button("Deactivate");
        deactivateBtn.setOnAction(e -> {
            Staff selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                controller.deactivateStaff(selected.getStaffId());
                refreshTable();
            } else {
                showAlert("Select a staff member first.");
            }
        });

        Button reportBtn = new Button("Role Report");
        reportBtn.setOnAction(e -> showRoleReport());

        HBox btnBox = new HBox(10, addBtn, editBtn, deactivateBtn, reportBtn);
        btnBox.setPadding(new Insets(10));

        rootPane = new VBox(10, new Label("Staff Management"), table, btnBox);
        rootPane.setPadding(new Insets(10));
    }

    private void refreshTable() {
        data.setAll(controller.getAllStaff());
    }

    private void showStaffDialog(Staff existing) {
        Dialog<Staff> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Add Staff" : "Edit Staff");
        dialog.setHeaderText("Enter staff details:");

        ButtonType saveBtnType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        TextField phoneField = new TextField();
        TextField emailField = new TextField();
        TextField userField = new TextField();
        PasswordField passField = new PasswordField();
        ComboBox<Role> roleBox = new ComboBox<>(FXCollections.observableArrayList(Role.values()));

        if (existing != null) {
            nameField.setText(existing.getFullName());
            phoneField.setText(existing.getPhoneNumber());
            emailField.setText(existing.getEmail());
            userField.setText(existing.getUsername());
            userField.setDisable(true); // Don't allow changing username easily
            roleBox.setValue(existing.getRole());
            passField.setPromptText("Leave blank to keep current");
        }

        grid.add(new Label("Full Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Phone:"), 0, 1);
        grid.add(phoneField, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("Role:"), 0, 3);
        grid.add(roleBox, 1, 3);
        grid.add(new Label("Username:"), 0, 4);
        grid.add(userField, 1, 4);
        grid.add(new Label("Password:"), 0, 5);
        grid.add(passField, 1, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveBtnType) {
                if (existing == null) {
                    // Create new
                    return new Staff(data.size() + 1, nameField.getText(), phoneField.getText(),
                            emailField.getText(), roleBox.getValue(), userField.getText(), passField.getText());
                } else {
                    // Update existing
                    existing.setFullName(nameField.getText());
                    existing.setPhoneNumber(phoneField.getText());
                    existing.setEmail(emailField.getText());
                    existing.setRole(roleBox.getValue());
                    if (!passField.getText().isEmpty()) {
                        existing.setPassword(passField.getText());
                    }
                    return existing;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(staff -> {
            if (existing == null) {
                controller.addStaff(staff);
            } else {
                controller.updateStaff(staff);
            }
            refreshTable();
        });
    }

    private void showRoleReport() {
        StringBuilder sb = new StringBuilder("Active Staff Distribution:\n");
        for (Role r : Role.values()) {
            sb.append(r).append(": ").append(controller.getRoleCount(r)).append("\n");
        }
        Alert alert = new Alert(Alert.AlertType.INFORMATION, sb.toString());
        alert.setHeaderText("Role Distribution Report");
        alert.show();
    }

    private void showAlert(String msg) {
        new Alert(Alert.AlertType.ERROR, msg).show();
    }
}
