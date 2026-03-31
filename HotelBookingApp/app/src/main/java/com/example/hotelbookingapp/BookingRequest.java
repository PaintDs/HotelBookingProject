package com.example.hotelbookingapp;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class BookingRequest {
    public int hotel_id;
    public String hotel_name;
    public String customer_name;
    public String cccd;         // <-- Chỗ chứa thứ 4 mới thêm
    public double total_price;  // <-- Chỗ chứa thứ 5 bị đẩy xuống

    // Cập nhật lại Constructor để khớp đúng thứ tự 5 món
    public BookingRequest(int hotel_id, String hotel_name, String customer_name, String cccd, double total_price) {
        this.hotel_id = hotel_id;
        this.hotel_name = hotel_name;
        this.customer_name = customer_name;
        this.cccd = cccd;
        this.total_price = total_price;
    }


}