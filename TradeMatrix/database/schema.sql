CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    mobile_number TEXT UNIQUE NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS transactions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    ticker TEXT NOT NULL,
    transaction_type TEXT NOT NULL CHECK(transaction_type IN ('Buy', 'Sell')),
    quantity REAL NOT NULL,
    price_per_share REAL NOT NULL,
    transaction_date TEXT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
