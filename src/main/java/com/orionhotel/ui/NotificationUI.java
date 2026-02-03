package com.orionhotel.ui;

import com.orionhotel.controller.NotificationController;
import com.orionhotel.model.Notification;
import com.orionhotel.model.Role;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class NotificationUI {
    private NotificationController controller;
    private Role userRole;

    public NotificationUI(NotificationController controller, Role userRole) {
        this.controller = controller;
        this.userRole = userRole;
    }

    public void show() {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Notifications");

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));

        ListView<Notification> list = new ListView<>();
        list.setCellFactory(param -> new ListCell<Notification>() {
            @Override
            protected void updateItem(Notification item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.getMessage() + "\n" + item.getTimestamp());
                    if (!item.isRead()) {
                        setStyle("-fx-font-weight: bold; -fx-background-color: #e6f7ff;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        refreshList(list);

        Button markReadBtn = new Button("Mark as Read");
        markReadBtn.setOnAction(e -> {
            Notification n = list.getSelectionModel().getSelectedItem();
            if (n != null) {
                controller.markAsRead(n.getId());
                refreshList(list);
            }
        });

        Button markAllBtn = new Button("Mark All Read");
        markAllBtn.setOnAction(e -> {
            controller.markAllAsRead(userRole);
            refreshList(list);
        });

        Button clearBtn = new Button("Clear All");
        clearBtn.setOnAction(e -> {
            controller.clearAll(userRole);
            refreshList(list);
        });

        layout.getChildren().addAll(new Label("Your Notifications:"), list, markReadBtn, markAllBtn, clearBtn);
        Scene scene = new Scene(layout, 400, 500);
        stage.setScene(scene);
        stage.show();
    }

    private void refreshList(ListView<Notification> list) {
        list.getItems().setAll(controller.getNotifications(userRole));
    }
}
