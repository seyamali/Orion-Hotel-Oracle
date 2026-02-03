package com.orionhotel.ui;

import com.orionhotel.controller.BillingController;
import com.orionhotel.controller.GuestController;
import com.orionhotel.controller.SettingsController;
import com.orionhotel.controller.RoomController;
import com.orionhotel.model.Bill;
import com.orionhotel.model.Guest;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.List;

public class BillingUI {

    private BillingController controller;
    private GuestController guestController;
    private RoomController roomController;

    private TableView<Bill.ServiceCharge> chargesTable = new TableView<>();
    private ObservableList<Bill.ServiceCharge> chargesData = FXCollections.observableArrayList();
    private ComboBox<Guest> guestCombo = new ComboBox<>();
    private VBox rootPane;

    private Label guestNameLabel = new Label("N/A");
    private Label roomChargesLabel = new Label("$0.00");
    private Label taxesLabel = new Label("$0.00");
    private Label discountLabel = new Label("$0.00");
    private Label grandTotalLabel = new Label("$0.00");
    private Label statusLabel = new Label("UNPAID");

    public BillingUI() {
        roomController = new RoomController();
        guestController = new GuestController(roomController);
        controller = new BillingController(guestController);
        initializeUI();
    }

    public void setControllers(SettingsController sc, RoomController rc) {
        if (controller != null) {
            this.roomController = rc;
            controller.setSettingsController(sc);
            controller.setRoomController(rc);
        }
    }

