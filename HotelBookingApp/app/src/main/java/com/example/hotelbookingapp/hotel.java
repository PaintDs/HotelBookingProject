package com.example.hotelbookingapp;

import com.google.gson.annotations.SerializedName;

public class hotel {

    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("address")
    private String address;

    @SerializedName("description")
    private String description;

    @SerializedName("image_url")
    private String image_url;

    // Đã sửa lại thành "price_per_night" cho khớp với JSON từ Server
    @SerializedName("price_per_night")
    private Double price_per_night; // Đổi sang Double (D viết hoa)

    @SerializedName("distance")
    private Double distance; // Đổi sang Double (D viết hoa)

    @SerializedName("lat")
    private Double lat; // Đổi sang Double (D viết hoa)

    @SerializedName("lng")
    private Double lng; // Đổi sang Double (D viết hoa)

    // --- GETTERS ---
    public int getId() { return id; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public String getDescription() { return description; }
    public String getImageUrl() { return image_url; }

    // Các Getter này cũng phải trả về Double (D viết hoa)
    public Double getPrice_per_night() { return price_per_night; }
    public Double getDistance() { return distance; }
    public Double getLat() { return lat; }
    public Double getLng() { return lng; }

    // --- SETTERS ---
    public void setPrice_per_night(Double price_per_night) { this.price_per_night = price_per_night; }
    public void setDistance(Double distance) { this.distance = distance; }
    public void setLat(Double lat) { this.lat = lat; }
    public void setLng(Double lng) { this.lng = lng; }
}