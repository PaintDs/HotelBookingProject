package com.example.hotelbookingapp.API;

import java.util.List;
import com.example.hotelbookingapp.Model.BookingModel;
import com.example.hotelbookingapp.Model.BookingRequest;
import com.example.hotelbookingapp.Model.Hotel;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {

    // --- 1. LẤY KHÁCH SẠN THEO TỌA ĐỘ ---
    @Headers("ngrok-skip-browser-warning: true")
    @GET("hotels/nearby")
    Call<List<Hotel>> getNearbyHotels(
            @Query("my_lat") double lat,
            @Query("my_lng") double lng
    );

    // --- 2. LẤY LỊCH SỬ ĐẶT PHÒNG (Đã thêm Header Ngrok) ---
    @Headers("ngrok-skip-browser-warning: true")
    @GET("bookings")
    Call<List<BookingModel>> getBookingHistory();

    // --- 3. LẤY DANH SÁCH KHÁCH SẠN MẶC ĐỊNH (Đã thêm Header Ngrok) ---
    @Headers("ngrok-skip-browser-warning: true")
    @GET("hotels")
    Call<List<Hotel>> getHotels();

    // --- 4. TẠO ĐƠN ĐẶT PHÒNG MỚI (POST thường không cần Header Ngrok nhưng thêm vào cho chắc cũng được) ---
    @Headers("ngrok-skip-browser-warning: true")
    @POST("book")
    Call<ResponseBody> postBooking(@Body BookingRequest request);
}