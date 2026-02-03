package com.orionhotel.ui;

import com.orionhotel.controller.*;
import com.orionhotel.model.Room;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Map;

public class AnalyticsUI {
    private RoomController roomController;
    private BillingController billingController;
    private InventoryController inventoryController;
    private BookingController bookingController;
    private VBox rootPane;

    public AnalyticsUI(RoomController rc, BillingController bc, InventoryController ic, BookingController bkc) {
        this.roomController = rc;
        this.billingController = bc;
        this.inventoryController = ic;
        this.bookingController = bkc;
        initializeUI();
    }

    private void initializeUI() {
        // Header
        Label headerLabel = new Label("Analytics & Insights");
        headerLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        TabPane tabPane = new TabPane();
        tabPane.getTabs().addAll(createOverviewTab(), createOccupancyTab(), createFinancialTab(), createInventoryTab());
        tabPane.setStyle("-fx-tab-min-width: 150;");

        rootPane = new VBox(20, headerLabel, tabPane);
        rootPane.setPadding(new Insets(30));
        rootPane.setStyle("-fx-background-color: #f4f7f6;");
    }

    private Tab createOverviewTab() {
        Tab tab = new Tab("Property Overview");
        VBox content = new VBox(30);
        content.setPadding(new Insets(20));

        // Key Metrics Row
        HBox metricsRow = new HBox(20);
        metricsRow.setAlignment(Pos.CENTER);

        int totalRooms = roomController.getAllRooms().size();
        int occupied = (int) roomController.getAllRooms().stream()
                .filter(r -> r.getStatus() == Room.RoomStatus.OCCUPIED).count();
        double occupancyRate = totalRooms > 0 ? (double) occupied / totalRooms * 100 : 0;

        double currentMonthRevenue = billingController.getMonthlyRevenue(LocalDate.now().getYear(),
                LocalDate.now().getMonthValue());
        int lowStockCount = inventoryController.getLowStockItems().size();
        int canceledBookings = (int) bookingController.getAllReservations().stream()
                .filter(r -> r.getStatus() == com.orionhotel.model.Reservation.Status.CANCELLED).count();

        metricsRow.getChildren().addAll(
                createMetricCard("Occupancy Rate", String.format("%.1f%%", occupancyRate), "#3498db"),
                createMetricCard("Monthly Revenue", String.format("$%.2f", currentMonthRevenue), "#27ae60"),
                createMetricCard("Low Stock Items", String.valueOf(lowStockCount), "#e67e22"),
                createMetricCard("Cancelled Bookings", String.valueOf(canceledBookings), "#e74c3c"));

        // Recent Bookings Summary or Quick Alerts
        VBox alertsBox = new VBox(15);
        alertsBox.setPadding(new Insets(20));
        alertsBox.setStyle(
                "-fx-background-color: white; -fx-border-radius: 10; -fx-background-radius: 10; -fx-border-color: #dee2e6;");

        Label alertsTitle = new Label("Operational Status Summary");
        alertsTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        TextArea summaryText = new TextArea();
        summaryText.setEditable(false);
        summaryText.setWrapText(true);
        summaryText.setPrefHeight(200);

        StringBuilder sb = new StringBuilder();
        sb.append("Property Health Check:\n");
        sb.append("----------------------------\n");
        sb.append(String.format("• Room Status: %d total rooms, %d occupied, %d available.\n", totalRooms, occupied,
                totalRooms - occupied));
        sb.append(String.format("• Revenue: Monthly target set, currently at $%.2f.\n", currentMonthRevenue));
        sb.append(String.format("• Inventory: %d items need restocking immediately.\n", lowStockCount));
        sb.append("• Housekeeping: Active tasks are being monitored.\n");

        summaryText.setText(sb.toString());
        alertsBox.getChildren().addAll(alertsTitle, summaryText);

        content.getChildren().addAll(metricsRow, alertsBox);
        tab.setContent(content);
        return tab;
    }

    private Tab createOccupancyTab() {
        Tab tab = new Tab("Occupancy Analytics");
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.CENTER);

        Map<Room.RoomStatus, Long> counts = roomController.getRoomStatusCounts();
        PieChart pieChart = new PieChart();
        pieChart.setTitle("Room Status Distribution");

        for (Map.Entry<Room.RoomStatus, Long> entry : counts.entrySet()) {
            pieChart.getData().add(new PieChart.Data(entry.getKey().name(), entry.getValue()));
        }

        content.getChildren().add(pieChart);
        tab.setContent(content);
        return tab;
    }

    private Tab createFinancialTab() {
        Tab tab = new Tab("Financial Performance");
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Revenue Trends (Last 6 Months)");
        xAxis.setLabel("Month");
        yAxis.setLabel("Revenue ($)");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Revenue");

        // Generate data for last 6 months
        LocalDate now = LocalDate.now();
        for (int i = 5; i >= 0; i--) {
            LocalDate monthDate = now.minusMonths(i);
            String monthName = monthDate.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            double revenue = billingController.getMonthlyRevenue(monthDate.getYear(), monthDate.getMonthValue());
            series.getData().add(new XYChart.Data<>(monthName, revenue));
        }

        barChart.getData().add(series);
        content.getChildren().add(barChart);
        tab.setContent(content);
        return tab;
    }

    private Tab createInventoryTab() {
        Tab tab = new Tab("Inventory Usage");
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> usageChart = new BarChart<>(xAxis, yAxis);
        usageChart.setTitle("Top Used Inventory Items");
        xAxis.setLabel("Item Name");
        yAxis.setLabel("Quantity Consumed");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Consumption Amount");

        var usageData = inventoryController.getMostUsedItems();
        for (var usage : usageData) {
            series.getData().add(new XYChart.Data<>(usage.itemName, usage.totalConsumed));
        }

        usageChart.getData().add(series);
        content.getChildren().add(usageChart);
        tab.setContent(content);
        return tab;
    }

    private VBox createMetricCard(String title, String value, String color) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20));
        card.setPrefSize(250, 150);
        card.setAlignment(Pos.CENTER);
        card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 15, 0, 0, 5); -fx-border-color: #f1f2f6; -fx-border-radius: 12;");

        Label tLabel = new Label(title);
        tLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 14px;");

        Label tValue = new Label(value);
        tValue.setStyle("-fx-font-weight: bold; -fx-font-size: 28px; -fx-text-fill: " + color + ";");

        card.getChildren().addAll(tLabel, tValue);
        return card;
    }

    public VBox getRootPane() {
        return rootPane;
    }
}