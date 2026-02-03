package com.orionhotel.ui;

import com.orionhotel.controller.BookingController;
import com.orionhotel.controller.RoomController;
import com.orionhotel.model.Reservation;
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

public class ReservationUI {

    private BookingController controller;
    private RoomController roomController;
    private TableView<Reservation> table = new TableView<>();
    private ObservableList<Reservation> data = FXCollections.observableArrayList();
    private TextField searchField = new TextField();
    private ComboBox<String> statusFilter = new ComboBox<>();
    private VBox rootPane;

    public ReservationUI() {
        roomController = new RoomController();
        controller = new BookingController(roomController);

        // Initialize sample rooms removed. User must add rooms via SQL initialization
        // or UI.

        // Sample reservations if none exist
        if (controller.getAllReservations().isEmpty()) {
            controller.addReservation(new Reservation(1, "Alice Johnson", "555-1234", "alice@example.com", "Single",
                    LocalDate.now().plusDays(1), LocalDate.now().plusDays(3), 1));
            controller.addReservation(new Reservation(2, "Bob Smith", "555-5678", "bob@example.com", "Double",
                    LocalDate.now().plusDays(2), LocalDate.now().plusDays(5), 2));
        }

        initializeUI();
    }

    public void setNotificationController(com.orionhotel.controller.NotificationController nc) {
        controller.setNotificationController(nc);
    }

    private void initializeUI() {
        refreshTable();

        // Search and filter controls
        searchField.setPromptText("Search by guest name...");
        statusFilter.setPromptText("Filter by status");
        statusFilter.getItems().addAll("All", "PENDING", "CONFIRMED", "CANCELLED", "COMPLETED");
        statusFilter.getSelectionModel().selectFirst();

        // Filter logic
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        HBox filterBar = new HBox(10, searchField, statusFilter);
        filterBar.setPadding(new Insets(10));

        // Columns
        TableColumn<Reservation, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("reservationId"));

