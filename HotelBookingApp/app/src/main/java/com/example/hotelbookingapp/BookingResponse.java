package com.example.hotelbookingapp;

import com.google.gson.annotations.SerializedName;

public class BookingResponse {

    @SerializedName("id")
    private int id;

    @SerializedName("hotel_name")
    private String hotel_name;

    @SerializedName("customer_name")
    private String customer_name;

    @SerializedName("cccd")
    private String cccd;

    @SerializedName("total_price")
    private float total_price;

    // --- GETTER (Để Adapter lấy dữ liệu ra hiển thị) ---
    public int getId() { return id; }
    public String getHotel_name() { return hotel_name; }
    public String getCustomer_name() { return customer_name; }
    public String getCccd() { return cccd; }
    public float getTotal_price() { return total_price; }

    // --- SETTER (Nếu cần thiết) ---
    public void setId(int id) { this.id = id; }
    public void setHotel_name(String hotel_name) { this.hotel_name = hotel_name; }
    public void setCustomer_name(String customer_name) { this.customer_name = customer_name; }
    public void setCccd(String cccd) { this.cccd = cccd; }
    public void setTotal_price(float total_price) { this.total_price = total_price; }
}