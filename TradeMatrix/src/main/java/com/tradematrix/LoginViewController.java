package com.tradematrix;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginViewController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label statusLabel;

    @FXML
    protected void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please enter username and password.");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        try (Connection conn = DatabaseManager.getConnection()) {
            PreparedStatement checkStmt = conn.prepareStatement(
                "SELECT id, full_name, email, base_currency FROM user_profile WHERE username = ? AND password = ?"
            );
            checkStmt.setString(1, username);
            checkStmt.setString(2, password);

            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                UserSession session = UserSession.getInstance();
                session.setUserId(rs.getInt("id"));
                session.setUsername(username);
                session.setFullName(rs.getString("full_name"));
                session.setEmail(rs.getString("email"));
                session.setBaseCurrency(rs.getString("base_currency"));

                statusLabel.setText("Login successful! Redirecting...");
                statusLabel.setStyle("-fx-text-fill: green;");
                loadDashboard();
            } else {
                statusLabel.setText("Invalid username or password.");
                statusLabel.setStyle("-fx-text-fill: red;");
            }
        } catch (Exception e) {
            statusLabel.setText("Database error: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: red;");
            e.printStackTrace();
        }
    }

    private void loadDashboard() {
        try {
            javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource("/fxml/Dashboard.fxml"));
            usernameField.getScene().setRoot(root);
        } catch (Exception e) {
            statusLabel.setText("Navigation Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    protected void handleGoToSignup() {
        try {
            javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource("/fxml/Login.fxml"));
            usernameField.getScene().setRoot(root);
        } catch (Exception e) {
            statusLabel.setText("Navigation Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
