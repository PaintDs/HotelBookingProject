package com.example.hotelbookingapp.API;

// =====================================================================
// LỚP CẤU HÌNH (CONFIGURATION CLASS): Quản lý hằng số toàn cục
// Nhiệm vụ: Tập trung toàn bộ các thông số môi trường (Environment Variables)
// vào một chỗ duy nhất. Giúp quá trình bảo trì, chuyển đổi giữa môi trường
// Lập trình (Dev/Local) và Môi trường Thực tế (Production) diễn ra an toàn, nhanh chóng.
// =====================================================================
public class MyConfig {

    // =====================================================================
    // 1. BASE URL (ĐIỂM NÚT GIAO TIẾP MẠNG CHÍNH)
    // Nhiệm vụ: Cung cấp địa chỉ gốc cho thư viện Retrofit hoạt động.
    // Lưu ý kỹ thuật: Thư viện Retrofit bắt buộc chuỗi Base URL phải kết thúc bằng dấu gạch chéo (/)
    // Nếu thiếu dấu (/) này, App sẽ Crash ngay lúc khởi tạo (IllegalArgumentException).
    // =====================================================================
    public static final String BASE_URL = "https://uncelebrated-lashandra-articulately.ngrok-free.dev/";

    // =====================================================================
    // 2. SERVER DOMAIN (XỬ LÝ ĐƯỜNG DẪN TÀI NGUYÊN TĨNH - ẢNH)
    // Nhiệm vụ: Dùng để Regex (Tìm và thay thế) các đường dẫn ảnh bị lưu cứng là '127.0.0.1'
    // dưới Database MySQL thành Domain thực tế có thể truy cập được từ Internet.
    // Giúp App Mobile có thể tải được hình ảnh qua 4G thay vì chỉ chạy được mạng LAN.
    // =====================================================================
    public static final String SERVER_DOMAIN = "uncelebrated-lashandra-articulately.ngrok-free.dev";
}