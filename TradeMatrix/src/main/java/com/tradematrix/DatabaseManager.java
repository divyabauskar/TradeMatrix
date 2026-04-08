package com.tradematrix;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DatabaseManager {
    private static final String DB_URL = buildSqliteUrl();

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
             
            String createUsers = "CREATE TABLE IF NOT EXISTS user_profile (" +
                                 "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                 "username TEXT, " +
                                 "email TEXT, " +
                                 "full_name TEXT, " +
                                 "password TEXT, " +
                                 "mobile_number TEXT, " +
                                 "base_currency TEXT DEFAULT 'INR', " +
                                 "created_at TEXT DEFAULT CURRENT_TIMESTAMP)";
            stmt.execute(createUsers);

            String createTx = "CREATE TABLE IF NOT EXISTS transactions (" +
                              "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                              "user_id INTEGER DEFAULT 0, " +
                              "ticker TEXT NOT NULL, " +
                              "transaction_type TEXT NOT NULL CHECK(transaction_type IN ('Buy','Sell')), " +
                              "quantity REAL NOT NULL, " +
                              "price_per_share REAL NOT NULL, " +
                              "transaction_date TEXT NOT NULL, " +
                              "created_at TEXT DEFAULT CURRENT_TIMESTAMP)";
            stmt.execute(createTx);

            // Migrate existing schema if column is missing
            if (!columnExists(conn, "user_profile", "username")) {
                stmt.execute("ALTER TABLE user_profile ADD COLUMN username TEXT");
            }
            if (!columnExists(conn, "user_profile", "email")) {
                stmt.execute("ALTER TABLE user_profile ADD COLUMN email TEXT");
            }
            if (!columnExists(conn, "user_profile", "password")) {
                stmt.execute("ALTER TABLE user_profile ADD COLUMN password TEXT");
            }
            if (!columnExists(conn, "user_profile", "base_currency")) {
                stmt.execute("ALTER TABLE user_profile ADD COLUMN base_currency TEXT DEFAULT 'INR'");
            }
            if (!columnExists(conn, "transactions", "user_id")) {
                stmt.execute("ALTER TABLE transactions ADD COLUMN user_id INTEGER DEFAULT 0");
            }
            
            System.out.println("SQLite database initialized successfully.");
        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static boolean columnExists(Connection conn, String tableName, String columnName) throws SQLException {
        try (ResultSet rs = conn.getMetaData().getColumns(null, null, tableName, columnName)) {
            return rs.next();
        }
    }

    private static String buildSqliteUrl() {
        try {
            Path appDir = Paths.get(System.getProperty("user.home"), ".tradematrix");
            Files.createDirectories(appDir);
            Path dbPath = appDir.resolve("tradematrix.db");
            return "jdbc:sqlite:" + dbPath.toAbsolutePath();
        } catch (Exception e) {
            // Last resort: current directory
            return "jdbc:sqlite:tradematrix.db";
        }
    }
}

