module com.tradematrix {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    
    opens com.tradematrix to javafx.fxml;
    exports com.tradematrix;
}
