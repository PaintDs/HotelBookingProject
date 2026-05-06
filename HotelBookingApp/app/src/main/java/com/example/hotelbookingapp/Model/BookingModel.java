package com.example.hotelbookingapp.Model;

import com.google.gson.annotations.SerializedName;

public class BookingModel {

    // @SerializedName giúp Android hiểu chính xác từ khóa "id" trong JSON từ Backend
    @SerializedName("id")
    private int id;

    @SerializedName("hotel_name")
    private String hotel_name;

    @SerializedName("customer_name")
    private String customer_name;

    @SerializedName("cccd")
    private String cccd;

    @SerializedName("total_price")
    private double total_price;

    @SerializedName("status")
    private String status;

    // Các hàm Getter và Setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getHotel_name() {
        return hotel_name;
    }

    public void setHotel_name(String hotel_name) {
        this.hotel_name = hotel_name;
    }

    public String getCustomer_name() {
        return customer_name;
    }

    public void setCustomer_name(String customer_name) {
        this.customer_name = customer_name;
    }

    public String getCccd() {
        return cccd;
    }

    public void setCccd(String cccd) {
        this.cccd = cccd;
    }

    public double getTotal_price() {
        return total_price;
    }

    public void setTotal_price(double total_price) {
        this.total_price = total_price;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}