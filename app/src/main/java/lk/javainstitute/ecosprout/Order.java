package lk.javainstitute.ecosprout;

import com.google.firebase.Timestamp;

import java.util.ArrayList;

public class Order {
    private String orderId;
    private String userId;
    private ArrayList<CartItem> items;
    private double totalPrice;
    private Timestamp timestamp;

    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Empty constructor required for Firestore
    public Order() {}

    public Order(String orderId, String userId, ArrayList<CartItem> items, double totalPrice, Timestamp timestamp, String status) {
        this.orderId = orderId;
        this.userId = userId;
        this.items = items;
        this.totalPrice = totalPrice;
        this.timestamp = timestamp;
        this.status = status;
    }

    // Getters and setters
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public ArrayList<CartItem> getItems() {
        return items;
    }

    public void setItems(ArrayList<CartItem> items) {
        this.items = items;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