        TableColumn<Reservation, String> nameCol = new TableColumn<>("Guest Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("guestName"));

        TableColumn<Reservation, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));

        TableColumn<Reservation, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

        TableColumn<Reservation, String> roomTypeCol = new TableColumn<>("Room Type");
        roomTypeCol.setCellValueFactory(new PropertyValueFactory<>("roomType"));

        TableColumn<Reservation, Integer> roomNumCol = new TableColumn<>("Room Number");
        roomNumCol.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));

        TableColumn<Reservation, LocalDate> checkInCol = new TableColumn<>("Check-In");
        checkInCol.setCellValueFactory(new PropertyValueFactory<>("checkInDate"));

        TableColumn<Reservation, LocalDate> checkOutCol = new TableColumn<>("Check-Out");
        checkOutCol.setCellValueFactory(new PropertyValueFactory<>("checkOutDate"));

        TableColumn<Reservation, Integer> guestsCol = new TableColumn<>("Guests");
        guestsCol.setCellValueFactory(new PropertyValueFactory<>("numberOfGuests"));

        TableColumn<Reservation, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getStatus().toString()));

        table.getColumns().add(idCol);
        table.getColumns().add(nameCol);
        table.getColumns().add(phoneCol);
        table.getColumns().add(emailCol);
        table.getColumns().add(roomTypeCol);
        table.getColumns().add(roomNumCol);
        table.getColumns().add(checkInCol);
        table.getColumns().add(checkOutCol);
        table.getColumns().add(guestsCol);
        table.getColumns().add(statusCol);

        // Buttons
        Button createBtn = new Button("Create Reservation");
        createBtn.setOnAction(e -> handleCreateReservation());

        Button confirmBtn = new Button("Confirm");
        confirmBtn.setOnAction(e -> handleConfirm());

        Button assignBtn = new Button("Assign Room");
        assignBtn.setOnAction(e -> handleAssignRoom());

        Button modifyBtn = new Button("Modify");
        modifyBtn.setOnAction(e -> handleModify());

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setOnAction(e -> handleCancel());

        Button checkInBtn = new Button("Check-In");
        checkInBtn.setOnAction(e -> handleCheckIn());

        Button reportsBtn = new Button("Reports");
        reportsBtn.setOnAction(e -> showReports());

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setOnAction(e -> refreshTable());

        HBox buttonBar = new HBox(10, createBtn, confirmBtn, assignBtn, modifyBtn, cancelBtn, checkInBtn, reportsBtn,
                refreshBtn);
        buttonBar.setPadding(new Insets(10));

        rootPane = new VBox(10, filterBar, table, buttonBar);
        rootPane.setPadding(new Insets(10));
    }

    public VBox getRootPane() {
        return rootPane;
    }

    private void refreshTable() {
        data.setAll(controller.getAllReservations());
        table.setItems(data);
    }

    private void applyFilters() {
        String search = searchField.getText().toLowerCase();
        String status = statusFilter.getValue();
        data.setAll(controller.getAllReservations().stream().filter(res -> {
            boolean matchesSearch = res.getGuestName().toLowerCase().contains(search);
            boolean matchesStatus = status.equals("All") || res.getStatus().toString().equalsIgnoreCase(status);
            return matchesSearch && matchesStatus;
        }).toList());
    }

    private void handleCreateReservation() {
        Dialog<Reservation> dialog = new Dialog<>();
        dialog.setTitle("Create Reservation");
        dialog.setHeaderText("Enter reservation details:");

        Label nameLabel = new Label("Guest Name:");
        TextField nameField = new TextField();
        Label phoneLabel = new Label("Phone:");
        TextField phoneField = new TextField();
        Label emailLabel = new Label("Email:");
        TextField emailField = new TextField();
        Label roomTypeLabel = new Label("Room Type:");
        ComboBox<String> roomTypeBox = new ComboBox<>();
        roomTypeBox.getItems().addAll("Single", "Double", "Suite");
        Label checkInLabel = new Label("Check-In:");
        DatePicker checkInPicker = new DatePicker(LocalDate.now().plusDays(1));
        Label checkOutLabel = new Label("Check-Out:");
        DatePicker checkOutPicker = new DatePicker(LocalDate.now().plusDays(2));
        Label guestsLabel = new Label("Number of Guests:");
        TextField guestsField = new TextField("1");

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
        grid.add(roomTypeLabel, 0, 3);
        grid.add(roomTypeBox, 1, 3);
        grid.add(checkInLabel, 0, 4);
        grid.add(checkInPicker, 1, 4);
        grid.add(checkOutLabel, 0, 5);
        grid.add(checkOutPicker, 1, 5);
        grid.add(guestsLabel, 0, 6);
        grid.add(guestsField, 1, 6);

        dialog.getDialogPane().setContent(grid);
        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                String name = nameField.getText().trim();
                String phone = phoneField.getText().trim();
                String email = emailField.getText().trim();
                String roomType = roomTypeBox.getValue();
                LocalDate checkIn = checkInPicker.getValue();
                LocalDate checkOut = checkOutPicker.getValue();
                String guestsStr = guestsField.getText().trim();

                if (name.isEmpty() || phone.isEmpty() || email.isEmpty() || roomType == null ||
                        checkIn == null || checkOut == null || guestsStr.isEmpty()) {
                    showAlert("All fields are required.");
                    return null;
                }
                if (!checkOut.isAfter(checkIn)) {
                    showAlert("Check-out must be after check-in.");
                    return null;
                }
                if (!controller.isRoomAvailable(roomType, checkIn, checkOut)) {
                    showAlert("No rooms available for selected dates and type.");
                    return null;
                }
                int guests;
                try {
                    guests = Integer.parseInt(guestsStr);
                    if (guests <= 0)
                        throw new NumberFormatException();
                } catch (NumberFormatException e) {
                    showAlert("Number of guests must be a positive integer.");
                    return null;
                }

                return new Reservation(data.size() + 1, name, phone, email, roomType, checkIn, checkOut, guests);
            }
            return null;
        });

        dialog.showAndWait().ifPresent(res -> {
            if (res != null) {
                controller.addReservation(res);
                refreshTable();
            }
        });
    }

    private void handleConfirm() {
        Reservation selected = table.getSelectionModel().getSelectedItem();
        if (selected != null && selected.getStatus() == Reservation.Status.PENDING) {
            controller.confirmReservation(selected.getReservationId());
            refreshTable();
        } else {
            showAlert("Select a pending reservation to confirm.");
        }
    }

    private void handleAssignRoom() {
        Reservation selected = table.getSelectionModel().getSelectedItem();
        if (selected != null && selected.getStatus() == Reservation.Status.CONFIRMED) {
            final Reservation currentRes = selected;
            List<Room> availableRooms = roomController.getAvailableRooms().stream()
                    .filter(r -> r.getType().equalsIgnoreCase(currentRes.getRoomType()))
                    .filter(r -> controller.isSpecificRoomAvailable(r.getRoomNumber(), currentRes.getCheckInDate(),
                            currentRes.getCheckOutDate()))
                    .toList();
            if (availableRooms.isEmpty()) {
                showAlert("No available rooms of the selected type.");
                return;
            }
            ChoiceDialog<Room> roomDialog = new ChoiceDialog<>(availableRooms.get(0), availableRooms);
            roomDialog.setTitle("Assign Room to Reservation");
            roomDialog.setHeaderText("Assigning " + selected.getRoomType() + " Room to " + selected.getGuestName());
            roomDialog.setContentText("Select Room:");
            roomDialog.showAndWait().ifPresent(room -> {
                controller.assignRoom(selected.getReservationId(), room.getRoomNumber());
                refreshTable();
            });
        } else {
            showAlert("Select a confirmed reservation to assign a room.");
        }
    }

    private void handleModify() {
        Reservation selected = table.getSelectionModel().getSelectedItem();
        if (selected != null && selected.getStatus() == Reservation.Status.PENDING) {
            // For simplicity, allow changing dates and room type
            DatePicker newCheckIn = new DatePicker(selected.getCheckInDate());
            DatePicker newCheckOut = new DatePicker(selected.getCheckOutDate());
            ComboBox<String> newRoomType = new ComboBox<>();
            newRoomType.getItems().addAll("Single", "Double", "Suite");
            newRoomType.setValue(selected.getRoomType());

            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Modify Reservation");
            dialog.setHeaderText("Update details:");

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));
            grid.add(new Label("Check-In:"), 0, 0);
            grid.add(newCheckIn, 1, 0);
            grid.add(new Label("Check-Out:"), 0, 1);
            grid.add(newCheckOut, 1, 1);
            grid.add(new Label("Room Type:"), 0, 2);
            grid.add(newRoomType, 1, 2);

            dialog.getDialogPane().setContent(grid);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            dialog.showAndWait().ifPresent(result -> {
                LocalDate ci = newCheckIn.getValue();
                LocalDate co = newCheckOut.getValue();
                String rt = newRoomType.getValue();
                if (ci != null && co != null && rt != null && co.isAfter(ci)) {
                    controller.modifyReservation(selected.getReservationId(), ci, co, rt);
                    refreshTable();
                } else {
                    showAlert("Invalid dates or room type.");
                }
            });
        } else {
            showAlert("Select a pending reservation to modify.");
        }
    }

    private void handleCancel() {
        Reservation selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Cancel reservation for " + selected.getGuestName() + "?",
                    ButtonType.YES, ButtonType.NO);
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    controller.cancelReservation(selected.getReservationId());
                    refreshTable();
                }
            });
        } else {
            showAlert("Select a reservation to cancel.");
        }
    }

    private void handleCheckIn() {
        Reservation selected = table.getSelectionModel().getSelectedItem();
        if (selected != null && selected.getStatus() == Reservation.Status.CONFIRMED
                && selected.getRoomNumber() != null) {
            controller.checkInFromReservation(selected.getReservationId());
            refreshTable();
            showAlert("Reservation checked in successfully.");
        } else {
            showAlert("Select a confirmed reservation with assigned room to check in.");
        }
    }

    private void showReports() {
        TabPane tabPane = new TabPane();

        // Upcoming reservations
        Tab upcomingTab = new Tab("Upcoming Reservations");
        TableView<Reservation> upcomingTable = new TableView<>();
        TableColumn<Reservation, String> uNameCol = new TableColumn<>("Guest Name");
        uNameCol.setCellValueFactory(new PropertyValueFactory<>("guestName"));
        TableColumn<Reservation, LocalDate> uCheckInCol = new TableColumn<>("Check-In");
        uCheckInCol.setCellValueFactory(new PropertyValueFactory<>("checkInDate"));
        TableColumn<Reservation, String> uRoomTypeCol = new TableColumn<>("Room Type");
        uRoomTypeCol.setCellValueFactory(new PropertyValueFactory<>("roomType"));
        upcomingTable.getColumns().add(uNameCol);
        upcomingTable.getColumns().add(uCheckInCol);
        upcomingTable.getColumns().add(uRoomTypeCol);
        upcomingTable.getItems().setAll(controller.getUpcomingReservations());
        upcomingTab.setContent(upcomingTable);

        // Cancelled reservations
        Tab cancelledTab = new Tab("Cancelled Reservations");
        TableView<Reservation> cancelledTable = new TableView<>();
        TableColumn<Reservation, String> cNameCol = new TableColumn<>("Guest Name");
        cNameCol.setCellValueFactory(new PropertyValueFactory<>("guestName"));
        TableColumn<Reservation, LocalDate> cCheckInCol = new TableColumn<>("Check-In");
        cCheckInCol.setCellValueFactory(new PropertyValueFactory<>("checkInDate"));
        cancelledTable.getColumns().add(cNameCol);
        cancelledTable.getColumns().add(cCheckInCol);
        cancelledTable.getItems().setAll(controller.getCancelledReservations());
        cancelledTab.setContent(cancelledTable);

        tabPane.getTabs().addAll(upcomingTab, cancelledTab);

        Alert dialog = new Alert(Alert.AlertType.INFORMATION);
        dialog.setTitle("Reservation Reports");
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
