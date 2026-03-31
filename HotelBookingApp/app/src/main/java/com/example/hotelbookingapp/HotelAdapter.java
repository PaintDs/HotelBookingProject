package com.example.hotelbookingapp;

import android.content.Intent;
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

import java.text.DecimalFormat;
import java.util.List;

public class HotelAdapter extends RecyclerView.Adapter<HotelAdapter.HotelViewHolder> {
    private List<hotel> hotelList;

    public HotelAdapter(List<hotel> hotelList) { this.hotelList = hotelList; }

    @NonNull
    @Override
    public HotelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hotel, parent, false);
        return new HotelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HotelViewHolder holder, int position) {
        hotel h = hotelList.get(position);

        // 1. Gắn Tên khách sạn
        holder.txtName.setText(h.getName());

        // 2. Xử lý Giá tiền (Gọn gàng, an toàn, không bị lặp code)
        try {
            // Dùng Double (chữ D viết hoa) để an toàn nếu server trả về null
            Double price = h.getPrice_per_night();

            if (price != null && price > 0) {
                java.text.DecimalFormat priceFormatter = new java.text.DecimalFormat("#,###");
                holder.txtPriceItem.setText("Giá: " + priceFormatter.format(price) + " VNĐ");
            } else {
                holder.txtPriceItem.setText("Giá: Đang cập nhật");
            }
        } catch (Exception e) {
            holder.txtPriceItem.setText("Giá: Liên hệ");
            Log.e("DEBUG_APP", "Lỗi hiển thị giá: " + e.getMessage());
        }

        // 3. Xử lý Khoảng cách (Tránh lỗi NullPointerException)
        try {
            Double dist = h.getDistance();

            if (dist != null && dist > 0) {
                holder.tvDistance.setText("Cách đây: " + dist + " km");
                holder.tvDistance.setVisibility(View.VISIBLE); // Hiện nếu có khoảng cách
            } else {
                holder.tvDistance.setText("");
                holder.tvDistance.setVisibility(View.GONE); // Ẩn hoàn toàn nếu mất GPS
            }
        } catch (Exception e) {
            holder.tvDistance.setVisibility(View.GONE);
        }

        // 4. Load Ảnh bằng Glide
        try {
            String url = h.getImageUrl();
            if (url != null && !url.isEmpty()) {
                url = url.replace("127.0.0.1:8000", "uncelebrated-lashandra-articulately.ngrok-free.dev");
                Glide.with(holder.itemView.getContext()).load(url).into(holder.imgHotelItem);
            }
        } catch (Exception e) {
            Log.e("DEBUG_APP", "Lỗi load ảnh: " + e.getMessage());
        }

        // 5. Sự kiện bấm vào dòng để xem Chi tiết
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), DetailActivity.class);
            intent.putExtra("hotel_name", h.getName());
            intent.putExtra("hotel_price", String.valueOf(h.getPrice_per_night()));
            intent.putExtra("hotel_image", h.getImageUrl());
            intent.putExtra("hotel_desc", h.getDescription());
            v.getContext().startActivity(intent);
        });

        // 6. Sự kiện bấm nút Chỉ đường
        holder.btnOpenMap.setOnClickListener(v -> {
            try {
                Double destinationLat = h.getLat();
                Double destinationLng = h.getLng();

                // Kiểm tra chắc chắn có tọa độ mới mở Maps
                if (destinationLat != null && destinationLng != null) {
                    String uri = "google.navigation:q=" + destinationLat + "," + destinationLng;
                    android.net.Uri gmmIntentUri = android.net.Uri.parse(uri);
                    android.content.Intent mapIntent = new android.content.Intent(android.content.Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");

                    if (mapIntent.resolveActivity(v.getContext().getPackageManager()) != null) {
                        v.getContext().startActivity(mapIntent);
                    } else {
                        // Cập nhật link Maps trên trình duyệt chuẩn hơn
                        String fallbackUrl = "https://www.google.com/maps/dir/?api=1&destination=" + destinationLat + "," + destinationLng;
                        android.content.Intent browserIntent = new android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(fallbackUrl));
                        v.getContext().startActivity(browserIntent);
                    }
                } else {
                    android.widget.Toast.makeText(v.getContext(), "Khách sạn này chưa cập nhật tọa độ!", android.widget.Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Log.e("DEBUG_APP", "Lỗi mở bản đồ: " + e.getMessage());
            }
        });
    }



    @Override
    public int getItemCount() { return hotelList != null ? hotelList.size() : 0; }

    // Tìm đến cuối file HotelAdapter.java
    public class HotelViewHolder extends RecyclerView.ViewHolder {
        Button btnOpenMap;
        ImageView imgHotelItem;
        TextView txtName, txtPriceItem;
        TextView tvDistance; // 1. Khai báo (Đã có)

        public HotelViewHolder(@NonNull View itemView) {
            super(itemView);
            btnOpenMap = itemView.findViewById(R.id.btnOpenMap);
            imgHotelItem = itemView.findViewById(R.id.imgHotelItem);
            txtName = itemView.findViewById(R.id.txtHotelNameItem);
            txtPriceItem = itemView.findViewById(R.id.txtPriceItem);

            // 2. BẠN ĐANG THIẾU DÒNG QUAN TRỌNG NÀY:
            tvDistance = itemView.findViewById(R.id.tvDistance);
        }
    }
}