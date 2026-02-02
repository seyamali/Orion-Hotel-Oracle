package com.orionhotel.ui;

import com.orionhotel.controller.GuestController;
import com.orionhotel.controller.RoomController;
import com.orionhotel.model.Guest;
import com.orionhotel.model.Room;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.List;

public class GuestUI {

    private GuestController controller;
    private RoomController roomController;
    private TableView<Guest> table = new TableView<>();
    private ObservableList<Guest> data = FXCollections.observableArrayList();
    private TextField searchField = new TextField();
    private ComboBox<String> statusFilter = new ComboBox<>();
    private VBox rootPane;

    public GuestUI() {
        roomController = new RoomController();
        controller = new GuestController(roomController);

        // Initialize some sample rooms if none exist
        if (roomController.getAllRooms().isEmpty()) {
            roomController.addRoom(new Room(101, "Single"));
            roomController.addRoom(new Room(102, "Double"));
            roomController.addRoom(new Room(103, "Suite"));
            roomController.addRoom(new Room(104, "Single"));
        }

        // Sample guests if none exist
        if (controller.getAllGuests().isEmpty()) {
            controller.addGuest(new Guest(1, "John Doe", "123-456-7890", "john@example.com", "123456789", "123 Main St"));
            controller.addGuest(new Guest(2, "Jane Smith", "987-654-3210", "jane@example.com", "987654321", "456 Oak Ave"));
        }

        initializeUI();
    }

    private void initializeUI() {
        refreshTable();

        table.setEditable(false); // For security, editing disabled in table

        // Search and filter controls
        searchField.setPromptText("Search by name or phone...");
        statusFilter.setPromptText("Filter by status");
        statusFilter.getItems().addAll("All", "REGISTERED", "CHECKED_IN", "CHECKED_OUT");
        statusFilter.getSelectionModel().selectFirst();

        // Filter logic
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        HBox filterBar = new HBox(10, searchField, statusFilter);
        filterBar.setPadding(new Insets(10));

        // Columns
        TableColumn<Guest, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("guestId"));

