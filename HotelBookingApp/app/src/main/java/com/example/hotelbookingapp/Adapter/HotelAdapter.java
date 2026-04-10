package com.example.hotelbookingapp.Adapter;

import android.content.Intent;
import android.net.Uri;
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
import com.example.hotelbookingapp.Activities.DetailActivity;
import com.example.hotelbookingapp.API.MyConfig;
import com.example.hotelbookingapp.Model.Hotel;
import com.example.hotelbookingapp.R;

import java.util.List;

public class HotelAdapter extends RecyclerView.Adapter<HotelAdapter.HotelViewHolder> {
    private List<Hotel> hotelList;
    private double currentLat;
    private double currentLng;

    public HotelAdapter(List<Hotel> hotelList, double currentLat, double currentLng) {
        this.hotelList = hotelList;
        this.currentLat = currentLat;
        this.currentLng = currentLng;
    }

    @NonNull
    @Override
    public HotelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.hotel_item, parent, false);
        return new HotelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HotelViewHolder holder, int position) {
        final Hotel h = hotelList.get(position);
        if (h == null) return;

        holder.tvName.setText(h.getName());

        try {
            Double price = h.getPrice_per_night();
            if (price != null && price > 0) {
                java.text.DecimalFormat priceFormatter = new java.text.DecimalFormat("#,###");
                holder.tvPrice.setText("Giá: " + priceFormatter.format(price) + " VNĐ");
            } else {
                holder.tvPrice.setText("Giá: Đang cập nhật");
            }
        } catch (Exception e) {
            holder.tvPrice.setText("Giá: Liên hệ");
        }

        if (h.getDistance() != null && h.getDistance() > 0) {
            holder.tvDistance.setText("Đường chim bay: " + h.getDistance() + " km");
            holder.tvDistance.setVisibility(View.VISIBLE);
        } else {
            holder.tvDistance.setVisibility(View.GONE);
        }

        String url = h.getImageUrl();
        if (url != null && !url.isEmpty()) {
            // TỐI ƯU: Sử dụng MyConfig.SERVER_DOMAIN thay vì hardcode
            url = url.replace("127.0.0.1:8000", MyConfig.SERVER_DOMAIN);

            Glide.with(holder.itemView.getContext())
                    .load(url)
                    .placeholder(R.drawable.loading_img)
                    .error(R.drawable.error_img)
                    .into(holder.imgHotel);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), DetailActivity.class);
            intent.putExtra("hotel_data", h);
            v.getContext().startActivity(intent);
        });

        holder.btnDirection.setOnClickListener(v -> {
            try {
                String uri = "google.navigation:q=" + h.getLat() + "," + h.getLng();
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                mapIntent.setPackage("com.google.android.apps.maps");
                v.getContext().startActivity(mapIntent);
            } catch (Exception e) {
                Toast.makeText(v.getContext(), "Vui lòng cài đặt Google Maps", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return hotelList != null ? hotelList.size() : 0;
    }

    public class HotelViewHolder extends RecyclerView.ViewHolder {
        Button btnDirection;
        ImageView imgHotel;
        TextView tvName, tvPrice, tvDistance;

        public HotelViewHolder(@NonNull View itemView) {
            super(itemView);
            btnDirection = itemView.findViewById(R.id.btnDirection);
            imgHotel = itemView.findViewById(R.id.imgHotel);
            tvName = itemView.findViewById(R.id.tvName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvDistance = itemView.findViewById(R.id.tvDistance);
        }
    }
}