    private void initializeUI() {
        // Header
        Label headerLabel = new Label("Billing & Payments");
        headerLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Guest selection
        Label guestSelectLabel = new Label("Select Guest:");
        guestSelectLabel.setStyle("-fx-font-weight: bold;");
        guestCombo.setPromptText("Choose checked-in guest...");
        guestCombo.setMaxWidth(Double.MAX_VALUE);
        updateGuestCombo();
        guestCombo.setOnAction(e -> loadBill());

        VBox guestSelectorBox = new VBox(5, guestSelectLabel, guestCombo);
        guestSelectorBox.setPadding(new Insets(10));
        guestSelectorBox.setStyle(
                "-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-radius: 5px; -fx-background-radius: 5px;");

        // Bill Summary Card
        javafx.scene.layout.GridPane billCard = new javafx.scene.layout.GridPane();
        billCard.setHgap(20);
        billCard.setVgap(10);
        billCard.setPadding(new Insets(20));
        billCard.setStyle(
                "-fx-background-color: white; -fx-border-color: #dee2e6; -fx-border-radius: 10px; -fx-background-radius: 10px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");

        addInfoRow(billCard, "Guest Name:", guestNameLabel, 0);
        addInfoRow(billCard, "Room Charges:", roomChargesLabel, 1);
        addInfoRow(billCard, "Taxes:", taxesLabel, 2);
        addInfoRow(billCard, "Discount:", discountLabel, 3);

        Label totalText = new Label("Grand Total:");
        totalText.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        grandTotalLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");
        billCard.add(totalText, 0, 4);
        billCard.add(grandTotalLabel, 1, 4);

        addInfoRow(billCard, "Payment Status:", statusLabel, 5);

        // Charges table
        chargesTable.setPlaceholder(new Label("No service charges recorded."));
        TableColumn<Bill.ServiceCharge, String> typeCol = new TableColumn<>("Service Type");
        typeCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().serviceType));
        typeCol.setPrefWidth(200);

        TableColumn<Bill.ServiceCharge, Double> amountCol = new TableColumn<>("Amount ($)");
        amountCol.setCellValueFactory(
                c -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().amount).asObject());
        amountCol.setPrefWidth(100);

        TableColumn<Bill.ServiceCharge, LocalDate> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().date));
        dateCol.setPrefWidth(120);

        chargesTable.getColumns().setAll(List.of(typeCol, amountCol, dateCol));
        chargesTable.setItems(chargesData);
        chargesTable.setPrefHeight(250);

        // Buttons Section
        Button generateBillBtn = createBtn("Generate Bill", "#3498db");
        generateBillBtn.setOnAction(e -> generateBill());

        Button addServiceBtn = createBtn("Add Service", "#2ecc71");
        addServiceBtn.setOnAction(e -> addService());

        Button applyDiscountBtn = createBtn("Apply Discount", "#e67e22");
        applyDiscountBtn.setOnAction(e -> applyDiscount());

        Button receivePaymentBtn = createBtn("Receive Payment", "#27ae60");
        receivePaymentBtn.setOnAction(e -> receivePayment());

        Button generateInvoiceBtn = createBtn("View Invoice", "#34495e");
        generateInvoiceBtn.setOnAction(e -> generateInvoice());

        Button reportsBtn = createBtn("Reports", "#8e44ad");
        reportsBtn.setOnAction(e -> showReports());

        Button refreshBtn = createBtn("Refresh", "#95a5a6");
        refreshBtn.setOnAction(e -> {
            updateGuestCombo();
            loadBill();
        });

        HBox actionRow1 = new HBox(10, generateBillBtn, addServiceBtn, applyDiscountBtn, receivePaymentBtn);
        HBox actionRow2 = new HBox(10, generateInvoiceBtn, reportsBtn, refreshBtn);
        actionRow1.setPadding(new Insets(5, 0, 5, 0));
        actionRow2.setPadding(new Insets(5, 0, 5, 0));

        VBox actionsBox = new VBox(5, actionRow1, actionRow2);

        rootPane = new VBox(20, headerLabel, guestSelectorBox, billCard, new Label("Service Charges Breakdown:"),
                chargesTable, actionsBox);
        rootPane.setPadding(new Insets(30));
    }

    private void addInfoRow(javafx.scene.layout.GridPane grid, String label, Label value, int row) {
        Label l = new Label(label);
        l.setStyle("-fx-font-weight: bold;");
        grid.add(l, 0, row);
        grid.add(value, 1, row);
    }

    private Button createBtn(String text, String color) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + color
                + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-cursor: hand;");
        btn.setMinWidth(130);
        return btn;
    }

    public VBox getRootPane() {
        return rootPane;
    }

    private void updateGuestCombo() {
        List<Guest> checkedInGuests = guestController.getAllGuests().stream()
                .filter(g -> g.getStatus() == Guest.GuestStatus.CHECKED_IN)
                .toList();
        guestCombo.setItems(FXCollections.observableArrayList(checkedInGuests));
    }

    private void loadBill() {
        Guest selected = guestCombo.getValue();
        if (selected != null) {
            Bill bill = controller.getBillForGuest(selected.getGuestId());
            if (bill != null) {
                chargesData.setAll(bill.getServiceCharges());
                guestNameLabel.setText(bill.getGuestName());
                roomChargesLabel.setText(String.format("$%.2f", bill.getRoomCharges()));
                taxesLabel.setText(String.format("$%.2f", bill.getTaxes()));
                discountLabel.setText(String.format("$%.2f", bill.getDiscount()));
                grandTotalLabel.setText(String.format("$%.2f", bill.getTotalAmount()));
                statusLabel.setText(bill.getPaymentStatus().toString());
                statusLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " +
                        (bill.getPaymentStatus() == Bill.PaymentStatus.PAID ? "#27ae60" : "#e74c3c") + ";");
            } else {
                chargesData.clear();
                clearBillInfo();
            }
        }
    }

    private void clearBillInfo() {
        guestNameLabel.setText("N/A");
        roomChargesLabel.setText("$0.00");
        taxesLabel.setText("$0.00");
        discountLabel.setText("$0.00");
        grandTotalLabel.setText("$0.00");
        statusLabel.setText("N/A");
    }

    private void generateBill() {
        Guest selected = guestCombo.getValue();
        if (selected != null) {
            controller.generateBillForGuest(selected.getGuestId());
            loadBill();
        } else {
            showAlert("Select a guest first.");
        }
    }

    private void addService() {
        Guest selected = guestCombo.getValue();
        if (selected != null) {
            TextInputDialog serviceDialog = new TextInputDialog("Food");
            serviceDialog.setTitle("Add Service Charge");
            serviceDialog.setHeaderText("Enter service details:");
            serviceDialog.setContentText("Service Type:");

            serviceDialog.showAndWait().ifPresent(serviceType -> {
                TextInputDialog amountDialog = new TextInputDialog("10.00");
                amountDialog.setTitle("Add Service Charge");
                amountDialog.setHeaderText("Enter amount for " + serviceType + ":");
                amountDialog.setContentText("Amount:");

                amountDialog.showAndWait().ifPresent(amountStr -> {
                    try {
                        double amount = Double.parseDouble(amountStr);
                        controller.addServiceCharge(selected.getGuestId(), serviceType, amount);
                        loadBill();
                    } catch (NumberFormatException e) {
                        showAlert("Invalid amount.");
                    }
                });
            });
        } else {
            showAlert("Select a guest first.");
        }
    }

    private void applyDiscount() {
        Guest selected = guestCombo.getValue();
        if (selected != null) {
            TextInputDialog discountDialog = new TextInputDialog("0.00");
            discountDialog.setTitle("Apply Discount");
            discountDialog.setHeaderText("Enter discount amount:");
            discountDialog.setContentText("Discount:");

            discountDialog.showAndWait().ifPresent(discountStr -> {
                try {
                    double discount = Double.parseDouble(discountStr);
                    controller.applyDiscount(selected.getGuestId(), discount);
                    loadBill();
                } catch (NumberFormatException e) {
                    showAlert("Invalid discount amount.");
                }
            });
        } else {
            showAlert("Select a guest first.");
        }
    }

    private void generateInvoice() {
        Guest selected = guestCombo.getValue();
        if (selected != null) {
            Bill bill = controller.getBillForGuest(selected.getGuestId());
            if (bill != null) {
                Alert invoice = new Alert(Alert.AlertType.INFORMATION);
                invoice.setTitle("Invoice - " + bill.getGuestName());
                invoice.setHeaderText("Bill ID: " + bill.getBillId());
                invoice.setContentText(String.format(
                        "Room Charges: $%.2f\nService Charges: $%.2f\nTaxes: $%.2f\nDiscount: $%.2f\nTotal: $%.2f\nStatus: %s",
                        bill.getRoomCharges(),
                        bill.getServiceCharges().stream().mapToDouble(sc -> sc.amount).sum(),
                        bill.getTaxes(),
                        bill.getDiscount(),
                        bill.getTotalAmount(),
                        bill.getPaymentStatus()));
                invoice.showAndWait();
            } else {
                showAlert("No bill to generate invoice for.");
            }
        } else {
            showAlert("Select a guest first.");
        }
    }

    private void receivePayment() {
        Guest selected = guestCombo.getValue();
        if (selected != null) {
            Bill bill = controller.getBillForGuest(selected.getGuestId());
            if (bill != null) {
                Dialog<ButtonType> dialog = new Dialog<>();
                dialog.setTitle("Receive Payment");
                dialog.setHeaderText("Processing payment for " + bill.getGuestName());

                javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
                grid.setHgap(10);
                grid.setVgap(15);
                grid.setPadding(new Insets(20));

                TextField amountField = new TextField(String.valueOf(bill.getTotalAmount()));
                ComboBox<Bill.PaymentMethod> methodBox = new ComboBox<>(
                        FXCollections.observableArrayList(Bill.PaymentMethod.values()));
                methodBox.setValue(Bill.PaymentMethod.CARD);
                methodBox.setMaxWidth(Double.MAX_VALUE);

                Label totalLabel = new Label("Due Amount: $" + String.format("%.2f", bill.getTotalAmount()));
                totalLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");

                grid.add(totalLabel, 0, 0, 2, 1);
                grid.add(new Label("Payment Amount:"), 0, 1);
                grid.add(amountField, 1, 1);
                grid.add(new Label("Payment Method:"), 0, 2);
                grid.add(methodBox, 1, 2);

                dialog.getDialogPane().setContent(grid);
                dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

                dialog.showAndWait().ifPresent(result -> {
                    if (result == ButtonType.OK) {
                        try {
                            double amount = Double.parseDouble(amountField.getText());
                            Bill.PaymentMethod method = methodBox.getValue();
                            if (controller.processPayment(selected.getGuestId(), amount, method)) {
                                loadBill();
                                Alert success = new Alert(Alert.AlertType.INFORMATION,
                                        "Payment of $" + amount + " processed successfully via " + method);
                                success.showAndWait();
                            } else {
                                showAlert("Payment processing failed.");
                            }
                        } catch (NumberFormatException e) {
                            showAlert("Invalid amount entered.");
                        }
                    }
                });
            } else {
                showAlert("No active bill found for this guest.");
            }
        } else {
            showAlert("Please select a guest first.");
        }
    }

    private void showReports() {
        TabPane tabPane = new TabPane();
        tabPane.setStyle("-fx-tab-min-width: 150;");

        // Daily Revenue
        Tab dailyTab = new Tab("Daily Analytics");
        VBox dailyBox = new VBox(15);
        dailyBox.setPadding(new Insets(20));
        dailyBox.setAlignment(Pos.CENTER);

        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setStyle("-fx-font-size: 14px;");

        Label dailyRevenueValue = new Label("$0.00");
        dailyRevenueValue.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");

        Button calcDailyBtn = createBtn("Calculate Revenue", "#34495e");
        calcDailyBtn.setOnAction(e -> {
            double revenue = controller.getDailyRevenue(datePicker.getValue());
            dailyRevenueValue.setText(String.format("$%.2f", revenue));
        });

        VBox revenueCard = new VBox(10, new Label("Total Revenue for Selected Date:"), dailyRevenueValue);
        revenueCard.setAlignment(Pos.CENTER);
        revenueCard.setPadding(new Insets(20));
        revenueCard.setStyle(
                "-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-radius: 10; -fx-background-radius: 10;");

        dailyBox.getChildren().addAll(new Label("Select Transaction Date:"), datePicker, calcDailyBtn, revenueCard);
        dailyTab.setContent(dailyBox);

        // Monthly Revenue
        Tab monthlyTab = new Tab("Monthly Trends");
        VBox monthlyBox = new VBox(15);
        monthlyBox.setPadding(new Insets(20));
        monthlyBox.setAlignment(Pos.CENTER);

        HBox selectors = new HBox(10);
        selectors.setAlignment(Pos.CENTER);
        ComboBox<Integer> yearCombo = new ComboBox<>();
        yearCombo.getItems().addAll(2024, 2025, 2026, 2027);
        yearCombo.setValue(LocalDate.now().getYear());

        ComboBox<String> monthCombo = new ComboBox<>(FXCollections.observableArrayList(
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"));
        monthCombo.getSelectionModel().select(LocalDate.now().getMonthValue() - 1);
        selectors.getChildren().addAll(new Label("Year:"), yearCombo, new Label("Month:"), monthCombo);

        Label monthlyRevenueValue = new Label("$0.00");
        monthlyRevenueValue.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #2980b9;");

        Button calcMonthlyBtn = createBtn("Analyze Month", "#34495e");
        calcMonthlyBtn.setOnAction(e -> {
            int monthIdx = monthCombo.getSelectionModel().getSelectedIndex() + 1;
            double revenue = controller.getMonthlyRevenue(yearCombo.getValue(), monthIdx);
            monthlyRevenueValue.setText(String.format("$%.2f", revenue));
        });

        VBox monthlyCard = new VBox(10, new Label("Aggregate Monthly Revenue:"), monthlyRevenueValue);
        monthlyCard.setAlignment(Pos.CENTER);
        monthlyCard.setPadding(new Insets(20));
        monthlyCard.setStyle(
                "-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-radius: 10; -fx-background-radius: 10;");

        monthlyBox.getChildren().addAll(new Label("Filter by Period:"), selectors, calcMonthlyBtn, monthlyCard);
        monthlyTab.setContent(monthlyBox);

        // Outstanding Balances
        Tab outstandingTab = new Tab("Debtors List");
        VBox outstandingBox = new VBox(10);
        outstandingBox.setPadding(new Insets(10));

        TableView<Bill> outstandingTable = new TableView<>();
        TableColumn<Bill, String> guestCol = new TableColumn<>("Guest Name");
        guestCol.setCellValueFactory(new PropertyValueFactory<>("guestName"));
        guestCol.setPrefWidth(200);

        TableColumn<Bill, Double> totalCol = new TableColumn<>("Balance Due ($)");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        totalCol.setPrefWidth(150);

        TableColumn<Bill, String> statusCol = new TableColumn<>("Payment Status");
        statusCol.setCellValueFactory(
                c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getPaymentStatus().toString()));
        statusCol.setPrefWidth(150);

        outstandingTable.getColumns().setAll(List.of(guestCol, totalCol, statusCol));
        outstandingTable.getItems().setAll(controller.getOutstandingBalances());

        outstandingBox.getChildren().addAll(new Label("Unpaid and Partial Balances:"), outstandingTable);
        outstandingTab.setContent(outstandingBox);

        tabPane.getTabs().addAll(dailyTab, monthlyTab, outstandingTab);

        Alert dialog = new Alert(Alert.AlertType.NONE);
        dialog.setTitle("Financial Reports Hub");
        dialog.getDialogPane().setContent(tabPane);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.setResizable(true);
        dialog.getDialogPane().setPrefSize(600, 500);
        dialog.showAndWait();
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.showAndWait();
    }
}
