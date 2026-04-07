package com.tradematrix;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class DashboardController {

    @FXML private Label totalInvestedLabel;
    @FXML private Label currentValueLabel;
    @FXML private Label pnlLabel;
    @FXML private PieChart sectorPieChart;
    @FXML private javafx.scene.chart.LineChart<String, Number> benchmarkChart;
    
    @FXML private VBox profileDetailsCard;
    @FXML private VBox dashboardContent;
    @FXML private Label profileUsernameLabel;
    @FXML private Label profileEmailLabel;
    @FXML private Label profileNameLabel;
    @FXML private Label profileMobileLabel;
    @FXML private Label profileCurrencyLabel;
    
    @FXML private TableView<Holding> holdingsTable;
    @FXML private TableColumn<Holding, String> tickerCol;
    @FXML private TableColumn<Holding, Double> qtyCol;
    @FXML private TableColumn<Holding, Double> avgCostCol;
    @FXML private TableColumn<Holding, Double> ltpCol;
    @FXML private TableColumn<Holding, Double> investedCol;
    @FXML private TableColumn<Holding, Double> currentValCol;
    @FXML private TableColumn<Holding, Double> pnlCol;

    private ObservableList<Holding> holdingsList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Initialize profile and dashboard visibility
        profileDetailsCard.setVisible(false);
        profileDetailsCard.setManaged(false);
        dashboardContent.setVisible(true);
        dashboardContent.setManaged(true);

        UserSession session = UserSession.getInstance();
        profileUsernameLabel.setText("Username: " + (session.getUsername() != null ? session.getUsername() : "N/A"));
        profileEmailLabel.setText("Email: " + (session.getEmail() != null ? session.getEmail() : "N/A"));
        profileNameLabel.setText("Name: " + (session.getFullName() != null ? session.getFullName() : "N/A"));
        profileMobileLabel.setText("Mobile: " + (session.getMobileNumber() != null ? session.getMobileNumber() : "N/A"));
        profileCurrencyLabel.setText("Base Currency: " + (session.getBaseCurrency() != null ? session.getBaseCurrency() : "INR"));
        
        tickerCol.setCellValueFactory(new PropertyValueFactory<>("ticker"));
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        avgCostCol.setCellValueFactory(new PropertyValueFactory<>("avgCost"));
        ltpCol.setCellValueFactory(new PropertyValueFactory<>("ltp"));
        investedCol.setCellValueFactory(new PropertyValueFactory<>("invested"));
        currentValCol.setCellValueFactory(new PropertyValueFactory<>("currentValue"));
        pnlCol.setCellValueFactory(new PropertyValueFactory<>("pnl"));
        
        pnlCol.setCellFactory(col -> new javafx.scene.control.TableCell<>() {
            @Override protected void updateItem(Double pnl, boolean empty) {
                super.updateItem(pnl, empty);
                if (empty || pnl == null) { setText(null); setStyle(""); }
                else {
                    setText(UserSession.getInstance().formatCurrency(pnl));
                    setStyle(pnl >= 0 ? "-fx-text-fill: #22c55e; -fx-font-weight: bold;" : "-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                }
            }
        });
        holdingsTable.setItems(holdingsList);
        holdingsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        totalInvestedLabel.setText(UserSession.getInstance().formatCurrency(0));
        currentValueLabel.setText(UserSession.getInstance().formatCurrency(0));
        pnlLabel.setText("Loading...");
        pnlLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-weight: bold; -fx-font-size: 14px;");

        loadHoldingsAsync();
    }
    
    @FXML
    private void handleRefresh() {
        updateLivePrices();
    }
    
    private void loadHoldingsAsync() {
        new Thread(() -> {
            Map<String, Holding> map = new HashMap<>();
            try (Connection conn = DatabaseManager.getConnection()) {
                PreparedStatement stmt = conn.prepareStatement("SELECT ticker, transaction_type, quantity, price_per_share FROM transactions WHERE user_id = ?");
                stmt.setInt(1, UserSession.getInstance().getUserId());
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    String ticker = rs.getString("ticker");
                    String type = rs.getString("transaction_type");
                    double qty = rs.getDouble("quantity");
                    double price = rs.getDouble("price_per_share");
                    
                    map.putIfAbsent(ticker, new Holding(ticker));
                    Holding h = map.get(ticker);
                    
                    if (type.equals("Buy")) {
                        double totalCost = (h.getQuantity() * h.getAvgCost()) + (qty * price);
                        h.setQuantity(h.getQuantity() + qty);
                        h.setAvgCost(totalCost / h.getQuantity());
                    } else if (type.equals("Sell")) {
                        h.setQuantity(h.getQuantity() - qty);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            double initialInvested = 0;
            ObservableList<PieChart.Data> initialPieData = FXCollections.observableArrayList();
            ObservableList<Holding> loadedHoldings = FXCollections.observableArrayList();
            for (Holding h : map.values()) {
                if (h.getQuantity() > 0) {
                    h.setCurrentValue(h.getInvested()); // Placeholder value until live price arrives
                    h.setLtp(h.getAvgCost());           // Placeholder current price
                    h.setPnl(0);
                    initialInvested += h.getInvested();
                    initialPieData.add(new PieChart.Data(h.getTicker(), h.getInvested()));
                    loadedHoldings.add(h);
                }
            }

            final double investedValue = initialInvested;
            javafx.application.Platform.runLater(() -> {
                holdingsList.setAll(loadedHoldings);
                sectorPieChart.setData(initialPieData);
                totalInvestedLabel.setText(UserSession.getInstance().formatCurrency(investedValue));
                currentValueLabel.setText(UserSession.getInstance().formatCurrency(investedValue));
                pnlLabel.setText(UserSession.getInstance().formatCurrency(0));
                pnlLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-weight: bold; -fx-font-size: 14px;");
                holdingsTable.refresh();
                updateLivePrices();
            });
        }).start();
    }
    
    private void updateLivePrices() {
        if (holdingsList.isEmpty()) return;
        
        StringBuilder tickers = new StringBuilder();
        for (Holding h : holdingsList) {
            tickers.append(h.getTicker()).append(",");
        }
        
        String pythonExe = Paths.get(System.getProperty("user.dir"), ".venv", "Scripts", "python.exe").toString();
        String scriptPath = Paths.get(System.getProperty("user.dir"), "scripts", "python", "fetch_price.py").toString();

        new Thread(() -> {
            try {
                ProcessBuilder pb = new ProcessBuilder(pythonExe, scriptPath, tickers.toString());
                pb.redirectErrorStream(true);
                Process p = pb.start();
                
                BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String jsonOutputStr = "";
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().startsWith("{")) jsonOutputStr = line.trim();
                }
                boolean finished = p.waitFor(8, java.util.concurrent.TimeUnit.SECONDS);
                if (!finished) {
                    p.destroyForcibly();
                    System.err.println("Live price script timed out. Falling back to default data.");
                }
                
                final String jsonOutput = jsonOutputStr;
                System.out.println("Live price raw output: " + jsonOutput);
                javafx.application.Platform.runLater(() -> {
                    boolean updated = false;
                    if (jsonOutput != null && jsonOutput.startsWith("{")) {
                        String clean = jsonOutput.trim();
                        if (clean.startsWith("{") && clean.endsWith("}")) {
                            clean = clean.substring(1, clean.length() - 1).trim();
                        }
                        if(!clean.isEmpty()){
                            String[] pairs = clean.split(",");
                            Map<String, Double> prices = new HashMap<>();
                            for (String pair : pairs) {
                                String[] kv = pair.split(":", 2);
                                if (kv.length == 2) {
                                    String key = kv[0].replace("\"", "").trim();
                                    String value = kv[1].replace("\"", "").trim();
                                    try {
                                        prices.put(key, Double.parseDouble(value));
                                    } catch (NumberFormatException ignored) {
                                    }
                                }
                            }
                            
                            double totalInvested = 0;
                            double currentTotal = 0;
                            
                            for (Holding h : holdingsList) {
                                Double price = prices.get(h.getTicker());
                                if (price != null && price > 0) {
                                    h.setLtp(price);
                                    h.setCurrentValue(price * h.getQuantity());
                                } else {
                                    h.setLtp(h.getAvgCost());
                                    h.setCurrentValue(h.getInvested());
                                }
                                h.setPnl(h.getCurrentValue() - h.getInvested());
                                
                                totalInvested += h.getInvested();
                                currentTotal += h.getCurrentValue();
                            }
                            
                            holdingsTable.refresh();
                            
                            totalInvestedLabel.setText(UserSession.getInstance().formatCurrency(totalInvested));
                            currentValueLabel.setText(UserSession.getInstance().formatCurrency(currentTotal));
                            
                            double pnl = currentTotal - totalInvested;
                            double pnlPct = (totalInvested > 0) ? (pnl / totalInvested) * 100 : 0;
                            pnlLabel.setText(String.format("%s (%.2f%%)", UserSession.getInstance().formatCurrency(pnl), pnlPct));
                            if (pnl >= 0) pnlLabel.setStyle("-fx-text-fill: #22c55e; -fx-font-weight: bold; -fx-font-size: 14px;");
                            else pnlLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold; -fx-font-size: 14px;");
                            
                            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
                            for(Holding h: holdingsList){
                                pieData.add(new PieChart.Data(h.getTicker(), h.getCurrentValue()));
                            }
                            sectorPieChart.setData(pieData);
                            updated = true;
                        }
                    }
                    if (!updated) {
                        double totalInvested = 0;
                        for (Holding h : holdingsList) {
                            h.setLtp(h.getAvgCost());
                            h.setCurrentValue(h.getInvested());
                            h.setPnl(0);
                            totalInvested += h.getInvested();
                        }
                        holdingsTable.refresh();
                        totalInvestedLabel.setText(UserSession.getInstance().formatCurrency(totalInvested));
                        currentValueLabel.setText(UserSession.getInstance().formatCurrency(totalInvested));
                        pnlLabel.setText(String.format("%s (%.2f%%)", UserSession.getInstance().formatCurrency(0), 0.0));
                        pnlLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-weight: bold; -fx-font-size: 14px;");
                        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
                        for(Holding h : holdingsList) {
                            pieData.add(new PieChart.Data(h.getTicker(), h.getCurrentValue()));
                        }
                        sectorPieChart.setData(pieData);
                    }
                });
                scheduleBenchmarkChartUpdate();
            } catch (Exception e) {
                System.err.println("Live price error: " + e.getMessage());
            }
        }).start();
    }
    
    private void scheduleBenchmarkChartUpdate() {
        new Thread(() -> {
            try {
                Thread.sleep(250);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
            updateBenchmarkChart();
        }).start();
    }
    
    private void updateBenchmarkChart() {
        if (holdingsList.isEmpty()) return;
        StringBuilder dict = new StringBuilder("{");
        for (int i=0; i<holdingsList.size(); i++) {
            Holding h = holdingsList.get(i);
            dict.append("\"").append(h.getTicker()).append("\":").append(h.getQuantity());
            if (i < holdingsList.size() - 1) dict.append(",");
        }
        dict.append("}");
        
        new Thread(() -> {
            try {
                ProcessBuilder pb = new ProcessBuilder(".\\.venv\\Scripts\\python.exe", "scripts/python/fetch_benchmark.py", dict.toString());
                Process p = pb.start();
                java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(p.getInputStream()));
                
                boolean finished = false;
                String jsonOutput = "";
                String line;
                
                // Read output concurrently or before wait depending on buffer, 
                // but since we read in a while loop, it might block. We will just read until it closes.
                // To properly timeout readLine, we could use a different approach, but for now:
                while ((line = reader.readLine()) != null) if (line.startsWith("{")) jsonOutput = line;
                
                finished = p.waitFor(8, java.util.concurrent.TimeUnit.SECONDS);
                if (!finished) p.destroyForcibly();
                
                if (!jsonOutput.isEmpty() && jsonOutput.contains("dates")) {
                    String[] parts = jsonOutput.split("\\]");
                    String dPart = parts[0].substring(parts[0].indexOf("\\[") + 1);
                    String nPart = parts[1].substring(parts[1].indexOf("\\[") + 1);
                    String pPart = parts[2].substring(parts[2].indexOf("\\[") + 1);
                    
                    String[] dates = dPart.replace("\"", "").split(",");
                    String[] niftys = nPart.split(",");
                    String[] ports = pPart.split(",");
                    
                    javafx.application.Platform.runLater(() -> {
                        javafx.scene.chart.XYChart.Series<String, Number> nSeries = new javafx.scene.chart.XYChart.Series<>();
                        nSeries.setName("Nifty 50");
                        javafx.scene.chart.XYChart.Series<String, Number> pSeries = new javafx.scene.chart.XYChart.Series<>();
                        pSeries.setName("Portfolio");
                        for (int i=0; i<dates.length; i++) {
                            if (i >= niftys.length || i >= ports.length) break;
                            try {
                                nSeries.getData().add(new javafx.scene.chart.XYChart.Data<>(dates[i].trim(), Double.parseDouble(niftys[i].trim())));
                                pSeries.getData().add(new javafx.scene.chart.XYChart.Data<>(dates[i].trim(), Double.parseDouble(ports[i].trim())));
                            } catch(Exception ignored) {}
                        }
                        benchmarkChart.getData().clear();
                        benchmarkChart.getData().addAll(pSeries, nSeries);
                    });
                }
            } catch (Exception e) {}
        }).start();
    }
    
    @FXML private void handleLogout() {
        UserSession session = UserSession.getInstance();
        session.logout();
        
        try {
            javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource("/fxml/LoginView.fxml"));
            totalInvestedLabel.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML private void handleGoToProfile() { showProfile(); }
    @FXML private void handleGoToDashboard() { showDashboard(); }
    @FXML private void handleGoToPortfolio() { navigate("/fxml/Onboarding.fxml"); }
    @FXML private void handleGoToHistory() { navigate("/fxml/History.fxml"); }
    @FXML private void handleGoToSettings() { navigate("/fxml/Settings.fxml"); }

    private void showProfile() {
        if (profileDetailsCard != null && dashboardContent != null) {
            profileDetailsCard.setVisible(true);
            profileDetailsCard.setManaged(true);
            dashboardContent.setVisible(false);
            dashboardContent.setManaged(false);
        }
    }

    private void showDashboard() {
        if (profileDetailsCard != null && dashboardContent != null) {
            profileDetailsCard.setVisible(false);
            profileDetailsCard.setManaged(false);
            dashboardContent.setVisible(true);
            dashboardContent.setManaged(true);
        }
    }

    private void navigate(String path) {
        try {
            javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource(path));
            totalInvestedLabel.getScene().setRoot(root);
        } catch (Exception e) { e.printStackTrace(); }
    }
}

