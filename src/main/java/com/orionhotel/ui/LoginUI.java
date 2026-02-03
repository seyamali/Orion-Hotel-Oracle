package com.orionhotel.ui;

import com.orionhotel.controller.StaffController;
import com.orionhotel.model.Staff;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class LoginUI extends Application {

    private StaffController staffController = new StaffController();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Login - Orion Hotel Oracle");

        // Main layout centered
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: #f4f4f4;");

        // Logo/Title Area
        VBox header = new VBox(10);
        header.setAlignment(Pos.CENTER);

        // Placeholder for Logo (Circle or ImageView)
        Label logoPlaceholder = new Label("🏨");
        logoPlaceholder.setStyle("-fx-font-size: 48px;");

        Text title = new Text("Orion Hotel");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        title.setFill(javafx.scene.paint.Color.web("#2c3e50"));

        header.getChildren().addAll(logoPlaceholder, title);

        // Login Form
        VBox form = new VBox(15);
        form.setMaxWidth(300);
        form.setAlignment(Pos.CENTER);
        form.setStyle(
                "-fx-background-color: white; -fx-padding: 30; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");

        TextField userTextField = new TextField();
        userTextField.setPromptText("Username");
        userTextField
                .setStyle("-fx-padding: 10; -fx-background-radius: 5; -fx-border-color: #ddd; -fx-border-radius: 5;");

        PasswordField pwBox = new PasswordField();
        pwBox.setPromptText("Password");
        pwBox.setStyle("-fx-padding: 10; -fx-background-radius: 5; -fx-border-color: #ddd; -fx-border-radius: 5;");

        Button btn = new Button("Login");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle(
                "-fx-background-color: #2c3e50; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10; -fx-background-radius: 5; -fx-cursor: hand;");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");

        Hyperlink forgotPw = new Hyperlink("Forgot Password?");
        forgotPw.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d;");

        form.getChildren().addAll(userTextField, pwBox, btn, errorLabel, forgotPw);

        // Action Logic
        btn.setOnAction(e -> {
            String user = userTextField.getText();
            String password = pwBox.getText();
            Staff staff = staffController.authenticate(user, password);

            if (staff != null) {
                try {
                    new MainMenu(staff).start(primaryStage); // Launch main menu
                } catch (Exception ex) {
                    ex.printStackTrace();
                    errorLabel.setText("System Error: Could not load menu.");
                }
            } else {
                errorLabel.setText("Invalid username or password.");
                // Shake effect logic could go here
            }
        });

        // Hint for dev
        Label hint = new Label("Dev Hint: admin / admin123");
        hint.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 10px;");

        root.getChildren().addAll(header, form, hint);

        Scene scene = new Scene(root, 500, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
