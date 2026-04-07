package com.tradematrix;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        DatabaseManager.initializeDatabase();
        javafx.fxml.FXMLLoader fxmlLoader = new javafx.fxml.FXMLLoader(App.class.getResource("/fxml/LoginView.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
        stage.setTitle("TradeMatrix - Local Stock Tracker");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
