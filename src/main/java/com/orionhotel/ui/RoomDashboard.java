package com.orionhotel.ui;

import com.orionhotel.controller.RoomController;
import com.orionhotel.model.Room;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.List;
import java.util.stream.Collectors;

public class RoomDashboard {
    private RoomController roomController = new RoomController();
    private VBox rootPane;
    private TilePane roomGrid;
    private ComboBox<String> statusFilter;
    private ComboBox<String> typeFilter;

    public RoomDashboard() {
        initializeUI();
    }

    private void initializeUI() {
        rootPane = new VBox(15);
        rootPane.setPadding(new Insets(20));
        rootPane.setStyle("-fx-background-color: #f4f4f4;");

        // Header
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Room Management");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #333;");

        Button addRoomBtn = new Button("+ Add New Room");
        addRoomBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");
        addRoomBtn.setOnAction(e -> showAddRoomDialog());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(title, spacer, addRoomBtn);

        // Filter Bar
        HBox filters = new HBox(15);
        filters.setAlignment(Pos.CENTER_LEFT);
        filters.setPadding(new Insets(10));
        filters.setStyle(
                "-fx-background-color: white; -fx-background-radius: 5; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 1);");

        statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("All Statuses", "AVAILABLE", "OCCUPIED", "DIRTY", "MAINTENANCE");
        statusFilter.setValue("All Statuses");
        statusFilter.setOnAction(e -> refreshRoomList());

        typeFilter = new ComboBox<>();
        typeFilter.getItems().addAll("All Types", "Single", "Double", "Suite");
        typeFilter.setValue("All Types");
        typeFilter.setOnAction(e -> refreshRoomList());

        Button refreshBtn = new Button("Refresh Dashboard");
        refreshBtn.setOnAction(e -> refreshRoomList());

        filters.getChildren().addAll(new Label("Filter by:"), statusFilter, typeFilter,
                new Separator(javafx.geometry.Orientation.VERTICAL), refreshBtn);

        // Room Grid
        roomGrid = new TilePane();
        roomGrid.setHgap(20);
        roomGrid.setVgap(20);
        roomGrid.setPadding(new Insets(10, 0, 10, 0));
        roomGrid.setPrefColumns(4);

        ScrollPane scrollPane = new ScrollPane(roomGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        rootPane.getChildren().addAll(header, filters, scrollPane);
        refreshRoomList();
    }

    public VBox getRootPane() {
        return rootPane;
    }

    private void refreshRoomList() {
        roomGrid.getChildren().clear();
        List<Room> allRooms = roomController.getAllRooms();

        String statusF = statusFilter.getValue();
        String typeF = typeFilter.getValue();

        List<Room> filtered = allRooms.stream()
                .filter(r -> statusF.equals("All Statuses") || r.getStatus().name().equals(statusF))
                .filter(r -> typeF.equals("All Types") || r.getType().equalsIgnoreCase(typeF))
                .collect(Collectors.toList());

        if (filtered.isEmpty()) {
            Label empty = new Label("No rooms found matching criteria.");
            empty.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
            roomGrid.getChildren().add(empty);
        } else {
            for (Room room : filtered) {
                roomGrid.getChildren().add(createRoomCard(room));
            }
        }
    }

    private VBox createRoomCard(Room room) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setPrefWidth(220);
        card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 2);");

        // Status Indicator
        HBox topRow = new HBox(10);
        topRow.setAlignment(Pos.CENTER_LEFT);
        Circle statusDot = new Circle(5);
        Label statusLabel = new Label(room.getStatus().name());
        statusLabel.setStyle("-fx-font-size: 10px; -fx-font-weight: bold;");

        Color statusColor = Color.GRAY;
        switch (room.getStatus()) {
            case AVAILABLE:
                statusColor = Color.GREEN;
                break;
            case OCCUPIED:
                statusColor = Color.RED;
                break;
            case DIRTY:
                statusColor = Color.ORANGE;
                break;
            case MAINTENANCE:
                statusColor = Color.GRAY;
                break;
        }
        statusDot.setFill(statusColor);
        statusLabel.setTextFill(statusColor);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label typeLabel = new Label(room.getType());
        typeLabel.setStyle(
                "-fx-background-color: #eee; -fx-padding: 3 8; -fx-background-radius: 10; -fx-font-size: 10px;");

        topRow.getChildren().addAll(statusDot, statusLabel, spacer, typeLabel);

        // Room Number
        Label roomNumber = new Label("Room " + room.getRoomNumber());
        roomNumber.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #333;");

        // Price
        Label priceLabel = new Label(String.format("$%.2f / night", room.getPrice()));
        priceLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");

        // Actions
        HBox actions = new HBox(5);
        actions.setAlignment(Pos.CENTER);

        MenuButton actionsBtn = new MenuButton("Actions");
        actionsBtn.setStyle("-fx-base: #f0f0f0;");

        MenuItem bookItem = new MenuItem("Book Room");
        bookItem.setOnAction(e -> {
            if (roomController.bookRoom(room.getRoomNumber()))
                refreshRoomList();
            else
                showAlert("Cannot book room (must be AVAILABLE).");
        });

        MenuItem checkoutItem = new MenuItem("Check Out");
        checkoutItem.setOnAction(e -> {
            if (roomController.checkoutRoom(room.getRoomNumber()))
                refreshRoomList();
        });

        MenuItem cleanItem = new MenuItem("Mark Clean");
        cleanItem.setOnAction(e -> {
            roomController.markCleaned(room.getRoomNumber());
            refreshRoomList();
        });

        MenuItem maintainItem = new MenuItem("Maintenance");
        maintainItem.setOnAction(e -> {
            roomController.markMaintenance(room.getRoomNumber());
            refreshRoomList();
        });

        actionsBtn.getItems().addAll(bookItem, checkoutItem, cleanItem, maintainItem);

        actions.getChildren().add(actionsBtn);

        card.getChildren().addAll(topRow, roomNumber, priceLabel, new Separator(), actions);
        return card;
    }

    private void showAddRoomDialog() {
        Dialog<Room> dialog = new Dialog<>();
        dialog.setTitle("Add New Room");
        dialog.setHeaderText("Enter details for new room");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField numField = new TextField();
        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("Single", "Double", "Suite");
        TextField priceField = new TextField();

        grid.add(new Label("Room Number:"), 0, 0);
        grid.add(numField, 1, 0);
        grid.add(new Label("Type:"), 0, 1);
        grid.add(typeBox, 1, 1);
        grid.add(new Label("Price:"), 0, 2);
        grid.add(priceField, 1, 2);

        dialog.getDialogPane().setContent(grid);
        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == addButtonType) {
                try {
                    int num = Integer.parseInt(numField.getText());
                    double price = Double.parseDouble(priceField.getText());
                    String type = typeBox.getValue();
                    if (type != null) {
                        return new Room(num, type, price);
                    }
                } catch (Exception e) {
                    showAlert("Invalid input. Please check fields.");
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(room -> {
            if (room != null) {
                roomController.addRoom(room);
                refreshRoomList();
            }
        });
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        alert.showAndWait();
    }
}
