package com.orionhotel.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import java.io.Serializable;

public class Bill implements Serializable {
    private static final long serialVersionUID = 1L;
    private int billId;
    private int guestId;
    private String guestName;
    private double roomCharges;
    private List<ServiceCharge> serviceCharges;
    private double taxes;
    private double discount;
    private double totalAmount;
    private PaymentStatus paymentStatus;
    private PaymentMethod paymentMethod;
    private LocalDate billDate;

    public enum PaymentStatus {
        UNPAID, PAID, PARTIAL
    }

    public enum PaymentMethod {
        CASH, CARD, MOBILE, BANK
    }

    public static class ServiceCharge implements Serializable {
        private static final long serialVersionUID = 1L;
        public String serviceType;
        public double amount;
        public LocalDate date;

        public ServiceCharge(String serviceType, double amount, LocalDate date) {
            this.serviceType = serviceType;
            this.amount = amount;
            this.date = date;
        }
    }

    public Bill(int billId, int guestId, String guestName) {
        this.billId = billId;
        this.guestId = guestId;
        this.guestName = guestName;
        this.serviceCharges = new ArrayList<>();
        this.paymentStatus = PaymentStatus.UNPAID;
        this.billDate = LocalDate.now();
    }

    // Getters and Setters
    public int getBillId() {
        return billId;
    }

    public void setBillId(int billId) {
        this.billId = billId;
    }

    public int getGuestId() {
        return guestId;
    }

    public void setGuestId(int guestId) {
        this.guestId = guestId;
    }

    public String getGuestName() {
        return guestName;
    }

    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }

    public double getRoomCharges() {
        return roomCharges;
    }

    public void setRoomCharges(double roomCharges) {
        this.roomCharges = roomCharges;
    }

    public List<ServiceCharge> getServiceCharges() {
        return serviceCharges;
    }

    public void addServiceCharge(ServiceCharge charge) {
        this.serviceCharges.add(charge);
    }

    public void setServiceCharges(List<ServiceCharge> serviceCharges) {
        this.serviceCharges = serviceCharges;
    }

    public double getTaxes() {
        return taxes;
    }

    public void setTaxes(double taxes) {
        this.taxes = taxes;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public LocalDate getBillDate() {
        return billDate;
    }

    public void setBillDate(LocalDate billDate) {
        this.billDate = billDate;
    }

    public void recalculateTotal(double taxRate) {
        double servicesTotal = serviceCharges.stream().mapToDouble(sc -> sc.amount).sum();
        double subtotal = roomCharges + servicesTotal;
        taxes = subtotal * taxRate;
        totalAmount = subtotal + taxes - discount;
    }
}