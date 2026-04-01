package com.example.hotelbookingapp.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotelbookingapp.R;

import java.util.List;

import com.example.hotelbookingapp.Model.BookingModel;

// =====================================================================
// LỚP ADAPTER: "Cây cầu" kết nối giữa Dữ liệu (List) và Giao diện (RecyclerView)
// Nhiệm vụ: Nhận một mảng dữ liệu BookingModel và vẽ chúng thành các dòng (item) hiển thị trên màn hình
// =====================================================================
public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    // Nguồn dữ liệu chính của Adapter (Danh sách các đơn đặt phòng lấy từ API)
    private List<BookingModel> list;

    // =====================================================================
    // HÀM KHỞI TẠO (CONSTRUCTOR)
    // Nhiệm vụ: Nhận dữ liệu từ HistoryActivity truyền sang khi bắt đầu khởi tạo danh sách
    // =====================================================================
    public HistoryAdapter(List<BookingModel> list) {
        this.list = list;
    }

    // =====================================================================
    // BƯỚC 1: TẠO KHUÔN (CREATE VIEW HOLDER)
    // Nhiệm vụ: Giống như "thợ xây", hàm này đọc file giao diện XML (item_history.xml)
    // và "bơm" (inflate) nó lên thành một View vật lý, sau đó giao cho ViewHolder quản lý.
    // Lưu ý: Hàm này chỉ chạy vài lần đủ để lấp đầy màn hình, sau đó các View sẽ được tái sử dụng khi cuộn.
    // =====================================================================
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Biến file XML thành một Object View trong Java
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view); // Trả về một "cái khay" chứa các giao diện vừa tạo
    }

    // =====================================================================
    // BƯỚC 2: ĐỔ DỮ LIỆU (BIND VIEW HOLDER)
    // Nhiệm vụ: Giống như "thợ trang trí", hàm này lấy dữ liệu tại vị trí (position) hiện tại
    // và gắn (set) vào các TextView tương ứng trên giao diện.
    // Đặc điểm: Hàm này chạy liên tục mỗi khi người dùng cuộn danh sách (tái sử dụng khuôn cũ, chỉ thay ruột mới).
    // =====================================================================
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Lấy ra phần tử (đơn đặt phòng) đang xét trong danh sách
        BookingModel b = list.get(position);

        // Đổ dữ liệu từ Model vào các Widget (TextView) trên màn hình
        holder.txtName.setText(b.getHotel_name());
        holder.txtCustomer.setText("Khách hàng: " + b.getCustomer_name());
        holder.txtCccd.setText("CCCD: " + b.getCccd());
        holder.txtPrice.setText("Giá: " + b.getTotal_price() + " VNĐ");
    }

    // =====================================================================
    // BƯỚC 3: ĐẾM SỐ LƯỢNG (GET ITEM COUNT)
    // Nhiệm vụ: Báo cáo cho Android biết danh sách này có tổng cộng bao nhiêu dòng
    // =====================================================================
    @Override
    public int getItemCount() {
        // Toán tử 3 ngôi (Ternary Operator) giúp chống lỗi NullPointerException (App bị văng)
        // Nếu list có dữ liệu thì trả về kích thước list, nếu list rỗng/null thì trả về 0
        return list != null ? list.size() : 0;
    }

    // =====================================================================
    // LỚP NỘI TẠI: BỘ QUẢN LÝ GIAO DIỆN (VIEW HOLDER)
    // Nhiệm vụ: Tránh việc gọi hàm findViewById() lặp đi lặp lại hàng nghìn lần khi người dùng cuộn app.
    // Nó "nhớ" (cache) các thành phần giao diện ngay từ lần đầu tiên để tái sử dụng, giúp App chạy mượt (60fps).
    // =====================================================================
    public class ViewHolder extends RecyclerView.ViewHolder {
        // Khai báo các thành phần UI có trong 1 dòng (item)
        TextView txtName, txtCustomer, txtCccd, txtPrice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Chỉ ánh xạ (tìm ID) duy nhất một lần khi khuôn (ViewHolder) được tạo ra
            txtName = itemView.findViewById(R.id.txtHistoryHotelName);
            txtCustomer = itemView.findViewById(R.id.txtHistoryCustomerName);
            txtCccd = itemView.findViewById(R.id.txtHistoryCccd);
            txtPrice = itemView.findViewById(R.id.txtHistoryPrice);
        }
    }
}