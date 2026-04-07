package com.tradematrix;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;

public class OnboardingController {

    @FXML private TextField tickerField;
    @FXML private ComboBox<String> typeComboBox;
    @FXML private TextField quantityField;
    @FXML private TextField priceField;
    @FXML private DatePicker datePicker;
    @FXML private Label statusLabel;

    private javafx.scene.control.ContextMenu tickerSuggestions;

    @FXML
    public void initialize() {
        typeComboBox.setItems(FXCollections.observableArrayList("Buy", "Sell"));
        typeComboBox.getSelectionModel().selectFirst();
        datePicker.setValue(LocalDate.now());

        tickerSuggestions = new javafx.scene.control.ContextMenu();
        
        tickerField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.length() < 2) {
                tickerSuggestions.hide();
                return;
            }
            
            new Thread(() -> {
                try {
                    ProcessBuilder pb = new ProcessBuilder(".\\.venv\\Scripts\\python.exe", "scripts/python/fetch_suggestions.py", newValue);
                    Process p = pb.start();
                    java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(p.getInputStream()));
                    String line = reader.readLine();
                    if (line != null && line.startsWith("[")) {
                        String[] arr = line.substring(1, line.length()-1).replace("\"", "").split(",");
                        javafx.application.Platform.runLater(() -> {
                            tickerSuggestions.getItems().clear();
                            for (String rawT : arr) {
                                String t = rawT.trim();
                                if (!t.isEmpty()) {
                                    javafx.scene.control.MenuItem item = new javafx.scene.control.MenuItem(t);
                                    item.setOnAction(e -> {
                                        tickerField.setText(t);
                                        tickerSuggestions.hide();
                                    });
                                    tickerSuggestions.getItems().add(item);
                                }
                            }
                            if (!tickerSuggestions.getItems().isEmpty()) {
                                tickerSuggestions.show(tickerField, javafx.geometry.Side.BOTTOM, 0, 0);
                            } else {
                                tickerSuggestions.hide();
                            }
                        });
                    }
                } catch (Exception e) {}
            }).start();
        });
    }
    
    @FXML
    private void handleAddTransaction() {
        String ticker = tickerField.getText().trim().toUpperCase();
        String type = typeComboBox.getValue();
        String qtyStr = quantityField.getText().trim();
        String priceStr = priceField.getText().trim();
        LocalDate date = datePicker.getValue();
        
        if (ticker.isEmpty() || qtyStr.isEmpty() || priceStr.isEmpty() || date == null) {
            statusLabel.setText("Please fill all fields.");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }
        
        try {
            double qty = Double.parseDouble(qtyStr);
            double price = Double.parseDouble(priceStr);
            
            try (Connection conn = DatabaseManager.getConnection()) {
                String sql = "INSERT INTO transactions (user_id, ticker, transaction_type, quantity, price_per_share, transaction_date) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, UserSession.getInstance().getUserId());
                pstmt.setString(2, ticker);
                pstmt.setString(3, type);
                pstmt.setDouble(4, qty);
                pstmt.setDouble(5, price);
                pstmt.setString(6, date.toString());
                pstmt.executeUpdate();
                
                statusLabel.setText("Added " + type + " " + qty + " of " + ticker);
                statusLabel.setStyle("-fx-text-fill: green;");
                
                tickerField.clear();
                quantityField.clear();
                priceField.clear();
            }
        } catch (NumberFormatException e) {
            statusLabel.setText("Quantity and Price must be valid numbers.");
            statusLabel.setStyle("-fx-text-fill: red;");
        } catch (Exception e) {
            statusLabel.setText("Database DB Error: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }
    
    @FXML private void handleGoToDashboard() { navigate("/fxml/Dashboard.fxml"); }
    @FXML private void handleGoToPortfolio() { navigate("/fxml/Onboarding.fxml"); }
    @FXML private void handleGoToHistory() { navigate("/fxml/History.fxml"); }
    @FXML private void handleGoToSettings() { navigate("/fxml/Settings.fxml"); }

    private void navigate(String path) {
        try {
            javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource(path));
            tickerField.getScene().setRoot(root);
        } catch (Exception e) { e.printStackTrace(); }
    }
}