        TableColumn<Guest, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));

        TableColumn<Guest, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));

        TableColumn<Guest, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

        TableColumn<Guest, String> idMaskedCol = new TableColumn<>("National ID");
        idMaskedCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getMaskedNationalId()));

        TableColumn<Guest, String> addressCol = new TableColumn<>("Address");
        addressCol.setCellValueFactory(new PropertyValueFactory<>("address"));

        TableColumn<Guest, Integer> roomCol = new TableColumn<>("Room");
        roomCol.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));

        TableColumn<Guest, LocalDate> checkInCol = new TableColumn<>("Check-In");
        checkInCol.setCellValueFactory(new PropertyValueFactory<>("checkInDate"));

        TableColumn<Guest, LocalDate> checkOutCol = new TableColumn<>("Check-Out");
        checkOutCol.setCellValueFactory(new PropertyValueFactory<>("checkOutDate"));

        TableColumn<Guest, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getStatus().toString()));

        table.getColumns().addAll(idCol, nameCol, phoneCol, emailCol, idMaskedCol, addressCol,
                roomCol, checkInCol, checkOutCol, statusCol);

        // Buttons
        Button addBtn = new Button("Add Guest");
        addBtn.setOnAction(e -> handleAddGuest());

        Button checkInBtn = new Button("Check-In");
        checkInBtn.setOnAction(e -> handleCheckIn());

        Button checkOutBtn = new Button("Check-Out");
        checkOutBtn.setOnAction(e -> handleCheckOut());

        Button historyBtn = new Button("View History");
        historyBtn.setOnAction(e -> handleViewHistory());

        Button reportsBtn = new Button("Reports");
        reportsBtn.setOnAction(e -> showReports());

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setOnAction(e -> refreshTable());

        HBox buttonBar = new HBox(10, addBtn, checkInBtn, checkOutBtn, historyBtn, reportsBtn, refreshBtn);
        buttonBar.setPadding(new Insets(10));

        rootPane = new VBox(10, filterBar, table, buttonBar);
        rootPane.setPadding(new Insets(10));
    }

    public VBox getRootPane() {
        return rootPane;
    }

    private void refreshTable() {
        data.setAll(controller.getAllGuests());
        table.setItems(data);
    }

    private void applyFilters() {
        String search = searchField.getText().toLowerCase();
        String status = statusFilter.getValue();
        data.setAll(controller.getAllGuests().stream().filter(guest -> {
            boolean matchesSearch = guest.getFullName().toLowerCase().contains(search) ||
                    guest.getPhoneNumber().contains(search);
            boolean matchesStatus = status.equals("All") || guest.getStatus().toString().equalsIgnoreCase(status);
            return matchesSearch && matchesStatus;
        }).toList());
    }

    private void handleAddGuest() {
        Dialog<Guest> dialog = new Dialog<>();
        dialog.setTitle("Add New Guest");
        dialog.setHeaderText("Enter guest details:");

        Label nameLabel = new Label("Full Name:");
        TextField nameField = new TextField();
        Label phoneLabel = new Label("Phone:");
        TextField phoneField = new TextField();
        Label emailLabel = new Label("Email:");
        TextField emailField = new TextField();
        Label idLabel = new Label("National ID:");
        TextField idField = new TextField();
        Label addressLabel = new Label("Address:");
        TextField addressField = new TextField();

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        grid.add(nameLabel, 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(phoneLabel, 0, 1);
        grid.add(phoneField, 1, 1);
        grid.add(emailLabel, 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(idLabel, 0, 3);
        grid.add(idField, 1, 3);
        grid.add(addressLabel, 0, 4);
        grid.add(addressField, 1, 4);

        dialog.getDialogPane().setContent(grid);
        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                String name = nameField.getText().trim();
                String phone = phoneField.getText().trim();
                String email = emailField.getText().trim();
                String nationalId = idField.getText().trim();
                String address = addressField.getText().trim();

                if (name.isEmpty() || phone.isEmpty() || email.isEmpty() || nationalId.isEmpty() || address.isEmpty()) {
                    showAlert("All fields are required.");
                    return null;
                }

                // Basic validation
                if (!email.contains("@")) {
                    showAlert("Invalid email format.");
                    return null;
                }

                return new Guest(data.size() + 1, name, phone, email, nationalId, address);
            }
            return null;
        });

        dialog.showAndWait().ifPresent(guest -> {
            if (guest != null) {
                controller.addGuest(guest);
                refreshTable();
            }
        });
    }

    private void handleCheckIn() {
        Guest selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (selected.getStatus() == Guest.GuestStatus.CHECKED_IN) {
                showAlert("Guest is already checked in.");
                return;
            }

            // Show available rooms
            List<Room> availableRooms = roomController.getAvailableRooms();
            if (availableRooms.isEmpty()) {
                showAlert("No available rooms.");
                return;
            }

            ChoiceDialog<Room> roomDialog = new ChoiceDialog<>(availableRooms.get(0), availableRooms);
            roomDialog.setTitle("Select Room");
            roomDialog.setHeaderText("Choose a room for check-in:");
            roomDialog.setContentText("Available rooms:");

            roomDialog.showAndWait().ifPresent(room -> {
                DatePicker checkInDatePicker = new DatePicker(LocalDate.now());
                DatePicker checkOutDatePicker = new DatePicker(LocalDate.now().plusDays(1));

                Dialog<Void> dateDialog = new Dialog<>();
                dateDialog.setTitle("Check-In Dates");
                dateDialog.setHeaderText("Select check-in and check-out dates:");

                GridPane dateGrid = new GridPane();
                dateGrid.setHgap(10);
                dateGrid.setVgap(10);
                dateGrid.setPadding(new Insets(20, 150, 10, 10));
                dateGrid.add(new Label("Check-In Date:"), 0, 0);
                dateGrid.add(checkInDatePicker, 1, 0);
                dateGrid.add(new Label("Check-Out Date:"), 0, 1);
                dateGrid.add(checkOutDatePicker, 1, 1);

                dateDialog.getDialogPane().setContent(dateGrid);
                dateDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

                dateDialog.showAndWait().ifPresent(result -> {
                    LocalDate checkIn = checkInDatePicker.getValue();
                    LocalDate checkOut = checkOutDatePicker.getValue();
                    if (checkIn != null && checkOut != null && !checkOut.isBefore(checkIn)) {
                        // For now, just check in with current date - dates are set automatically
                        controller.checkInGuest(selected.getGuestId(), room.getRoomNumber());
                        refreshTable();
                    } else {
                        showAlert("Invalid dates selected.");
                    }
                });
            });
        } else {
            showAlert("Please select a guest to check in.");
        }
    }

    private void handleCheckOut() {
        Guest selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (selected.getStatus() != Guest.GuestStatus.CHECKED_IN) {
                showAlert("Guest is not checked in.");
                return;
            }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Check out guest " + selected.getFullName() + " from room " + selected.getRoomNumber() + "?",
                    ButtonType.YES, ButtonType.NO);
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    controller.checkOutGuest(selected.getGuestId());
                    refreshTable();
                }
            });
        } else {
            showAlert("Please select a guest to check out.");
        }
    }

    private void handleViewHistory() {
        Guest selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            List<GuestController.PastStay> history = controller.getGuestHistory(selected.getGuestId());

            Alert historyAlert = new Alert(Alert.AlertType.INFORMATION);
            historyAlert.setTitle("Stay History - " + selected.getFullName());
            historyAlert.setHeaderText("Previous stays:");

            if (history.isEmpty()) {
                historyAlert.setContentText("No previous stays recorded.");
            } else {
                StringBuilder sb = new StringBuilder();
                for (GuestController.PastStay record : history) {
                    sb.append(String.format("Room %d: %s to %s\n",
                            record.roomNumber, record.checkInDate, record.checkOutDate));
                }
                historyAlert.setContentText(sb.toString());
            }

            historyAlert.showAndWait();
        } else {
            showAlert("Please select a guest to view history.");
        }
    }

    private void showReports() {
        TabPane tabPane = new TabPane();

        // Current guests report
        Tab currentTab = new Tab("Current Guests");
        VBox currentBox = new VBox(10);
        currentBox.setPadding(new Insets(10));

        TableView<Guest> currentTable = new TableView<>();
        currentTable.setPlaceholder(new Label("No current guests"));

        TableColumn<Guest, String> cNameCol = new TableColumn<>("Name");
        cNameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        TableColumn<Guest, Integer> cRoomCol = new TableColumn<>("Room");
        cRoomCol.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        TableColumn<Guest, LocalDate> cCheckInCol = new TableColumn<>("Check-In");
        cCheckInCol.setCellValueFactory(new PropertyValueFactory<>("checkInDate"));
        TableColumn<Guest, LocalDate> cCheckOutCol = new TableColumn<>("Check-Out");
        cCheckOutCol.setCellValueFactory(new PropertyValueFactory<>("checkOutDate"));

        currentTable.getColumns().addAll(cNameCol, cRoomCol, cCheckInCol, cCheckOutCol);
        currentTable.getItems().setAll(controller.getCheckedInGuests());
        currentBox.getChildren().add(currentTable);
        currentTab.setContent(currentBox);

        // Occupancy report
        Tab occupancyTab = new Tab("Occupancy Report");
        VBox occupancyBox = new VBox(10);
        occupancyBox.setPadding(new Insets(10));

        Label totalRoomsLabel = new Label("Total Rooms: " + roomController.getAllRooms().size());
        Label occupiedRoomsLabel = new Label("Occupied Rooms: " + controller.getCheckedInGuests().size());
        Label availableRoomsLabel = new Label("Available Rooms: " +
                (roomController.getAllRooms().size() - controller.getCheckedInGuests().size()));
        double occupancyRate = (double) controller.getCheckedInGuests().size() / roomController.getAllRooms().size() * 100;
        Label occupancyRateLabel = new Label(String.format("Occupancy Rate: %.1f%%", occupancyRate));

        occupancyBox.getChildren().addAll(totalRoomsLabel, occupiedRoomsLabel, availableRoomsLabel, occupancyRateLabel);
        occupancyTab.setContent(occupancyBox);

        // Revenue report (simplified)
        Tab revenueTab = new Tab("Revenue Report");
        VBox revenueBox = new VBox(10);
        revenueBox.setPadding(new Insets(10));

        // This would be more complex in a real system with pricing
        Label totalStaysLabel = new Label("Total Completed Stays: " + controller.getGuestHistory(0).size()); // Simplified
        Label avgStayLabel = new Label("Average Stay Duration: N/A"); // Would need calculation

        revenueBox.getChildren().addAll(totalStaysLabel, avgStayLabel);
        revenueTab.setContent(revenueBox);

        tabPane.getTabs().addAll(currentTab, occupancyTab, revenueTab);

        Alert dialog = new Alert(Alert.AlertType.INFORMATION);
        dialog.setTitle("Guest Reports");
        dialog.setHeaderText(null);
        dialog.getDialogPane().setContent(tabPane);
        dialog.setResizable(true);
        dialog.showAndWait();
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.showAndWait();
    }
}
