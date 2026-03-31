package com.example.hotelbookingapp;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.DecimalFormat;
import java.util.List;

public class HotelAdapter extends RecyclerView.Adapter<HotelAdapter.HotelViewHolder> {
    private List<hotel> hotelList;

    // 1. Thêm 2 biến để nhận tọa độ GPS thực tế của bạn
    private double currentLat;
    private double currentLng;

    // 2. Nâng cấp Constructor để nhận 3 tham số (Danh sách, Vĩ độ, Kinh độ)
    public HotelAdapter(List<hotel> hotelList, double currentLat, double currentLng) {
        this.hotelList = hotelList;
        this.currentLat = currentLat;
        this.currentLng = currentLng;
    }

    @NonNull
    @Override
    public HotelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hotel, parent, false);
        return new HotelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HotelViewHolder holder, int position) {
        hotel h = hotelList.get(position);

        // Gắn Tên khách sạn
        holder.txtName.setText(h.getName());

        // Xử lý Giá tiền
        try {
            Double price = h.getPrice_per_night();
            if (price != null && price > 0) {
                DecimalFormat priceFormatter = new DecimalFormat("#,###");
                holder.txtPriceItem.setText("Giá: " + priceFormatter.format(price) + " VNĐ");
            } else {
                holder.txtPriceItem.setText("Giá: Đang cập nhật");
            }
        } catch (Exception e) {
            holder.txtPriceItem.setText("Giá: Liên hệ");
        }

        // Xử lý Khoảng cách
        try {
            Double dist = h.getDistance();
            if (dist != null && dist > 0) {
                holder.tvDistance.setText("Cách đây: " + dist + " km");
                holder.tvDistance.setVisibility(View.VISIBLE);
            } else {
                holder.tvDistance.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            holder.tvDistance.setVisibility(View.GONE);
        }

        // Load Ảnh bằng Glide
        try {
            String url = h.getImageUrl();
            if (url != null && !url.isEmpty()) {
                url = url.replace("127.0.0.1:8000", "uncelebrated-lashandra-articulately.ngrok-free.dev");
                Glide.with(holder.itemView.getContext()).load(url).into(holder.imgHotelItem);
            }
        } catch (Exception e) {
            Log.e("DEBUG_APP", "Lỗi load ảnh: " + e.getMessage());
        }

        // Sự kiện bấm vào dòng để xem Chi tiết
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), DetailActivity.class);
            intent.putExtra("hotel_name", h.getName());
            intent.putExtra("hotel_price", String.valueOf(h.getPrice_per_night()));
            intent.putExtra("hotel_image", h.getImageUrl());
            intent.putExtra("hotel_desc", h.getDescription());
            v.getContext().startActivity(intent);
        });

        // =====================================================================
        // CHỈ ĐƯỜNG PRO CỦA GOOGLE MAPS (CHUẨN ĐƯA VÀO CV)
        // =====================================================================
        holder.btnOpenMap.setOnClickListener(v -> {
            try {
                double destinationLat = h.getLat();
                double destinationLng = h.getLng();

                // LỆNH CHUẨN ĐỂ MỞ CHẾ ĐỘ DẪN ĐƯỜNG (TURN-BY-TURN NAVIGATION)
                // mode=d nghĩa là chế độ Lái xe (Driving)
                String uri = "https://www.google.com/maps/dir/?api=1&destination=" + destinationLat + "," + destinationLng + "&travelmode=driving";

                Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                mapIntent.setPackage("com.google.android.apps.maps");

                // Mở thẳng Google Maps
                v.getContext().startActivity(mapIntent);

            } catch (android.content.ActivityNotFoundException e) {
                android.widget.Toast.makeText(v.getContext(), "Vui lòng cài đặt ứng dụng Google Maps!", android.widget.Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                android.widget.Toast.makeText(v.getContext(), "Lỗi hệ thống bản đồ", android.widget.Toast.LENGTH_SHORT).show();
                Log.e("DEBUG_MAP", "Lỗi mở map: " + e.getMessage());
            }
        });
    }

    @Override
    public int getItemCount() { return hotelList != null ? hotelList.size() : 0; }

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
            tvDistance = itemView.findViewById(R.id.tvDistance); // Đã gom gọn gàng
        }
    }
}