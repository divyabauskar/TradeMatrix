package com.tradematrix;

public class Holding {
    private String ticker;
    private double quantity;
    private double avgCost;
    private double ltp;
    private double currentValue;
    private double pnl;

    public Holding(String ticker) {
        this.ticker = ticker;
    }

    public String getTicker() { return ticker; }
    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }
    public double getAvgCost() { return avgCost; }
    public void setAvgCost(double avgCost) { this.avgCost = avgCost; }
    public double getLtp() { return ltp; }
    public void setLtp(double ltp) { this.ltp = ltp; }
    
    public double getInvested() { return quantity * avgCost; }
    
    public double getCurrentValue() { return currentValue; }
    public void setCurrentValue(double currentValue) { this.currentValue = currentValue; }
    public double getPnl() { return pnl; }
    public void setPnl(double pnl) { this.pnl = pnl; }
}
