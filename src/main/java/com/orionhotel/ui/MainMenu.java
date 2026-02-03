package com.orionhotel.ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import com.orionhotel.model.Staff;
import com.orionhotel.model.Role;
import com.orionhotel.controller.RoomController;
import com.orionhotel.controller.BookingController;
import com.orionhotel.controller.HousekeepingController;
import com.orionhotel.controller.StaffController;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class MainMenu extends Application {

    private BorderPane mainLayout;
    private ScrollPane dashboardPane;
    private Staff currentUser;

    private com.orionhotel.controller.NotificationController notificationController;
    private com.orionhotel.controller.SettingsController settingsController;
    private RoomController roomController;
    private BookingController bookingController;
    private HousekeepingController housekeepingController;
    private StaffController staffController;
    private com.orionhotel.controller.BillingController billingController;
    private com.orionhotel.controller.InventoryController inventoryController;

    public MainMenu() {
    }

    public MainMenu(Staff currentUser) {
        this.currentUser = currentUser;
    }

    @Override
    public void start(Stage primaryStage) {
        initControllers();

        mainLayout = new BorderPane();
        mainLayout.setLeft(createSidebar());

        dashboardPane = createDashboard();
        mainLayout.setCenter(dashboardPane);

        Scene scene = new Scene(mainLayout, 1280, 800);
        primaryStage.setTitle("Orion Hotel Oracle - Dashboard");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void initControllers() {
        notificationController = new com.orionhotel.controller.NotificationController();
        roomController = new RoomController();
        settingsController = new com.orionhotel.controller.SettingsController();
        bookingController = new BookingController(roomController);
        staffController = new StaffController();
        housekeepingController = new HousekeepingController(roomController, staffController);
        inventoryController = new com.orionhotel.controller.InventoryController();
        billingController = new com.orionhotel.controller.BillingController(
                new com.orionhotel.controller.GuestController(roomController));
        billingController.setSettingsController(settingsController);
        billingController.setRoomController(roomController);
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(15);
        sidebar.setPadding(new Insets(20, 10, 20, 10));
        sidebar.setPrefWidth(220);

        // Modern Gradient Background
        Stop[] stops = new Stop[] { new Stop(0, Color.web("#2c3e50")), new Stop(1, Color.web("#000000")) };
        LinearGradient bg = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, stops);
        sidebar.setBackground(new Background(new BackgroundFill(bg, CornerRadii.EMPTY, Insets.EMPTY)));

        Text logo = new Text("ORION ORACLE");
        logo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        logo.setFill(Color.WHITE);
        HBox logoBox = new HBox(logo);
        logoBox.setAlignment(Pos.CENTER);
        logoBox.setPadding(new Insets(0, 0, 30, 0));

        VBox navItems = new VBox(5);
        navItems.getChildren().addAll(
                createNavButton("\uD83C\uDFE0 Dashboard", e -> showHome()),
                createNavButton("\uD83D\uDECC Room Dashboard", e -> showRoomDashboard()),
                createNavButton("\uD83D\uDCC5 Bookings", e -> showReservations()),
                createNavButton("\uD83D\uDC65 Guests", e -> showGuestManagement()),
                createNavButton("\uD83D\uDCE6 Inventory", e -> showInventoryManagement()),
                createNavButton("\uD83E\uDDF9 Housekeeping", e -> showHousekeeping()),
                createNavButton("\uD83D\uDCC8 Analytics", e -> showAnalytics()),
                createNavButton("\uD83C\uDFAB Billing", e -> showBilling()),
                createNavButton("\uD83D\uDC54 Staff", e -> showStaffManagement()),
                createNavButton("\uD83D\uDD14 Notifications", e -> showNotifications()),
                createNavButton("\u2699 Settings", e -> showSettings()));

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button logoutBtn = createNavButton("\uD83D\uDEAA Logout", e -> {
            ((Stage) mainLayout.getScene().getWindow()).close();
            new LoginUI().start(new Stage());
        });
        logoutBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #e74c3c; -fx-font-weight: bold;");

        sidebar.getChildren().addAll(logoBox, navItems, spacer, logoutBtn);
        return sidebar;
    }

    private Button createNavButton(String text, javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPadding(new Insets(10, 15, 10, 15));
        btn.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
        btn.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #ecf0f1; -fx-cursor: hand; -fx-background-radius: 5;");

        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: rgba(255,255,255,0.1); -fx-text-fill: #ffffff; -fx-cursor: hand; -fx-background-radius: 5;"));
        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #ecf0f1; -fx-cursor: hand; -fx-background-radius: 5;"));

        btn.setOnAction(handler);
        return btn;
    }

    private ScrollPane createDashboard() {
        VBox content = new VBox(30);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: #f4f7f6;");

        // Header
        HBox header = new HBox();
        VBox greeting = new VBox(5);
        Text welcome = new Text("Welcome back, " + (currentUser != null ? currentUser.getFullName() : "Admin"));
        welcome.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        Text date = new Text(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy")));
        date.setFont(Font.font("Segoe UI", 16));
        date.setFill(Color.GRAY);
        greeting.getChildren().addAll(welcome, date);
        header.getChildren().add(greeting);

        // Stats Cards
        FlowPane statsPane = new FlowPane(20, 20);
        statsPane.getChildren().addAll(
                createStatCard("Total Rooms", String.valueOf(roomController.getAllRooms().size()), "#3498db"),
                createStatCard("Available", String.valueOf(roomController.getAvailableRooms().size()), "#2ecc71"),
                createStatCard("Pending Tasks", String.valueOf(housekeepingController.getAllTasks().stream()
                        .filter(t -> t.getStatus() != com.orionhotel.model.HousekeepingTask.Startus.COMPLETED).count()),
                        "#f1c40f"),
                createStatCard("Active Bookings", String.valueOf(bookingController.getAllReservations().stream()
                        .filter(r -> r.getStatus() == com.orionhotel.model.Reservation.Status.CONFIRMED).count()),
                        "#9b59b6"));

        // Recent Activity and Notifications
        HBox lowerBody = new HBox(30);
        VBox alertsBox = new VBox(15);
        alertsBox.setPrefWidth(400);
        Text alertsTitle = new Text("Recent Notifications");
        alertsTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));

        VBox notificationsList = new VBox(10);
        var notifs = notificationController.getNotifications(currentUser != null ? currentUser.getRole() : Role.ADMIN);
        for (int i = 0; i < Math.min(notifs.size(), 5); i++) {
            notificationsList.getChildren().add(createNotificationItem(notifs.get(i)));
        }
        alertsBox.getChildren().addAll(alertsTitle, notificationsList);

        VBox quickActions = new VBox(15);
        Text actionsTitle = new Text("Quick Actions");
        actionsTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        Button bookBtn = new Button("+ New Reservation");
        bookBtn.setPrefWidth(200);
        bookBtn.setStyle(
                "-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10;");
        bookBtn.setOnAction(e -> showReservations());

        Button guestBtn = new Button("+ Add Guest");
        guestBtn.setPrefWidth(200);
        guestBtn.setStyle(
                "-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10;");
        guestBtn.setOnAction(e -> showGuestManagement());

        quickActions.getChildren().addAll(actionsTitle, bookBtn, guestBtn);
        lowerBody.getChildren().addAll(alertsBox, quickActions);

        content.getChildren().addAll(header, statsPane, lowerBody);

        ScrollPane sp = new ScrollPane(content);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background-color: transparent; -fx-background: #f4f7f6;");
        return sp;
    }

    private VBox createStatCard(String title, String value, String color) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20));
        card.setPrefSize(200, 120);
        card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");

        Text tLabel = new Text(title);
        tLabel.setFont(Font.font("Segoe UI", 14));
        tLabel.setFill(Color.GRAY);

        Text tValue = new Text(value);
        tValue.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        tValue.setFill(Color.web(color));

        card.getChildren().addAll(tLabel, tValue);
        return card;
    }

    private HBox createNotificationItem(com.orionhotel.model.Notification n) {
        HBox item = new HBox(10);
        item.setPadding(new Insets(10));
        item.setStyle("-fx-background-color: white; -fx-background-radius: 5;");
        item.setAlignment(Pos.CENTER_LEFT);

        Circle dot = new Circle(4, n.isRead() ? Color.LIGHTGRAY : Color.web("#e74c3c"));
        Text msg = new Text(n.getMessage());
        msg.setWrappingWidth(300);
        msg.setFont(Font.font("Segoe UI", 13));

        item.getChildren().addAll(dot, msg);
        return item;
    }

    private void showHome() {
        mainLayout.setCenter(createDashboard());
    }

    private void showGuestManagement() {
        mainLayout.setCenter(new GuestUI().getRootPane());
    }

    private void showInventoryManagement() {
        InventoryUI ui = new InventoryUI();
        ui.setNotificationController(notificationController);
        mainLayout.setCenter(ui.getRootPane());
    }

    private void showRoomDashboard() {
        mainLayout.setCenter(new RoomDashboard().getRootPane());
    }

    private void showReservations() {
        ReservationUI ui = new ReservationUI();
        ui.setNotificationController(notificationController);
        mainLayout.setCenter(ui.getRootPane());
    }

    private void showBilling() {
        BillingUI ui = new BillingUI();
        ui.setControllers(settingsController, roomController);
        mainLayout.setCenter(ui.getRootPane());
    }

    private void showStaffManagement() {
        mainLayout.setCenter(new StaffUI().getRootPane());
    }

    private void showHousekeeping() {
        HousekeepingUI ui = new HousekeepingUI();
        ui.setNotificationController(notificationController);
        mainLayout.setCenter(ui.getRootPane());
    }

    private void showAnalytics() {
        mainLayout.setCenter(new AnalyticsUI(roomController, billingController, inventoryController, bookingController)
                .getRootPane());
    }

    private void showNotifications() {
        new NotificationUI(notificationController, currentUser != null ? currentUser.getRole() : Role.ADMIN).show();
    }

    private void showSettings() {
        mainLayout.setCenter(new SettingsUI(settingsController).getRootPane());
    }

    public static void main(String[] args) {
        launch(args);
    }
}