package com.orionhotel.ui;

import com.orionhotel.controller.RoomController;
import com.orionhotel.model.Room;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import java.util.List;

public class RoomDashboard extends Application {
    private RoomController controller = new RoomController();

    @Override
    public void start(Stage primaryStage) {
        // Initialize some sample rooms
        controller.addRoom(new Room(101, "Single"));
        controller.addRoom(new Room(102, "Double"));
        controller.addRoom(new Room(103, "Suite"));
        controller.addRoom(new Room(201, "Single"));
        controller.addRoom(new Room(202, "Double"));

        // Create UI
        VBox root = new VBox(10);
        root.setPadding(new javafx.geometry.Insets(10));

        // For each room, create a HBox with info and buttons
        List<Room> allRooms = controller.getAllRooms();
        for (Room room : allRooms) {
            HBox roomBox = new HBox(10);
            Label number = new Label("Room " + room.getRoomNumber());
            Label type = new Label(room.getType());
            Rectangle status = new Rectangle(20, 20);
            updateStatus(status, room);

            Button book = new Button("Book");
            book.setOnAction(e -> {
                if (controller.bookRoom(room.getRoomNumber())) {
                    updateStatus(status, room);
                }
            });

            Button checkout = new Button("Checkout");
            checkout.setOnAction(e -> {
                if (controller.checkoutRoom(room.getRoomNumber())) {
                    updateStatus(status, room);
                }
            });

            Button clean = new Button("Mark Clean");
            clean.setOnAction(e -> {
                controller.markCleaned(room.getRoomNumber());
                updateStatus(status, room);
            });

            Button maintenance = new Button("Mark Maintenance");
            maintenance.setOnAction(e -> {
                controller.markMaintenance(room.getRoomNumber());
                updateStatus(status, room);
            });

            roomBox.getChildren().addAll(number, type, status, book, checkout, clean, maintenance);
            root.getChildren().add(roomBox);
        }

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("Room Dashboard - Orion Hotel Oracle");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void updateStatus(Rectangle status, Room room) {
        if (room.needsMaintenance()) {
            status.setFill(Color.GRAY);
        } else if (!room.isClean()) {
            status.setFill(Color.YELLOW);
        } else if (room.isOccupied()) {
            status.setFill(Color.RED);
        } else {
            status.setFill(Color.GREEN);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}