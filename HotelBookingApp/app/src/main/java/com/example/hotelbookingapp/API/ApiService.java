package com.example.hotelbookingapp.API;

import java.util.List;

import com.example.hotelbookingapp.Model.BookingModel;
import com.example.hotelbookingapp.Model.BookingRequest;
import com.example.hotelbookingapp.Model.Hotel;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

// =====================================================================
// INTERFACE: API SERVICE (Giao diện cấu hình Mạng)
// Nhiệm vụ: Sử dụng thư viện Retrofit để biến các HTTP API (định tuyến trên mạng)
// thành các hàm Java có thể gọi trực tiếp trong code.
// Tuân thủ kiến trúc RESTful API chuẩn mực.
// =====================================================================
public interface ApiService {

    // =====================================================================
    // 1. GET: LẤY DANH SÁCH KHÁCH SẠN GẦN NHẤT (Luồng chính có GPS)
    // - @Headers: Bypass (Vượt qua) trang cảnh báo HTML mặc định của Ngrok Free.
    //   Nếu không có dòng này, App sẽ crash vì Retrofit cố gắng ép kiểu trang web HTML thành JSON.
    // - @GET: Khai báo phương thức lấy dữ liệu từ URL "hotels/nearby"
    // - @Query: Tự động nối tham số vào URL (VD: .../hotels/nearby?my_lat=21.0&my_lng=105.8)
    // =====================================================================
    @Headers("ngrok-skip-browser-warning: true")
    @GET("hotels/nearby")
    Call<List<Hotel>> getNearbyHotels(
            @Query("my_lat") double lat,
            @Query("my_lng") double lng
    );

    // =====================================================================
    // 2. GET: LẤY LỊCH SỬ ĐẶT PHÒNG
    // Nhiệm vụ: Lấy toàn bộ danh sách đơn hàng đã lưu trong Database
    // Trả về một Call (Tiến trình bất đồng bộ) chứa một Mảng (List) các BookingModel
    // =====================================================================
    @GET("bookings")
    Call<List<BookingModel>> getBookingHistory();

    // =====================================================================
    // 3. GET: LẤY DANH SÁCH KHÁCH SẠN MẶC ĐỊNH (Luồng Fallback - Dự phòng)
    // Nhiệm vụ: Gọi khi người dùng TỪ CHỐI cấp quyền GPS hoặc máy mất định vị.
    // Không truyền tham số, chỉ lấy danh sách tĩnh.
    // =====================================================================
    @GET("hotels")
    Call<List<Hotel>> getHotels();

    // =====================================================================
    // 4. POST: TẠO ĐƠN ĐẶT PHÒNG MỚI
    // - @POST: Gửi dữ liệu bảo mật lên Server (Không lộ trên URL như GET)
    // - @Body: Tự động Serialize (Chuyển đổi) Object BookingRequest của Java
    //   thành định dạng JSON chuẩn trước khi bắn sang cho Python xử lý.
    // =====================================================================
    @POST("book")
    Call<okhttp3.ResponseBody> postBooking(@Body BookingRequest request);
}