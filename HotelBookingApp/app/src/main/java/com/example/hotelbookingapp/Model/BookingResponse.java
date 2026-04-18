package com.example.hotelbookingapp.Model;

import com.google.gson.annotations.SerializedName;

// =====================================================================
// LỚP MÔ HÌNH DỮ LIỆU NHẬN VỀ (RESPONSE PAYLOAD / DTO)
// Nhiệm vụ: Hứng dữ liệu JSON từ Backend Python trả về (thường là sau khi gọi GET Lịch sử
// hoặc nhận kết quả trả về từ lệnh POST Đặt phòng).
// Thư viện Gson sẽ tự động "đổ" dữ liệu từ JSON vào cái khuôn này.
// =====================================================================
public class BookingResponse {

    // =====================================================================
    // 1. CÁC TRƯỜNG DỮ LIỆU (FIELDS) TỪ DATABASE
    // Điểm khác biệt lớn nhất so với BookingRequest: Có thêm trường 'id'.
    // Trường 'id' này do MySQL tự động sinh ra (Auto Increment) chứ người dùng không nhập.
    // Dùng @SerializedName để ép khớp chính xác 100% với tên Key trong cục JSON của Backend.
    // =====================================================================
    @SerializedName("id")
    private int id;

    @SerializedName("hotel_name")
    private String hotel_name;

    @SerializedName("customer_name")
    private String customer_name;

    @SerializedName("cccd")
    private String cccd;

    @SerializedName("total_price")
    private float total_price; // Lưu ý nhỏ: Bên Python nếu trả về số thập phân thì dùng float/double là cực chuẩn

    // =====================================================================
    // 2. CỔNG LẤY DỮ LIỆU (GETTER)
    // Nhiệm vụ: Cung cấp dữ liệu (Read-only) cho các lớp giao diện.
    // Ví dụ: Lớp HistoryAdapter sẽ gọi getHotel_name() để in tên khách sạn lên màn hình.
    // =====================================================================
    public int getId() { return id; }
    public String getHotel_name() { return hotel_name; }
    public String getCustomer_name() { return customer_name; }
    public String getCccd() { return cccd; }
    public float getTotal_price() { return total_price; }

    // =====================================================================
    // 3. CỔNG GHI DỮ LIỆU (SETTER)
    // Nhiệm vụ: Cho phép thay đổi giá trị của biến. Dù Gson thường tự động set bằng Reflection,
    // việc khai báo Setter vẫn là chuẩn mực của một POJO (Plain Old Java Object) trong Java.
    // =====================================================================
    public void setId(int id) { this.id = id; }
    public void setHotel_name(String hotel_name) { this.hotel_name = hotel_name; }
    public void setCustomer_name(String customer_name) { this.customer_name = customer_name; }
    public void setCccd(String cccd) { this.cccd = cccd; }
    public void setTotal_price(float total_price) { this.total_price = total_price; }
}