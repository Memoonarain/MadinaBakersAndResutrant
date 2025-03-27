package com.example.madinabakersresutrant;

import java.util.List;

public class OrderModel {
    private String orderId, userId, status, paymentMethod, deliveryType, address;
    private long timestamp;
    private List<CartItem> items;
    private int totalAmount;

    public OrderModel() {} // Needed for Firebase

    public OrderModel(String orderId, String userId, String status, List<CartItem> items,
                      int totalAmount, String paymentMethod, String deliveryType,
                      String address, long timestamp) {
        this.orderId = orderId;
        this.userId = userId;
        this.status = status;
        this.items = items;
        this.totalAmount = totalAmount;
        this.paymentMethod = paymentMethod;
        this.deliveryType = deliveryType;
        this.address = address;
        this.timestamp = timestamp;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDeliveryType() {
        return deliveryType;
    }

    public void setDeliveryType(String deliveryType) {
        this.deliveryType = deliveryType;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(int totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
// Getters and setters...
}
