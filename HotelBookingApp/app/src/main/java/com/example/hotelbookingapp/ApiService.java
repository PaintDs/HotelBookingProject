package com.example.hotelbookingapp;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {
    @Headers("ngrok-skip-browser-warning: true")
    // Gọi API lấy danh sách khách sạn gần nhất dựa trên tọa độ
    @GET("hotels/nearby")
    Call<List<hotel>> getNearbyHotels(
            @Query("my_lat") double lat,
            @Query("my_lng") double lng
    );
    // Trong ApiService.java
    @GET("bookings")
    Call<List<BookingModel>> getBookingHistory(); // Đổi BookingResponse thành BookingModel
    @GET("hotels")
    Call<List<hotel>> getHotels();

    @POST("book")
    Call<okhttp3.ResponseBody> postBooking(@Body BookingRequest request);
}
