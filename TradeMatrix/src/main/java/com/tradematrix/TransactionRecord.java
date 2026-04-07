package com.tradematrix;

public class TransactionRecord {
    private String date;
    private String ticker;
    private String type;
    private double quantity;
    private double price;
    private double totalAmount;

    public TransactionRecord(String date, String ticker, String type, double quantity, double price) {
        this.date = date;
        this.ticker = ticker;
        this.type = type;
        this.quantity = quantity;
        this.price = price;
        this.totalAmount = quantity * price;
    }

    public String getDate() { return date; }
    public String getTicker() { return ticker; }
    public String getType() { return type; }
    public double getQuantity() { return quantity; }
    public double getPrice() { return price; }
    public double getTotalAmount() { return totalAmount; }
}
