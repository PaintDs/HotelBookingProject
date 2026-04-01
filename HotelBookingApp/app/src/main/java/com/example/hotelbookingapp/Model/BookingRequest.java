package com.example.hotelbookingapp.Model;

// =====================================================================
// LỚP MÔ HÌNH DỮ LIỆU GỬI ĐI (REQUEST PAYLOAD / DTO)
// Nhiệm vụ: Đóng gói các thông tin rời rạc (do người dùng nhập và dữ liệu gốc)
// thành một khối thống nhất (Object) để chuẩn bị "bắn" qua mạng lên Server Python.
// Lớp này đặc biệt phục vụ cho phương thức HTTP POST (Tạo mới đơn đặt phòng).
// =====================================================================
public class BookingRequest {

    // =====================================================================
    // 1. CẤU TRÚC GÓI TIN (PAYLOAD STRUCTURE)
    // Các biến này được đặt là 'public' (thay vì private + Get/Set) để hoạt động
    // như một Struct (Cấu trúc dữ liệu thuần túy). Thư viện Gson sẽ đọc trực tiếp
    // tên các biến này để tạo ra các Key trong file JSON tương ứng gửi lên Backend.
    //
    // Yêu cầu bắt buộc: Tên biến ở đây PHẢI KHỚP 100% với tên trường (field)
    // mà API Python FastAPI đang chờ đợi (Pydantic BaseModel).
    // =====================================================================
    public int hotel_id;
    public String hotel_name;
    public String customer_name;
    public String cccd;
    public double total_price;

    // =====================================================================
    // 2. HÀM KHỞI TẠO TỔNG HỢP (AGGREGATION CONSTRUCTOR)
    // Nhiệm vụ: "Thu gom" dữ liệu từ 2 nguồn khác nhau vào chung một chỗ:
    // - Nguồn 1 (Từ UI): customer_name, cccd (Do người dùng gõ vào EditText).
    // - Nguồn 2 (Từ Hệ thống): hotel_id, hotel_name, total_price (Truyền ngầm qua Intent).
    // Bằng cách ép buộc dùng Constructor này, Lập trình viên sẽ không bao giờ
    // quên truyền thiếu tham số khi gửi Request lên Server.
    // =====================================================================
    public BookingRequest(int hotel_id, String hotel_name, String customer_name, String cccd, double total_price) {
        this.hotel_id = hotel_id;
        this.hotel_name = hotel_name;
        this.customer_name = customer_name;
        this.cccd = cccd;
        this.total_price = total_price;
    }
}