package com.example.hotelbookingapp;

import com.google.gson.annotations.SerializedName;

public class BookingModel {
    private int id;

    @SerializedName("customer_name")
    private String customer_name;

    private String cccd;

    @SerializedName("hotel_name")
    private String hotel_name;

    private int hotel_id;

    // PHẦN QUAN TRỌNG: Thêm biến này để hết lỗi đỏ ở Adapter
    @SerializedName("total_price")
    private int total_price;

    // Getter và Setter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCustomer_name() { return customer_name; }
    public void setCustomer_name(String customer_name) { this.customer_name = customer_name; }

    public String getCccd() { return cccd; }
    public void setCccd(String cccd) { this.cccd = cccd; }

    public String getHotel_name() { return hotel_name; }
    public void setHotel_name(String hotel_name) { this.hotel_name = hotel_name; }

    public int getHotel_id() { return hotel_id; }
    public void setHotel_id(int hotel_id) { this.hotel_id = hotel_id; }

    // Thêm hàm này để dòng 35 bên HistoryAdapter hết báo lỗi
    public int getTotal_price() { return total_price; }
    public void setTotal_price(int total_price) { this.total_price = total_price; }
}