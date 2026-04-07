package com.tradematrix;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SettingsController {

    @FXML private ComboBox<String> themeComboBox;

    @FXML private Label statusLabel;

    @FXML
    public void initialize() {
        themeComboBox.setItems(FXCollections.observableArrayList("Light Mode", "Dark Mode"));
        
        if (UserSession.getInstance().isLightMode()) {
            themeComboBox.getSelectionModel().select("Light Mode");
        } else {
            themeComboBox.getSelectionModel().select("Dark Mode");
        }
    }
        
    @FXML
    private void handleSaveChanges() {
        String selected = themeComboBox.getValue();
        if (selected == null) {
            statusLabel.setText("Please select a theme before saving.");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        boolean isLight = selected.equals("Light Mode");
        UserSession.getInstance().setLightMode(isLight);
        
        java.net.URL resource = getClass().getResource("/css/light-theme.css");
        if (resource == null) {
            statusLabel.setText("Light theme CSS not found by IDE/ClassLoader. Please restart/recompile.");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }
        String lightCss = resource.toExternalForm();
        
        if (statusLabel.getScene() != null) {
            if (isLight) {
                if (!statusLabel.getScene().getStylesheets().contains(lightCss)) {
                    statusLabel.getScene().getStylesheets().add(lightCss);
                }
            } else {
                statusLabel.getScene().getStylesheets().remove(lightCss);
            }
        }
        
        statusLabel.setText("Changes saved successfully");
        statusLabel.setStyle("-fx-text-fill: #22c55e;");
    }

    @FXML
    private void handleBackup() {
        try {
            File dbFile = new File("database/tradematrix.db");
            if (dbFile.exists()) {
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                File backupFile = new File("database/tradematrix_backup_" + timestamp + ".db");
                Files.copy(dbFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                statusLabel.setText("Backup successful: " + backupFile.getName());
                statusLabel.setStyle("-fx-text-fill: green;");
            } else {
                statusLabel.setText("Database file not found!");
                statusLabel.setStyle("-fx-text-fill: red;");
            }
        } catch (Exception e) {
            statusLabel.setText("Backup failed: " + e.getMessage());
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
            statusLabel.getScene().setRoot(root);
        } catch (Exception e) { e.printStackTrace(); }
    }
}
