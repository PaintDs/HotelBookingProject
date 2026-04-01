package com.example.hotelbookingapp.Model;

import com.google.gson.annotations.SerializedName;

// =====================================================================
// LỚP MÔ HÌNH KHÁCH SẠN (CORE ENTITY MODEL)
// Nhiệm vụ: Định nghĩa cấu trúc dữ liệu của một Khách sạn.
// Đây là lớp trung tâm kết nối giữa Cơ sở dữ liệu MySQL (qua Python) và Giao diện Android.
// =====================================================================
public class Hotel {

    // 1. Thông tin định danh cơ bản
    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("address")
    private String address;

    @SerializedName("description")
    private String description;

    // 2. Thông tin hình ảnh (Dùng để Glide tải ảnh từ URL)
    @SerializedName("image_url")
    private String image_url;

    // =====================================================================
    // 3. THÔNG TIN ĐỊNH LƯỢNG & TỌA ĐỘ (Dùng kiểu dữ liệu Wrapper 'Double')
    // Tại sao dùng 'Double' (viết hoa) thay vì 'double' (viết thường)?
    // - double: Luôn mặc định là 0.0 nếu Server trả về null (Gây hiểu nhầm là khách sạn miễn phí).
    // - Double: Có thể nhận giá trị 'null'. Giúp Adapter nhận biết dữ liệu trống để ẩn View (Visibility.GONE),
    //   tạo ra trải nghiệm người dùng (UX) chuyên nghiệp, không hiện số 0 vô lý.
    // =====================================================================
    @SerializedName("price_per_night")
    private Double price_per_night;

    @SerializedName("distance")
    private Double distance; // Khoảng cách tính toán từ Backend

    // Tọa độ thực tế phục vụ cho chức năng "Chỉ đường Google Maps"
    @SerializedName("lat")
    private Double lat;

    @SerializedName("lng")
    private Double lng;

    // =====================================================================
    // 4. HÀM TRUY XUẤT DỮ LIỆU (GETTERS)
    // Nhiệm vụ: Cung cấp dữ liệu cho Adapter để vẽ lên màn hình (UI Rendering)
    // và cho Intent để truyền dữ liệu giữa các màn hình (Data Navigation).
    // =====================================================================
    public int getId() { return id; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public String getDescription() { return description; }
    public String getImageUrl() { return image_url; }

    public Double getPrice_per_night() { return price_per_night; }
    public Double getDistance() { return distance; }
    public Double getLat() { return lat; }
    public Double getLng() { return lng; }

    // --- SETTERS: Cập nhật dữ liệu khi cần thiết ---
    public void setPrice_per_night(Double price_per_night) { this.price_per_night = price_per_night; }
    public void setDistance(Double distance) { this.distance = distance; }
    public void setLat(Double lat) { this.lat = lat; }
    public void setLng(Double lng) { this.lng = lng; }
}