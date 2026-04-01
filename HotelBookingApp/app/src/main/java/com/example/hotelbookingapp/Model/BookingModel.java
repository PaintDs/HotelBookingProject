package com.example.hotelbookingapp.Model;

import com.google.gson.annotations.SerializedName;

// =====================================================================
// LỚP MÔ HÌNH DỮ LIỆU (DATA MODEL / DTO - Data Transfer Object)
// Nhiệm vụ: Đóng vai trò như một "cái khuôn" (Blueprint) để thư viện Gson
// đổ dữ liệu JSON từ API (Backend Python) vào các đối tượng Java.
// =====================================================================
public class BookingModel {

    // =====================================================================
    // 1. TÍNH ĐÓNG GÓI (ENCAPSULATION - Nền tảng OOP)
    // Nhiệm vụ: Đặt tất cả các biến là 'private' để bảo vệ dữ liệu,
    // không cho phép các class khác can thiệp sửa đổi trực tiếp gây lỗi hệ thống.
    // =====================================================================
    private int id;
    private String cccd;
    private int hotel_id;

    // =====================================================================
    // 2. ÁNH XẠ DỮ LIỆU JSON (DATA MAPPING & PARSING)
    // @SerializedName: Chìa khóa vàng của thư viện Gson.
    // Nhiệm vụ: Đảm bảo App không bị Crash (Null dữ liệu) khi Backend trả về JSON.
    // Nó báo cho Android biết: "Hãy tìm cái key 'customer_name' trong cục JSON
    // mà Python gửi về, và nhét giá trị của nó vào cái biến ở ngay bên dưới".
    // =====================================================================
    @SerializedName("customer_name")
    private String customer_name;

    @SerializedName("hotel_name")
    private String hotel_name;

    @SerializedName("total_price")
    private int total_price;

    // =====================================================================
    // 3. CỔNG GIAO TIẾP (GETTER & SETTER)
    // Nhiệm vụ: Cung cấp các hàm 'public' để đọc (Get) hoặc ghi (Set) dữ liệu
    // một cách an toàn và có kiểm soát từ bên ngoài (như từ Adapter hay Activity).
    // =====================================================================
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

    // Cung cấp tổng tiền để HistoryAdapter lấy ra hiển thị lên giao diện
    public int getTotal_price() { return total_price; }
    public void setTotal_price(int total_price) { this.total_price = total_price; }
}