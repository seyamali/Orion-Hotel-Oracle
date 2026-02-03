package com.orionhotel.ui;

import com.orionhotel.controller.InventoryController;
import com.orionhotel.model.InventoryItem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.converter.IntegerStringConverter;

public class InventoryUI {

    private InventoryController controller = new InventoryController();
    private TableView<InventoryItem> table = new TableView<>();
    private ObservableList<InventoryItem> data = FXCollections.observableArrayList();
    private VBox rootPane;

    public InventoryUI() {
        initializeUI();
    }

    public void setNotificationController(com.orionhotel.controller.NotificationController nc) {
        controller.setNotificationController(nc);
    }

    private void initializeUI() {
        // Sample data removed. User starts fresh or uses SQL seeded data.

        refreshTable();

        table.setEditable(true);

        // Search and filter controls
        TextField searchField = new TextField();
        searchField.setPromptText("Search by name...");
        ComboBox<String> categoryFilter = new ComboBox<>();
        categoryFilter.setPromptText("Filter by category");
        categoryFilter.getItems().addAll("All", "Housekeeping Supplies", "Guest Amenities", "Food & Beverage Items",
                "Maintenance Tools & Parts", "Office Supplies");
        categoryFilter.getSelectionModel().selectFirst();
        CheckBox lowStockOnly = new CheckBox("Low Stock Only");

        // Filter logic
        searchField.textProperty()
                .addListener((obs, oldVal, newVal) -> applyFilters(searchField, categoryFilter, lowStockOnly));
        categoryFilter.valueProperty()
                .addListener((obs, oldVal, newVal) -> applyFilters(searchField, categoryFilter, lowStockOnly));
        lowStockOnly.selectedProperty()
                .addListener((obs, oldVal, newVal) -> applyFilters(searchField, categoryFilter, lowStockOnly));

        HBox filterBar = new HBox(10, searchField, categoryFilter, lowStockOnly);
        filterBar.setPadding(new Insets(10));

        // Columns
        TableColumn<InventoryItem, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("itemId"));

        TableColumn<InventoryItem, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        nameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        nameCol.setOnEditCommit(
                e -> e.getTableView().getItems().get(e.getTablePosition().getRow()).setItemName(e.getNewValue()));

        TableColumn<InventoryItem, String> catCol = new TableColumn<>("Category");
        catCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        catCol.setCellFactory(TextFieldTableCell.forTableColumn());
        catCol.setOnEditCommit(
                e -> e.getTableView().getItems().get(e.getTablePosition().getRow()).setCategory(e.getNewValue()));

        TableColumn<InventoryItem, Integer> qtyCol = new TableColumn<>("Quantity");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        // Quantity is better handled by Restock/Consume buttons, but can be made
        // editable if needed
        // qtyCol.setCellFactory(TextFieldTableCell.forTableColumn(new
        // IntegerStringConverter()));

        TableColumn<InventoryItem, Integer> thresholdCol = new TableColumn<>("Min Threshold");
        thresholdCol.setCellValueFactory(new PropertyValueFactory<>("minimumThreshold"));
        thresholdCol.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        thresholdCol.setOnEditCommit(e -> {
            e.getTableView().getItems().get(e.getTablePosition().getRow()).setMinimumThreshold(e.getNewValue());
            table.refresh(); // Refresh to update color coding
        });

        TableColumn<InventoryItem, String> supplierCol = new TableColumn<>("Supplier");
        supplierCol.setCellValueFactory(new PropertyValueFactory<>("supplier"));
        supplierCol.setCellFactory(TextFieldTableCell.forTableColumn());
        supplierCol.setOnEditCommit(
                e -> e.getTableView().getItems().get(e.getTablePosition().getRow()).setSupplier(e.getNewValue()));

        // Row factory for color coding, respecting selection state
        table.setRowFactory(tv -> {
            TableRow<InventoryItem> row = new TableRow<>();
            row.itemProperty().addListener((obs, oldItem, newItem) -> updateRowStyle(row));
            row.selectedProperty().addListener((obs, wasSelected, isSelected) -> updateRowStyle(row));
            return row;
        });

        table.getColumns().add(idCol);
        table.getColumns().add(nameCol);
        table.getColumns().add(catCol);
        table.getColumns().add(qtyCol);
        table.getColumns().add(thresholdCol);
        table.getColumns().add(supplierCol);
        table.setItems(data);

