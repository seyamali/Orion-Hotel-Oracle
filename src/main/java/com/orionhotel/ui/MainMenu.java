package com.orionhotel.ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class MainMenu extends Application {

    private BorderPane mainLayout;
    private StackPane homePane;

    @Override
    public void start(Stage primaryStage) {

        // Create main layout
        mainLayout = new BorderPane();

        // Create navigation bar
        HBox navBar = createNavBar();

        // Create home pane
        homePane = createHomePane();

        // Set initial content
        mainLayout.setTop(navBar);
        mainLayout.setCenter(homePane);

        // Scene
        Scene scene = new Scene(mainLayout, 1200, 700);
        primaryStage.setTitle("Orion Hotel Oracle - Hotel Management System");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private HBox createNavBar() {
        Button homeBtn = new Button("Home");
        homeBtn.setOnAction(e -> showHome());

        Button guestBtn = new Button("Guest Management");
        guestBtn.setOnAction(e -> showGuestManagement());

        Button inventoryBtn = new Button("Inventory Management");
        inventoryBtn.setOnAction(e -> showInventoryManagement());

        Button roomBtn = new Button("Room Dashboard");
        roomBtn.setOnAction(e -> showRoomDashboard());

        HBox navBar = new HBox(15, homeBtn, guestBtn, inventoryBtn, roomBtn);
        navBar.setPadding(new Insets(10));
        navBar.setAlignment(Pos.CENTER);
        navBar.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc; -fx-border-width: 0 0 1 0;");

        return navBar;
    }

    private StackPane createHomePane() {
        Text title = new Text("Orion Hotel Oracle");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 32));

        Text subtitle = new Text("Hotel Management System");
        subtitle.setFont(Font.font("Arial", FontWeight.NORMAL, 18));

        Text welcome = new Text("Welcome! Use the navigation bar above to access different modules.");
        welcome.setFont(Font.font("Arial", FontWeight.NORMAL, 14));

        StackPane homePane = new StackPane();
        homePane.getChildren().addAll(title, subtitle, welcome);

        // Position elements
        StackPane.setAlignment(title, Pos.TOP_CENTER);
        StackPane.setMargin(title, new Insets(100, 0, 0, 0));

        StackPane.setAlignment(subtitle, Pos.TOP_CENTER);
        StackPane.setMargin(subtitle, new Insets(150, 0, 0, 0));

        StackPane.setAlignment(welcome, Pos.CENTER);

        return homePane;
    }

    private void showHome() {
        mainLayout.setCenter(homePane);
    }

    private void showGuestManagement() {
        GuestUI guestUI = new GuestUI();
        mainLayout.setCenter(guestUI.getRootPane());
    }

    private void showInventoryManagement() {
        InventoryUI inventoryUI = new InventoryUI();
        mainLayout.setCenter(inventoryUI.getRootPane());
    }

    private void showRoomDashboard() {
        RoomDashboard roomDashboard = new RoomDashboard();
        mainLayout.setCenter(roomDashboard.getRootPane());
    }

    public static void main(String[] args) {
        launch(args);
    }
}