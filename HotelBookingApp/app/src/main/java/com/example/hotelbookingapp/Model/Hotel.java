package com.example.hotelbookingapp.Model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Hotel implements Serializable {

    // 0. CHỐT CHẶN PHIÊN BẢN (QUAN TRỌNG NHẤT ĐỂ FIX LỖI CACHE)
    // Mỗi khi thay đổi Model, hãy đổi số này (ví dụ từ 1L lên 2L) để App buộc phải load lại dữ liệu mới
    private static final long serialVersionUID = 1L;

    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("address")
    private String address;

    @SerializedName("description")
    private String description;

    @SerializedName("image_url")
    private String image_url;

    @SerializedName("price_per_night")
    private Double price_per_night;

    @SerializedName("distance")
    private Double distance;

    @SerializedName("lat")
    private Double lat;

    @SerializedName("lng")
    private Double lng;

    // --- GETTERS ---
    public int getId() { return id; }

    public String getName() {
        return name != null ? name : "Khách sạn chưa cập nhật tên";
    }

    public String getAddress() {
        return address != null ? address : "";
    }

    // Logic này sẽ ưu tiên dữ liệu thật, nếu NULL mới hiện văn mẫu
    public String getDescription() {
        if (description == null || description.trim().isEmpty() || description.equalsIgnoreCase("null")) {
            return "Đang cập nhật mô tả...";
        }
        return description;
    }

    public String getImageUrl() {
        return image_url != null ? image_url : "";
    }

    // Tránh lỗi NullPointerException khi tính toán số thực
    public Double getPrice_per_night() { return price_per_night != null ? price_per_night : 0.0; }
    public Double getDistance() { return distance != null ? distance : 0.0; }
    public Double getLat() { return lat != null ? lat : 0.0; }
    public Double getLng() { return lng != null ? lng : 0.0; }

    // --- SETTERS (Giữ nguyên) ---
    public void setPrice_per_night(Double price_per_night) { this.price_per_night = price_per_night; }
    public void setDistance(Double distance) { this.distance = distance; }
    public void setLat(Double lat) { this.lat = lat; }
    public void setLng(Double lng) { this.lng = lng; }
}