package com.example.madinabakersresutrant;

public class FoodModel {
    private String foodName;
    private String Price;
    private String category;
    private String img;

    public String getItemId() {
        return itemId;
    }

    public FoodModel( String foodName,String price,String category, String img, String itemId ) {
        this.category = category;
        this.foodName = foodName;
        this.img = img;
        this.itemId = itemId;
        Price = price;
    }

    private String itemId;

    public FoodModel() {} // Required for Firebase

    public FoodModel(String foodName, String price, String category, String img) {
        this.foodName = foodName;
        this.Price = price;
        this.category = category;
        this.img = img;
    }

    public String getFoodName() {
        return foodName;
    }

    public String getPrice() {
        return Price;
    }

    public String getCategory() {
        return category;
    }

    public String getImg() {
        return img;
    }
}