        // Buttons
        Button restockBtn = new Button("Restock");
        restockBtn.setOnAction(e -> handleRestock());

        Button consumeBtn = new Button("Consume");
        consumeBtn.setOnAction(e -> handleConsume());

        Button addBtn = new Button("Add Item");
        addBtn.setOnAction(e -> handleAddItem());

        Button lowStockBtn = new Button("Show Low Stock");
        lowStockBtn.setOnAction(e -> {
            data.setAll(controller.getLowStockItems());
        });

        Button refreshBtn = new Button("Refresh All");
        refreshBtn.setOnAction(e -> refreshTable());

        // Reporting button
        Button reportBtn = new Button("Reports");
        reportBtn.setOnAction(e -> showReports());

        HBox buttonBar = new HBox(10, addBtn, restockBtn, consumeBtn, lowStockBtn, refreshBtn, reportBtn);
        buttonBar.setPadding(new Insets(10));

        rootPane = new VBox(10, filterBar, table, buttonBar);
        rootPane.setPadding(new Insets(10));
    }

    public VBox getRootPane() {
        return rootPane;
    }

    private void updateRowStyle(TableRow<InventoryItem> row) {
        InventoryItem item = row.getItem();
        if (item == null || row.isEmpty()) {
            row.setStyle("");
        } else {
            if (row.isSelected()) {
                row.setStyle("");
            } else {
                if (item.getQuantity() <= item.getMinimumThreshold()) {
                    row.setStyle("-fx-background-color: #ffcccc; -fx-text-fill: black;");
                } else if (item.getQuantity() <= item.getMinimumThreshold() * 2) {
                    row.setStyle("-fx-background-color: #fff2cc; -fx-text-fill: black;");
                } else {
                    row.setStyle("-fx-background-color: #e6ffcc; -fx-text-fill: black;");
                }
            }
        }
    }

    private void refreshTable() {
        data.setAll(controller.getAllItems());
    }

    private void handleRestock() {
        InventoryItem selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            TextInputDialog dialog = new TextInputDialog("10");
            dialog.setTitle("Restock " + selected.getItemName());
            dialog.setHeaderText("Enter restock amount:");
            dialog.setContentText("Amount:");
            dialog.showAndWait().ifPresent(amountStr -> {
                try {
                    int amount = Integer.parseInt(amountStr);
                    if (amount > 0) {
                        controller.restockItem(selected.getItemId(), amount);
                        table.refresh();
                    }
                } catch (NumberFormatException ignored) {
                }
            });
        }
    }

    private void handleConsume() {
        InventoryItem selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            TextInputDialog dialog = new TextInputDialog("1");
            dialog.setTitle("Consume " + selected.getItemName());
            dialog.setHeaderText("Enter consumption amount:");
            dialog.setContentText("Amount:");
            dialog.showAndWait().ifPresent(amountStr -> {
                try {
                    int amount = Integer.parseInt(amountStr);
                    if (amount > 0) {
                        if (controller.consumeItem(selected.getItemId(), amount)) {
                            table.refresh();
                        } else {
                            Alert alert = new Alert(Alert.AlertType.ERROR, "Not enough stock!");
                            alert.show();
                        }
                    }
                } catch (NumberFormatException ignored) {
                }
            });
        }
    }

    private void handleAddItem() {
        Dialog<InventoryItem> dialog = new Dialog<>();
        dialog.setTitle("Add New Inventory Item");
        dialog.setHeaderText("Enter item details:");

        Label nameLabel = new Label("Name:");
        TextField nameField = new TextField();
        Label catLabel = new Label("Category:");
        ComboBox<String> catBox = new ComboBox<>();
        catBox.getItems().addAll("Housekeeping Supplies", "Guest Amenities", "Food & Beverage Items",
                "Maintenance Tools & Parts", "Office Supplies");
        catBox.getSelectionModel().selectFirst();

        Label qtyLabel = new Label("Quantity:");
        TextField qtyField = new TextField();
        qtyField.setText("50");
        Label minLabel = new Label("Min Level:");
        TextField minField = new TextField();
        minField.setText("10");
        Label supplierLabel = new Label("Supplier:");
        TextField supplierField = new TextField();

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        grid.add(nameLabel, 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(catLabel, 0, 1);
        grid.add(catBox, 1, 1);
        grid.add(qtyLabel, 0, 2);
        grid.add(qtyField, 1, 2);
        grid.add(minLabel, 0, 3);
        grid.add(minField, 1, 3);
        grid.add(supplierLabel, 0, 4);
        grid.add(supplierField, 1, 4);

        dialog.getDialogPane().setContent(grid);
        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                String name = nameField.getText().trim();
                String cat = catBox.getValue();
                String qtyStr = qtyField.getText().trim();
                String minStr = minField.getText().trim();
                String supplier = supplierField.getText().trim();
                if (name.isEmpty()) {
                    showAlert("Item name cannot be empty.");
                    return null;
                }
                int qty, min;
                try {
                    qty = Integer.parseInt(qtyStr);
                    min = Integer.parseInt(minStr);
                } catch (NumberFormatException e) {
                    showAlert("Quantity and Min Level must be numbers.");
                    return null;
                }
                if (qty < 0) {
                    showAlert("Quantity cannot be negative.");
                    return null;
                }
                if (min <= 0) {
                    showAlert("Minimum level must be greater than 0.");
                    return null;
                }
                return new InventoryItem(data.size() + 1, name, cat, qty, min, supplier);
            }
            return null;
        });

        dialog.showAndWait().ifPresent(item -> {
            if (item != null) {
                controller.addItem(item);
                refreshTable();
            }
        });
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.showAndWait();
    }

    // Filtering logic for search and filter controls
    private void applyFilters(TextField searchField, ComboBox<String> categoryFilter, CheckBox lowStockOnly) {
        String search = searchField.getText().toLowerCase();
        String category = categoryFilter.getValue();
        boolean lowStock = lowStockOnly.isSelected();
        data.setAll(controller.getAllItems().stream().filter(item -> {
            boolean matchesName = item.getItemName().toLowerCase().contains(search);
            boolean matchesCategory = category.equals("All") || item.getCategory().equalsIgnoreCase(category);
            boolean matchesLowStock = !lowStock || item.getQuantity() <= item.getMinimumThreshold();
            return matchesName && matchesCategory && matchesLowStock;
        }).toList());
    }

    // --- Reporting logic ---
    @SuppressWarnings("unchecked")
    private void showReports() {
        TabPane tabPane = new TabPane();

        // Daily consumption report
        Tab dailyTab = new Tab("Daily Consumption");
        VBox dailyBox = new VBox(10);
        dailyBox.setPadding(new Insets(10));
        DatePicker datePicker = new DatePicker(java.time.LocalDate.now());
        TableView<InventoryController.ConsumptionEvent> dailyTable = new TableView<>();
        dailyTable.setPlaceholder(new Label("No consumption data for this date"));

        TableColumn<InventoryController.ConsumptionEvent, String> dNameCol = new TableColumn<>("Item Name");
        dNameCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().itemName));
        TableColumn<InventoryController.ConsumptionEvent, Integer> dAmtCol = new TableColumn<>("Amount Used");
        dAmtCol.setCellValueFactory(
                c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().amount).asObject());
        TableColumn<InventoryController.ConsumptionEvent, String> dTimeCol = new TableColumn<>("Time");
        dTimeCol.setCellValueFactory(
                c -> new javafx.beans.property.SimpleStringProperty(c.getValue().timestamp.toLocalTime().toString()));
        dailyTable.getColumns().addAll(dNameCol, dAmtCol, dTimeCol);

        dailyBox.getChildren().addAll(new Label("Select date:"), datePicker, dailyTable);
        dailyTab.setContent(dailyBox);

        datePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateDailyTable(dailyTable, newVal);
        });
        updateDailyTable(dailyTable, datePicker.getValue());

        // Monthly restock report
        Tab monthlyTab = new Tab("Monthly Restock");
        VBox monthlyBox = new VBox(10);
        monthlyBox.setPadding(new Insets(10));
        DatePicker monthPicker = new DatePicker(java.time.LocalDate.now()); // User picks a day, we take the month
        monthPicker.setPromptText("Pick any day in the month");

        TableView<InventoryController.RestockEvent> monthlyTable = new TableView<>();
        monthlyTable.setPlaceholder(new Label("No restock data for this month"));

        TableColumn<InventoryController.RestockEvent, String> mNameCol = new TableColumn<>("Item Name");
        mNameCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().itemName));
        TableColumn<InventoryController.RestockEvent, Integer> mAmtCol = new TableColumn<>("Amount Added");
        mAmtCol.setCellValueFactory(
                c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().amount).asObject());
        TableColumn<InventoryController.RestockEvent, String> mDateCol = new TableColumn<>("Date");
        mDateCol.setCellValueFactory(
                c -> new javafx.beans.property.SimpleStringProperty(c.getValue().timestamp.toLocalDate().toString()));
        monthlyTable.getColumns().addAll(mNameCol, mAmtCol, mDateCol);

        monthlyBox.getChildren().addAll(new Label("Select month (pick any day):"), monthPicker, monthlyTable);
        monthlyTab.setContent(monthlyBox);

        monthPicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                monthlyTable.getItems().setAll(controller.getRestockEventsForMonth(java.time.YearMonth.from(newVal)));
            }
        });
        // Initial load
        if (monthPicker.getValue() != null) {
            monthlyTable.getItems()
                    .setAll(controller.getRestockEventsForMonth(java.time.YearMonth.from(monthPicker.getValue())));
        }

        // Low-stock history
        Tab lowStockTab = new Tab("Low-Stock History");
        VBox lowStockBox = new VBox(10);
        lowStockBox.setPadding(new Insets(10));

        TableView<InventoryController.LowStockEvent> lowStockTable = new TableView<>();
        lowStockTable.setPlaceholder(new Label("No low stock events recorded history"));

        TableColumn<InventoryController.LowStockEvent, String> lNameCol = new TableColumn<>("Item Name");
        lNameCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().itemName));
        TableColumn<InventoryController.LowStockEvent, Integer> lQtyCol = new TableColumn<>("Qty at Alert");
        lQtyCol.setCellValueFactory(
                c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().currentQty).asObject());
        TableColumn<InventoryController.LowStockEvent, Integer> lThreshCol = new TableColumn<>("Threshold");
        lThreshCol.setCellValueFactory(
                c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().threshold).asObject());
        TableColumn<InventoryController.LowStockEvent, String> lTimeCol = new TableColumn<>("Time");
        lTimeCol.setCellValueFactory(
                c -> new javafx.beans.property.SimpleStringProperty(c.getValue().timestamp.toString()));
        lowStockTable.getColumns().addAll(lNameCol, lQtyCol, lThreshCol, lTimeCol);

        lowStockTable.getItems().setAll(controller.getLowStockHistory());
        lowStockBox.getChildren().add(lowStockTable);
        lowStockTab.setContent(lowStockBox);

        // Most-used items
        Tab mostUsedTab = new Tab("Most Used Items");
        VBox mostUsedBox = new VBox(10);
        mostUsedBox.setPadding(new Insets(10));

        TableView<InventoryController.ItemUsage> mostUsedTable = new TableView<>();
        mostUsedTable.setPlaceholder(new Label("No usage data available"));

        TableColumn<InventoryController.ItemUsage, String> muNameCol = new TableColumn<>("Item Name");
        muNameCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().itemName));
        TableColumn<InventoryController.ItemUsage, Integer> muTotalCol = new TableColumn<>("Total Consumed");
        muTotalCol.setCellValueFactory(
                c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().totalConsumed).asObject());
        mostUsedTable.getColumns().addAll(muNameCol, muTotalCol);

        mostUsedTable.getItems().setAll(controller.getMostUsedItems());
        mostUsedBox.getChildren().add(mostUsedTable);
        mostUsedTab.setContent(mostUsedBox);

        tabPane.getTabs().addAll(dailyTab, monthlyTab, lowStockTab, mostUsedTab);

        Alert dialog = new Alert(Alert.AlertType.INFORMATION);
        dialog.setTitle("Inventory Reports");
        dialog.setHeaderText(null);
        dialog.getDialogPane().setContent(tabPane);
        dialog.setResizable(true);
        dialog.showAndWait();
    }

    private void updateDailyTable(TableView<InventoryController.ConsumptionEvent> table, java.time.LocalDate date) {
        if (date != null) {
            table.getItems().setAll(controller.getConsumptionEventsForDay(date));
        }
    }
}
