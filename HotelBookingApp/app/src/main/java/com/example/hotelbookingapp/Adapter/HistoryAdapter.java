package com.example.hotelbookingapp.Adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotelbookingapp.R;
import com.example.hotelbookingapp.Model.BookingModel;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private List<BookingModel> list;

    // Constructor nhận dữ liệu
    public HistoryAdapter(List<BookingModel> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Bơm layout history_item.xml vào Adapter
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BookingModel b = list.get(position);

        // Cầu chì chống Crash: Nếu b rỗng thì bỏ qua dòng này
        if (b == null) return;

        // 1. Gán Tên Khách Sạn
        holder.tvHotelName.setText(b.getHotel_name() != null ? b.getHotel_name() : "Không tên");

        // 2. Gán Tên Khách Hàng (Xử lý null)
        String customer = b.getCustomer_name();
        holder.tvCustomer.setText((customer == null || customer.isEmpty()) ? "Ẩn danh" : customer);

        // 3. Xử lý CCCD (Đặc biệt quan trọng để không hiện chữ "null")
        String cccd = b.getCccd();
        if (cccd == null || cccd.isEmpty() || cccd.equalsIgnoreCase("null")) {
            holder.tvCccd.setText("Chưa cập nhật");
            holder.tvCccd.setTextColor(Color.parseColor("#BDBDBD")); // Màu xám nhạt
        } else {
            holder.tvCccd.setText(cccd);
            holder.tvCccd.setTextColor(Color.parseColor("#333333")); // Màu đen xám chuẩn
        }

        // 4. Gán Giá Tiền (Định dạng có dấu phẩy ngăn cách hàng nghìn)
        try {
            double price = b.getTotal_price();
            holder.tvPrice.setText(String.format("%,.0f VNĐ", price));
        } catch (Exception e) {
            holder.tvPrice.setText("Liên hệ");
        }
    }

    @Override
    public int getItemCount() {
        // Chống crash lần 1: Nếu list null thì trả về 0 dòng thay vì văng app
        return (list != null) ? list.size() : 0;
    }

    // Lớp ViewHolder giữ các Widget để tái sử dụng
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvHotelName, tvCustomer, tvCccd, tvPrice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // PHẢI KHỚP ID VỚI FILE history_item.xml CỦA BẠN
            tvHotelName = itemView.findViewById(R.id.tvHotelNameHistory);
            tvCustomer = itemView.findViewById(R.id.tvCustomerNameHistory);
            tvCccd = itemView.findViewById(R.id.tvCCCDHistory);
            tvPrice = itemView.findViewById(R.id.tvTotalPriceHistory);
        }
    }
}