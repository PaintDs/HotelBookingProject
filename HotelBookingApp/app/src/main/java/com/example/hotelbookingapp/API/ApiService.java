package com.example.hotelbookingapp.API;

import com.example.hotelbookingapp.Model.BookingModel;
import com.example.hotelbookingapp.Model.BookingRequest;
import com.example.hotelbookingapp.Model.Hotel;
import com.example.hotelbookingapp.Model.LoginRequest;
import com.example.hotelbookingapp.Model.LoginResponse;
import com.example.hotelbookingapp.Model.RegisterRequest;
import com.google.gson.JsonObject;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {

    // 1. Lấy khách sạn gần đây
    @Headers("ngrok-skip-browser-warning: true")
    @GET("hotels/nearby")
    Call<List<Hotel>> getNearbyHotels(@Query("my_lat") double lat, @Query("my_lng") double lng);

    // 2. LẤY TẤT CẢ KHÁCH SẠN
    @Headers("ngrok-skip-browser-warning: true")
    @GET("hotels")
    Call<List<Hotel>> getHotels();

    // 3. LẤY LỊCH SỬ (QUAN TRỌNG: Phải truyền user_email vào đây)
    @Headers("ngrok-skip-browser-warning: true")
    @GET("bookings")
    Call<List<BookingModel>> getBookingHistory(@Query("user_email") String email);

    // 4. ĐẶT PHÒNG MỚI
    @Headers("ngrok-skip-browser-warning: true")
    @POST("book")
    Call<ResponseBody> postBooking(@Body BookingRequest request);

    // 5. ĐĂNG NHẬP & ĐĂNG KÝ
    @POST("login")
    Call<LoginResponse> loginUser(@Body LoginRequest request);

    @POST("register")
    Call<JsonObject> registerUser(@Body RegisterRequest request);
}