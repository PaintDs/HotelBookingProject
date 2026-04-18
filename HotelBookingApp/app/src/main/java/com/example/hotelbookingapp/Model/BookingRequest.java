package com.example.hotelbookingapp.Model;

import com.google.gson.annotations.SerializedName;

public class BookingRequest {
    @SerializedName("hotel_id")
    public int hotel_id;

    @SerializedName("hotel_name")
    public String hotel_name;

    @SerializedName("customer_name")
    public String customer_name;

    @SerializedName("cccd")
    public String cccd;

    @SerializedName("total_price")
    public double total_price;

    // PHẢI CÓ @SerializedName("user_email") để khớp với cột user_email ở Python
    @SerializedName("user_email")
    public String user_email;

    public BookingRequest(int hotel_id, String hotel_name, String customer_name, String cccd, double total_price, String user_email) {
        this.hotel_id = hotel_id;
        this.hotel_name = hotel_name;
        this.customer_name = customer_name;
        this.cccd = cccd;
        this.total_price = total_price;
        this.user_email = user_email; // Đảm bảo dòng này đã có
    }
}