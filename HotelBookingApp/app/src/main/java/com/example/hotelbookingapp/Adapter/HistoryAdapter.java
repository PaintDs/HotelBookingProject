package com.example.hotelbookingapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotelbookingapp.Activities.PaymentActivity;
import com.example.hotelbookingapp.R;
import com.example.hotelbookingapp.Model.BookingModel;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    // 1. THÊM BIẾN CONTEXT ĐỂ CHUYỂN TRANG
    private Context context;
    private List<BookingModel> list;

    // 2. SỬA LẠI CONSTRUCTOR ĐỂ NHẬN ĐỦ 2 THAM SỐ (Fix lỗi báo đỏ)
    public HistoryAdapter(Context context, List<BookingModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BookingModel b = list.get(position);

        if (b == null) return;

        holder.tvHotelName.setText(b.getHotel_name() != null ? b.getHotel_name() : "Không tên");

        String customer = b.getCustomer_name();
        holder.tvCustomer.setText((customer == null || customer.isEmpty()) ? "Ẩn danh" : customer);

        String cccd = b.getCccd();
        if (cccd == null || cccd.isEmpty() || cccd.equalsIgnoreCase("null")) {
            holder.tvCccd.setText("Chưa cập nhật");
            holder.tvCccd.setTextColor(Color.parseColor("#BDBDBD"));
        } else {
            holder.tvCccd.setText(cccd);
            holder.tvCccd.setTextColor(Color.parseColor("#333333"));
        }

        try {
            double price = b.getTotal_price();
            holder.tvPrice.setText(String.format("%,.0f VNĐ", price));
        } catch (Exception e) {
            holder.tvPrice.setText("Liên hệ");
        }

        // ==========================================
        // 3. LOGIC XỬ LÝ TRẠNG THÁI THANH TOÁN
        // ==========================================
        // LƯU Ý: Phải đảm bảo file history_item.xml của bạn đã có TextView tvStatusHistory và Button btnPayNowHistory

        String status = b.getStatus();

        if (status != null && status.equalsIgnoreCase("Đã thanh toán")) {
            holder.tvStatus.setText("Đã thanh toán");
            holder.tvStatus.setTextColor(Color.parseColor("#4CAF50")); // Màu xanh lá
            holder.btnPayNow.setVisibility(View.GONE); // Đã trả tiền thì ẩn nút đi
        } else {
            holder.tvStatus.setText("Chưa thanh toán");
            holder.tvStatus.setTextColor(Color.parseColor("#F44336")); // Màu đỏ
            holder.btnPayNow.setVisibility(View.VISIBLE); // Hiện nút để khách bấm

            // Xử lý khi khách bấm nút "Thanh toán ngay"
            holder.btnPayNow.setOnClickListener(v -> {
                Intent intent = new Intent(context, PaymentActivity.class);
                intent.putExtra("bookingId", b.getId());
                intent.putExtra("amount", (int) b.getTotal_price());
                context.startActivity(intent); // Dùng context để chuyển trang
            });
        }

    }

    @Override
    public int getItemCount() {
        return (list != null) ? list.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvHotelName, tvCustomer, tvCccd, tvPrice;

        // Khai báo sẵn widget cho trạng thái (bỏ comment khi file XML đã có)
         TextView tvStatus;
         Button btnPayNow;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHotelName = itemView.findViewById(R.id.tvHotelNameHistory);
            tvCustomer = itemView.findViewById(R.id.tvCustomerNameHistory);
            tvCccd = itemView.findViewById(R.id.tvCCCDHistory);
            tvPrice = itemView.findViewById(R.id.tvTotalPriceHistory);

            // Ánh xạ id cho trạng thái (bỏ comment khi file XML đã có)
             tvStatus = itemView.findViewById(R.id.tvStatusHistory);
             btnPayNow = itemView.findViewById(R.id.btnPayNowHistory);
        }
    }
}