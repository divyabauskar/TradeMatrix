package com.tradematrix;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    // Requires MySQL running locally
    private static final String DB_URL = "jdbc:mysql://localhost:3306/tradematrix?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASS = "123456";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }

    public static void initializeDatabase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL Driver not found in Classpath!");
        }
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
             
            String createUsers = "CREATE TABLE IF NOT EXISTS user_profile (" +
                                 "id INT AUTO_INCREMENT PRIMARY KEY, " +
                                 "username VARCHAR(100), " +
                                 "email VARCHAR(255), " +
                                 "full_name VARCHAR(255), " +
                                 "password VARCHAR(255), " +
                                 "mobile_number VARCHAR(20), " +
                                 "base_currency VARCHAR(10) DEFAULT 'INR', " +
                                 "created_at DATETIME DEFAULT CURRENT_TIMESTAMP)";
            stmt.execute(createUsers);

            String createTx = "CREATE TABLE IF NOT EXISTS transactions (" +
                              "id INT AUTO_INCREMENT PRIMARY KEY, " +
                              "user_id INT DEFAULT 0, " +
                              "ticker VARCHAR(100) NOT NULL, " +
                              "transaction_type ENUM('Buy', 'Sell') NOT NULL, " +
                              "quantity DOUBLE NOT NULL, " +
                              "price_per_share DOUBLE NOT NULL, " +
                              "transaction_date DATE NOT NULL, " +
                              "created_at DATETIME DEFAULT CURRENT_TIMESTAMP)";
            stmt.execute(createTx);

            // Migrate existing schema if column is missing
            if (!columnExists(conn, "user_profile", "username")) {
                stmt.execute("ALTER TABLE user_profile ADD COLUMN username VARCHAR(100)");
            }
            if (!columnExists(conn, "user_profile", "email")) {
                stmt.execute("ALTER TABLE user_profile ADD COLUMN email VARCHAR(255)");
            }
            if (!columnExists(conn, "user_profile", "password")) {
                stmt.execute("ALTER TABLE user_profile ADD COLUMN password VARCHAR(255)");
            }
            if (!columnExists(conn, "user_profile", "base_currency")) {
                stmt.execute("ALTER TABLE user_profile ADD COLUMN base_currency VARCHAR(10) DEFAULT 'INR'");
            }
            if (!columnExists(conn, "transactions", "user_id")) {
                stmt.execute("ALTER TABLE transactions ADD COLUMN user_id INT DEFAULT 0");
            }
            
            System.out.println("MySQL database 'tradematrix' initialized successfully.");
        } catch (SQLException e) {
            System.err.println("MySQL Database initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static boolean columnExists(Connection conn, String tableName, String columnName) throws SQLException {
        try (ResultSet rs = conn.getMetaData().getColumns(null, null, tableName, columnName)) {
            return rs.next();
        }
    }
}

