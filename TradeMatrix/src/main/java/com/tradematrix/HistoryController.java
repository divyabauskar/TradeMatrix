package com.tradematrix;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class HistoryController {
    
    @FXML private TableView<TransactionRecord> historyTable;
    @FXML private TableColumn<TransactionRecord, String> dateCol;
    @FXML private TableColumn<TransactionRecord, String> tickerCol;
    @FXML private TableColumn<TransactionRecord, String> typeCol;
    @FXML private TableColumn<TransactionRecord, Double> qtyCol;
    @FXML private TableColumn<TransactionRecord, Double> priceCol;
    @FXML private TableColumn<TransactionRecord, Double> totalCol;
    
    private ObservableList<TransactionRecord> recordList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        tickerCol.setCellValueFactory(new PropertyValueFactory<>("ticker"));
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        totalCol.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        
        priceCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) setText(null);
                else setText(UserSession.getInstance().formatCurrency(price));
            }
        });
        
        totalCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double total, boolean empty) {
                super.updateItem(total, empty);
                if (empty || total == null) {
                    setText(null); setStyle("");
                } else {
                    setText(UserSession.getInstance().formatCurrency(total));
                    TransactionRecord record = getTableView().getItems().get(getIndex());
                    if ("Sell".equalsIgnoreCase(record.getType())) {
                        setStyle("-fx-text-fill: #22c55e; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                    }
                }
            }
        });

        loadHistory();
        historyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        historyTable.setItems(recordList);
    }
    
    private void loadHistory() {
        recordList.clear();
        try (Connection conn = DatabaseManager.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM transactions WHERE user_id = ? ORDER BY transaction_date DESC, id DESC"
            );
            stmt.setInt(1, UserSession.getInstance().getUserId());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String date = rs.getString("transaction_date");
                String ticker = rs.getString("ticker");
                String type = rs.getString("transaction_type");
                double qty = rs.getDouble("quantity");
                double price = rs.getDouble("price_per_share");
                
                recordList.add(new TransactionRecord(date, ticker, type, qty, price));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML private void handleGoToDashboard() { navigate("/fxml/Dashboard.fxml"); }
    @FXML private void handleGoToPortfolio() { navigate("/fxml/Onboarding.fxml"); }
    @FXML private void handleGoToHistory() { navigate("/fxml/History.fxml"); }
    @FXML private void handleGoToSettings() { navigate("/fxml/Settings.fxml"); }

    private void navigate(String path) {
        try {
            javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource(path));
            historyTable.getScene().setRoot(root);
        } catch (Exception e) { e.printStackTrace(); }
    }
}
