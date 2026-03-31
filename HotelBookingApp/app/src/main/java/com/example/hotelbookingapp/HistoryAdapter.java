package com.example.hotelbookingapp; // Kiểm tra lại tên package của bạn nhé

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    // 1. Kiểm tra xem bạn đã tạo class BookingModel chưa, nếu chưa hãy đổi tên này cho khớp
    private List<BookingModel> list;

    public HistoryAdapter(List<BookingModel> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BookingModel b = list.get(position);

        // Bạn nhớ kiểm tra các hàm get...() trong file BookingModel nhé
        holder.txtName.setText(b.getHotel_name());
        holder.txtCustomer.setText("Khách hàng: " + b.getCustomer_name());
        holder.txtCccd.setText("CCCD: " + b.getCccd());
        holder.txtPrice.setText("Giá: " + b.getTotal_price() + " VNĐ");
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    // ĐÂY LÀ PHẦN BẠN ĐANG THIẾU KHIẾN CODE BÁO ĐỎ
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtName, txtCustomer, txtCccd, txtPrice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtHistoryHotelName);
            txtCustomer = itemView.findViewById(R.id.txtHistoryCustomerName);
            txtCccd = itemView.findViewById(R.id.txtHistoryCccd);
            txtPrice = itemView.findViewById(R.id.txtHistoryPrice);
        }
    }
}