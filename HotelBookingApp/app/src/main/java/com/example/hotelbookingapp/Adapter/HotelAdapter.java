package com.example.hotelbookingapp.Adapter;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.hotelbookingapp.Activities.DetailActivity;
import com.example.hotelbookingapp.R;

import java.text.DecimalFormat;
import java.util.List;

import com.example.hotelbookingapp.Model.Hotel;

// =====================================================================
// LỚP ADAPTER CHÍNH: Quản lý và render danh sách Khách sạn
// Nhiệm vụ: Xử lý logic phức tạp trên từng dòng (item), bao gồm hiển thị dữ liệu,
// tải ảnh bất đồng bộ, định dạng tiền tệ và xử lý các sự kiện click (Chi tiết, Bản đồ).
// =====================================================================
public class HotelAdapter extends RecyclerView.Adapter<HotelAdapter.HotelViewHolder> {
    private List<Hotel> hotelList; // Nguồn dữ liệu từ Server

    // Nhận tọa độ GPS thực tế của người dùng để truyền cho Google Maps nếu cần
    private double currentLat;
    private double currentLng;

    // =====================================================================
    // HÀM KHỞI TẠO (CONSTRUCTOR): Nhận cả Data và Context (Tọa độ)
    // =====================================================================
    public HotelAdapter(List<Hotel> hotelList, double currentLat, double currentLng) {
        this.hotelList = hotelList;
        this.currentLat = currentLat;
        this.currentLng = currentLng;
    }

