package com.tradematrix;

public class UserSession {
    private static UserSession instance;
    private int userId;
    private String username;
    private String email;
    private String fullName;
    private String mobileNumber;
    private String baseCurrency = "INR";
    private boolean lightMode = false;
    
    private UserSession() {}
    
    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    
    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }
    
    public String getBaseCurrency() { return baseCurrency; }
    public void setBaseCurrency(String baseCurrency) { this.baseCurrency = baseCurrency; }
    
    public boolean isLightMode() { return lightMode; }
    public void setLightMode(boolean lightMode) { this.lightMode = lightMode; }
    
    public String formatCurrency(double amount) {
        if ("USD".equalsIgnoreCase(baseCurrency)) {
            return String.format("$%,.2f", amount);
        } else {
            return String.format("₹%,.2f", amount);
        }
    }
    
    public void logout() {
        userId = 0;
        username = null;
        email = null;
        fullName = null;
        mobileNumber = null;
        baseCurrency = "INR";
        lightMode = false;
    }
}
