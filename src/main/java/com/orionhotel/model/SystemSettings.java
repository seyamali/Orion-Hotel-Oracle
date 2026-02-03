package com.orionhotel.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class SystemSettings implements Serializable {
    private static final long serialVersionUID = 1L;

    // Hotel Info
    private String hotelName = "Orion Hotel";
    private String hotelAddress = "123 Galaxy Way, Star City";
    private String hotelPhone = "555-0199";
    private String hotelEmail = "contact@orionhotel.com";
    private String currencySymbol = "$";

    // Financials
    private double taxRate = 0.10; // 10%
    private double serviceChargeRate = 0.05; // 5%

    // Room Pricing (Base prices)
    private Map<String, Double> roomPrices;

    // Security
    private int passwordMinLength = 8;
    private int sessionTimeoutMinutes = 30;

    // Notifications
    private boolean emailNotificationsEnabled = true;
    private boolean systemAlertsEnabled = true;

    public SystemSettings() {
        roomPrices = new HashMap<>();
        roomPrices.put("Single", 100.0);
        roomPrices.put("Double", 150.0);
        roomPrices.put("Suite", 300.0);
    }

    // Getters and Setters
    public String getHotelName() {
        return hotelName;
    }

    public void setHotelName(String hotelName) {
        this.hotelName = hotelName;
    }

    public String getHotelAddress() {
        return hotelAddress;
    }

    public void setHotelAddress(String hotelAddress) {
        this.hotelAddress = hotelAddress;
    }

    public String getHotelPhone() {
        return hotelPhone;
    }

    public void setHotelPhone(String hotelPhone) {
        this.hotelPhone = hotelPhone;
    }

    public String getHotelEmail() {
        return hotelEmail;
    }

    public void setHotelEmail(String hotelEmail) {
        this.hotelEmail = hotelEmail;
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
    }

    public double getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(double taxRate) {
        this.taxRate = taxRate;
    }

    public double getServiceChargeRate() {
        return serviceChargeRate;
    }

    public void setServiceChargeRate(double rate) {
        this.serviceChargeRate = rate;
    }

    public Map<String, Double> getRoomPrices() {
        return roomPrices;
    }

    public void setRoomPrices(Map<String, Double> roomPrices) {
        this.roomPrices = roomPrices;
    }

    public double getRoomPrice(String type) {
        return roomPrices.getOrDefault(type, 100.0);
    }

    public void setRoomPrice(String type, double price) {
        roomPrices.put(type, price);
    }

    public int getPasswordMinLength() {
        return passwordMinLength;
    }

    public void setPasswordMinLength(int passwordMinLength) {
        this.passwordMinLength = passwordMinLength;
    }

    public int getSessionTimeoutMinutes() {
        return sessionTimeoutMinutes;
    }

    public void setSessionTimeoutMinutes(int sessionTimeoutMinutes) {
        this.sessionTimeoutMinutes = sessionTimeoutMinutes;
    }

    public boolean isEmailNotificationsEnabled() {
        return emailNotificationsEnabled;
    }

    public void setEmailNotificationsEnabled(boolean emailNotificationsEnabled) {
        this.emailNotificationsEnabled = emailNotificationsEnabled;
    }

    public boolean isSystemAlertsEnabled() {
        return systemAlertsEnabled;
    }

    public void setSystemAlertsEnabled(boolean systemAlertsEnabled) {
        this.systemAlertsEnabled = systemAlertsEnabled;
    }
}