    // =====================================================================
    // BƯỚC 1: TẠO KHUÔN (Chỉ chạy vài lần để lấp đầy màn hình)
    // =====================================================================
    @NonNull
    @Override
    public HotelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hotel, parent, false);
        return new HotelViewHolder(view);
    }

    // =====================================================================
    // BƯỚC 2: ĐỔ DỮ LIỆU & XỬ LÝ BUSINESS LOGIC CHO TỪNG DÒNG
    // =====================================================================
    @Override
    public void onBindViewHolder(@NonNull HotelViewHolder holder, int position) {
        Hotel h = hotelList.get(position);

        // 1. Tên khách sạn
        holder.txtName.setText(h.getName());

        // 2. Format Tiền tệ (UX Optimization)
        // Bọc try-catch đề phòng lỗi dữ liệu từ Server khiến app bị crash
        try {
            Double price = h.getPrice_per_night();
            if (price != null && price > 0) {
                // Định dạng số chuẩn Việt Nam (VD: 1000000 -> 1,000,000)
                DecimalFormat priceFormatter = new DecimalFormat("#,###");
                holder.txtPriceItem.setText("Giá: " + priceFormatter.format(price) + " VNĐ");
            } else {
                holder.txtPriceItem.setText("Giá: Đang cập nhật"); // Fallback UX
            }
        } catch (Exception e) {
            holder.txtPriceItem.setText("Giá: Liên hệ");
        }

        // 3. Xử lý logic Khoảng cách (Dynamic UI)
        try {
            Double dist = h.getDistance();
            if (dist != null && dist > 0) {
                // Đổi thành "Đường chim bay" như đã thống nhất để tối ưu chi phí API bản đồ
                holder.tvDistance.setText("Đường chim bay: " + dist + " km");
                holder.tvDistance.setVisibility(View.VISIBLE); // Hiện View
            } else {
                // Ẩn hoàn toàn View khoảng cách nếu người dùng tắt GPS (đỡ bị trống layout)
                holder.tvDistance.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            holder.tvDistance.setVisibility(View.GONE);
        }

        // 4. Tải hình ảnh bất đồng bộ (Async Image Loading)
        // Sử dụng Glide để tự động cache ảnh, tiết kiệm băng thông mạng
        try {
            String url = h.getImageUrl();
            if (url != null && !url.isEmpty()) {
                // Mẹo xử lý môi trường Dev: Đổi localhost thành Ngrok URL để Mobile có thể tải được ảnh
                url = url.replace("127.0.0.1:8000", "uncelebrated-lashandra-articulately.ngrok-free.dev");
                Glide.with(holder.itemView.getContext()).load(url).into(holder.imgHotelItem);
            }
        } catch (Exception e) {
            Log.e("DEBUG_APP", "Lỗi load ảnh: " + e.getMessage());
        }

        // =====================================================================
        // SỰ KIỆN 1: BẤM VÀO ITEM ĐỂ XEM CHI TIẾT
        // Nhiệm vụ: Đóng gói dữ liệu hiển thị (Intent Extras) và chuyển sang DetailActivity
        // =====================================================================
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), DetailActivity.class);
            // Gửi ID để truyền lên API đặt phòng
            intent.putExtra("hotel_id", h.getId()); // (Lưu ý: Bổ sung dòng này nếu Model có hàm getId())
            intent.putExtra("hotel_name", h.getName());
            intent.putExtra("hotel_price", String.valueOf(h.getPrice_per_night()));
            intent.putExtra("hotel_image", h.getImageUrl());
            intent.putExtra("hotel_desc", h.getDescription());
            v.getContext().startActivity(intent);
        });

        // =====================================================================
        // SỰ KIỆN 2: TÍCH HỢP GOOGLE MAPS (Tính năng đinh của CV)
        // Nhiệm vụ: Chuyển hướng người dùng sang Google Maps với luồng trải nghiệm trơn tru
        // =====================================================================
        holder.btnOpenMap.setOnClickListener(v -> {
            try {
                // Trích xuất tọa độ điểm đến (Khách sạn) từ Database
                double destinationLat = h.getLat();
                double destinationLng = h.getLng();

                // LỆNH CHUẨN ĐỂ MỞ CHẾ ĐỘ DẪN ĐƯỜNG (TURN-BY-TURN NAVIGATION)
                // Sử dụng URI chuẩn của Google để bắt hệ thống mở app Maps và vẽ đường
                String uri = "https://www.google.com/maps/dir/?api=1&destination=" + destinationLat + "," + destinationLng + "&travelmode=driving";

                // Implicit Intent (Intent không tường minh): Giao việc cho hệ điều hành tự tìm App xử lý
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                mapIntent.setPackage("com.google.android.apps.maps"); // Ép hệ thống dùng đúng Google Maps, không mở bừa app khác

                // Thực thi chuyển app
                v.getContext().startActivity(mapIntent);

            } catch (android.content.ActivityNotFoundException e) {
                // Xử lý ngoại lệ (Exception Handling): Rất quan trọng!
                // Xảy ra khi máy người dùng không cài Google Maps (VD: Một số máy nội địa Trung Quốc)
                android.widget.Toast.makeText(v.getContext(), "Vui lòng cài đặt ứng dụng Google Maps!", android.widget.Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                // Bắt mọi lỗi vặt khác để đảm bảo App không bao giờ bị Crash
                android.widget.Toast.makeText(v.getContext(), "Lỗi hệ thống bản đồ", android.widget.Toast.LENGTH_SHORT).show();
                Log.e("DEBUG_MAP", "Lỗi mở map: " + e.getMessage());
            }
        });
    }

    @Override
    public int getItemCount() {
        return hotelList != null ? hotelList.size() : 0;
    }

    // =====================================================================
    // LỚP VIEW HOLDER: Cache giao diện
    // =====================================================================
    public class HotelViewHolder extends RecyclerView.ViewHolder {
        Button btnOpenMap;
        ImageView imgHotelItem;
        TextView txtName, txtPriceItem, tvDistance;

        public HotelViewHolder(@NonNull View itemView) {
            super(itemView);
            btnOpenMap = itemView.findViewById(R.id.btnOpenMap);
            imgHotelItem = itemView.findViewById(R.id.imgHotelItem);
            txtName = itemView.findViewById(R.id.txtHotelNameItem);
            txtPriceItem = itemView.findViewById(R.id.txtPriceItem);
            tvDistance = itemView.findViewById(R.id.tvDistance);
        }
    }
}