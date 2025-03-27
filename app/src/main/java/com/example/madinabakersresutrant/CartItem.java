package com.example.madinabakersresutrant;

public class CartItem {
    private String name;
    private String itemId;
    private int price;

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public CartItem(String name, int price, int quantity, String imageUrl, String itemId) {
        this.imageUrl = imageUrl;
        this.isSelected = isSelected;
        this.itemId = itemId;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }
    public CartItem() {
        // Needed for Firebase deserialization
    }
    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    private int quantity;
    private String imageUrl; // optional, if using Glide/Picasso
    private boolean isSelected = true; // default selected

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public CartItem(String name, int price, int quantity, String imageUrl) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
    }

    public String getName() { return name; }
    public int getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public String getImageUrl() { return imageUrl; }

    public void setQuantity(int quantity) { this.quantity = quantity; }
}
