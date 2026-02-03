package com.orionhotel.ui;

import com.orionhotel.controller.HousekeepingController;
import com.orionhotel.controller.RoomController;
import com.orionhotel.controller.StaffController;
import com.orionhotel.model.HousekeepingTask;
import com.orionhotel.model.MaintenanceRequest;
import com.orionhotel.model.Role;
import com.orionhotel.model.Staff;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.stream.Collectors;

public class HousekeepingUI {

    private HousekeepingController controller;
    private RoomController roomController;
    private StaffController staffController; // Needed to pick staff for assignment

    private VBox rootPane;
    private TableView<HousekeepingTask> taskTable;
    private TableView<MaintenanceRequest> maintTable;

    public HousekeepingUI() {
        // Initialize controllers - in a real DI world these would be injected
        roomController = new RoomController();
        staffController = new StaffController();
        controller = new HousekeepingController(roomController, staffController);

        initializeUI();
    }

    public void setNotificationController(com.orionhotel.controller.NotificationController nc) {
        controller.setNotificationController(nc);
    }

    public VBox getRootPane() {
        return rootPane;
    }

    private void initializeUI() {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // --- Tab 1: Housekeeping ---
        Tab cleanTab = new Tab("Cleaning Tasks");
        VBox cleanBox = new VBox(10);
        cleanBox.setPadding(new Insets(10));

        taskTable = new TableView<>();

        TableColumn<HousekeepingTask, String> tId = new TableColumn<>("ID");
        tId.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getTaskId())));

        TableColumn<HousekeepingTask, String> tRoom = new TableColumn<>("Room");
        tRoom.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getRoomNumber())));

        TableColumn<HousekeepingTask, String> tType = new TableColumn<>("Type");
        tType.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getType().toString()));

        TableColumn<HousekeepingTask, String> tStaff = new TableColumn<>("Assigned To");
        tStaff.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAssignedStaffName()));

        TableColumn<HousekeepingTask, String> tStatus = new TableColumn<>("Status");
        tStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus().toString()));

        taskTable.getColumns().add(tId);
        taskTable.getColumns().add(tRoom);
        taskTable.getColumns().add(tType);
        taskTable.getColumns().add(tStaff);
        taskTable.getColumns().add(tStatus);

        Button createTaskBtn = new Button("New Task");
        createTaskBtn.setOnAction(e -> showCreateTaskDialog());

        Button statusBtn = new Button("Update Status");
        statusBtn.setOnAction(e -> showUpdateTaskStatusDialog());

        Button refreshT = new Button("Refresh");
        refreshT.setOnAction(e -> refreshTables());

        cleanBox.getChildren().addAll(new HBox(10, createTaskBtn, statusBtn, refreshT), taskTable);
        cleanTab.setContent(cleanBox);

        // --- Tab 2: Maintenance ---
        Tab maintTab = new Tab("Maintenance Requests");
        VBox maintBox = new VBox(10);
        maintBox.setPadding(new Insets(10));

        maintTable = new TableView<>();

        TableColumn<MaintenanceRequest, String> mId = new TableColumn<>("ID");
        mId.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getRequestId())));

        TableColumn<MaintenanceRequest, String> mRoom = new TableColumn<>("Room");
        mRoom.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getRoomNumber())));

        TableColumn<MaintenanceRequest, String> mIssue = new TableColumn<>("Issue");
        mIssue.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getIssueType().toString()));

        TableColumn<MaintenanceRequest, String> mDesc = new TableColumn<>("Description");
        mDesc.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDescription()));

        TableColumn<MaintenanceRequest, String> mPrio = new TableColumn<>("Priority");
        mPrio.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPriority().toString()));

        TableColumn<MaintenanceRequest, String> mTech = new TableColumn<>("Technician");
        mTech.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAssignedTechnicianName()));

        TableColumn<MaintenanceRequest, String> mStat = new TableColumn<>("Status");
        mStat.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus().toString()));

        maintTable.getColumns().add(mId);
        maintTable.getColumns().add(mRoom);
        maintTable.getColumns().add(mIssue);
        maintTable.getColumns().add(mDesc);
        maintTable.getColumns().add(mPrio);
        maintTable.getColumns().add(mTech);
        maintTable.getColumns().add(mStat);

        Button createReqBtn = new Button("New Request");
        createReqBtn.setOnAction(e -> showCreateReqDialog());

        Button assignBtn = new Button("Assign Tech");
        assignBtn.setOnAction(e -> showAssignTechDialog());

        Button completeBtn = new Button("Mark Fixed");
        completeBtn.setOnAction(e -> markReqFixed());

        Button refreshM = new Button("Refresh");
        refreshM.setOnAction(e -> refreshTables());

        maintBox.getChildren().addAll(new HBox(10, createReqBtn, assignBtn, completeBtn, refreshM), maintTable);
        maintTab.setContent(maintBox);

        tabPane.getTabs().addAll(cleanTab, maintTab);

        rootPane = new VBox(tabPane);
        refreshTables();
    }

    private void refreshTables() {
        taskTable.setItems(FXCollections.observableArrayList(controller.getAllTasks()));
        maintTable.setItems(FXCollections.observableArrayList(controller.getAllMaintenance()));
    }

    // --- Dialogs ---

    private void showCreateTaskDialog() {
        Dialog<HousekeepingTask> dialog = new Dialog<>();
        dialog.setTitle("New Cleaning Task");
        dialog.setHeaderText("Create new housekeeping task");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        ComboBox<Integer> roomBox = new ComboBox<>();
        // Populate room numbers
        roomBox.setItems(FXCollections.observableArrayList(
                roomController.getAllRooms().stream().map(r -> r.getRoomNumber()).collect(Collectors.toList())));

        ComboBox<HousekeepingTask.TaskType> typeBox = new ComboBox<>(
                FXCollections.observableArrayList(HousekeepingTask.TaskType.values()));
        typeBox.setValue(HousekeepingTask.TaskType.CLEANING);

        ComboBox<Staff> staffBox = new ComboBox<>();
        staffBox.setItems(FXCollections.observableArrayList(
                staffController.getActiveStaff().stream()
                        .filter(s -> s.getRole() == Role.HOUSEKEEPING || s.getRole() == Role.ADMIN) // Assuming
                                                                                                    // housekeeping
                                                                                                    // staff
                        .collect(Collectors.toList())));
        // Custom cell factory to show names
        staffBox.setCellFactory(lv -> new ListCell<Staff>() {
            protected void updateItem(Staff item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item.getFullName());
            }
        });
        staffBox.setButtonCell(new ListCell<Staff>() {
            protected void updateItem(Staff item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item.getFullName());
            }
        });

        grid.add(new Label("Room:"), 0, 0);
        grid.add(roomBox, 1, 0);
        grid.add(new Label("Type:"), 0, 1);
        grid.add(typeBox, 1, 1);
        grid.add(new Label("Assign To:"), 0, 2);
        grid.add(staffBox, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK && roomBox.getValue() != null) {
                Integer sId = (staffBox.getValue() != null) ? staffBox.getValue().getStaffId() : null;
                controller.createCleaningTask(roomBox.getValue(), typeBox.getValue(), sId);
            }
            return null;
        });

        dialog.showAndWait();
        refreshTables();
    }

    private void showUpdateTaskStatusDialog() {
        HousekeepingTask selected = taskTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Select a task.");
            return;
        }

        ChoiceDialog<HousekeepingTask.Startus> dialog = new ChoiceDialog<>(selected.getStatus(),
                HousekeepingTask.Startus.values());
        dialog.setTitle("Update Status");
        dialog.setHeaderText("Set status for Task ID " + selected.getTaskId());
        dialog.showAndWait().ifPresent(s -> {
            controller.updateTaskStatus(selected.getTaskId(), s);
            refreshTables();
        });
    }

    private void showCreateReqDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("New Maintenance Request");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        ComboBox<Integer> roomBox = new ComboBox<>();
        roomBox.setItems(FXCollections.observableArrayList(
                roomController.getAllRooms().stream().map(r -> r.getRoomNumber()).collect(Collectors.toList())));

        ComboBox<MaintenanceRequest.IssueType> typeBox = new ComboBox<>(
                FXCollections.observableArrayList(MaintenanceRequest.IssueType.values()));

        ComboBox<MaintenanceRequest.Priority> priorityBox = new ComboBox<>(
                FXCollections.observableArrayList(MaintenanceRequest.Priority.values()));

        TextField descField = new TextField();

        grid.add(new Label("Room:"), 0, 0);
        grid.add(roomBox, 1, 0);
        grid.add(new Label("Issue:"), 0, 1);
        grid.add(typeBox, 1, 1);
        grid.add(new Label("Priority:"), 0, 2);
        grid.add(priorityBox, 1, 2);
        grid.add(new Label("Description:"), 0, 3);
        grid.add(descField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK && roomBox.getValue() != null) {
                controller.createMaintenanceRequest(roomBox.getValue(), typeBox.getValue(), descField.getText(),
                        priorityBox.getValue());
            }
            return null;
        });

        dialog.showAndWait();
        refreshTables();
    }

    private void showAssignTechDialog() {
        MaintenanceRequest selected = maintTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Select a request.");
            return;
        }

        // Pick technician (anyone not housekeeping or receptionist essentially, but for
        // simplicity showing all or maintenance role if existed)
        // We will show all staff for now as we don't have a Technician role explicitly
        // defined in enum earlier,
        // let's assume Housekeeping or maybe Manager does it?
        // Or we can just show all active staff.

        Dialog<Staff> dialog = new Dialog<>();
        dialog.setTitle("Assign Technician");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        ComboBox<Staff> staffBox = new ComboBox<>();
        staffBox.setItems(FXCollections.observableArrayList(staffController.getActiveStaff()));
        // format cells
        staffBox.setCellFactory(lv -> new ListCell<Staff>() {
            protected void updateItem(Staff item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item.getFullName() + " (" + item.getRole() + ")");
            }
        });
        staffBox.setButtonCell(new ListCell<Staff>() {
            protected void updateItem(Staff item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item.getFullName());
            }
        });

        VBox vbox = new VBox(10, new Label("Assign request " + selected.getRequestId() + " to:"), staffBox);
        vbox.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(vbox);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK)
                return staffBox.getValue();
            return null;
        });

        dialog.showAndWait().ifPresent(staff -> {
            controller.assignTechnician(selected.getRequestId(), staff.getStaffId());
            refreshTables();
        });
    }

    private void markReqFixed() {
        MaintenanceRequest selected = maintTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            controller.completeMaintenance(selected.getRequestId());
            refreshTables();
        } else {
            showAlert("Select a request.");
        }
    }

    private void showAlert(String msg) {
        new Alert(Alert.AlertType.ERROR, msg).show();
    }

}
