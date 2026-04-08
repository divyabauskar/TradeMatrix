package com.tradematrix;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {

    @FXML private TextField fullNameField;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private TextField mobileField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button signupButton;
    @FXML private Label statusLabel;

    @FXML
    protected void handleSignup() {
        String fullName = fullNameField.getText().trim();
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String mobile = mobileField.getText().trim();
        // Normalize accidental leading/trailing whitespace (common with copy/paste).
        String password = passwordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();

        if (fullName.isEmpty() || username.isEmpty() || email.isEmpty() || 
            mobile.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            statusLabel.setText("Please fill all fields.");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        if (!email.contains("@")) {
            statusLabel.setText("Please enter a valid email address.");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        if (password.length() < 6) {
            statusLabel.setText("Password must be at least 6 characters.");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        if (!password.equals(confirmPassword)) {
            statusLabel.setText("Passwords do not match.");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        try (Connection conn = DatabaseManager.getConnection()) {
            PreparedStatement checkStmt = conn.prepareStatement(
                "SELECT id FROM user_profile WHERE username = ? OR email = ?"
            );
            checkStmt.setString(1, username);
            checkStmt.setString(2, email);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                statusLabel.setText("Username or Email already exists.");
                statusLabel.setStyle("-fx-text-fill: red;");
                return;
            }

            PreparedStatement insertStmt = conn.prepareStatement(
                "INSERT INTO user_profile (username, email, full_name, password, mobile_number, base_currency) VALUES (?, ?, ?, ?, ?, 'INR')"
            );
            insertStmt.setString(1, username);
            insertStmt.setString(2, email);
            insertStmt.setString(3, fullName);
            insertStmt.setString(4, password);
            insertStmt.setString(5, mobile);
            insertStmt.executeUpdate();

            statusLabel.setText("Account created successfully! Redirecting to login...");
            statusLabel.setStyle("-fx-text-fill: green;");

            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    javafx.application.Platform.runLater(this::handleGoToLogin);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (Exception e) {
            statusLabel.setText("Database error: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: red;");
            e.printStackTrace();
        }
    }

    @FXML
    protected void handleGoToLogin() {
        try {
            javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource("/fxml/LoginView.fxml"));
            fullNameField.getScene().setRoot(root);
        } catch (Exception e) {
            statusLabel.setText("Navigation Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
