package com.example.madinabakersresutrant;

public class AddressModel {
    private String id, name, phone, city, address;

    public AddressModel() {
        // Default constructor required for Firebase
    }

    public AddressModel(String id, String name, String phone, String city, String address) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.city = city;
        this.address = address;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getCity() { return city; }
    public String getAddress() { return address; }

    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setCity(String city) { this.city = city; }
    public void setAddress(String address) { this.address = address; }
}
