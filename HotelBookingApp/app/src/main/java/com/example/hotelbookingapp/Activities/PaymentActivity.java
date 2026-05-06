package com.example.hotelbookingapp.Activities;

import io.socket.client.IO;
import io.socket.client.Socket;
import java.net.URISyntaxException;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.hotelbookingapp.R;

public class PaymentActivity extends AppCompatActivity {

    ImageView imgQrCode;
    TextView tvAmount, tvContent;
    Button btnConfirmPayment, btnCancelPayment;
    private Socket mSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        // 1. Ánh xạ các View trên màn hình
        imgQrCode = findViewById(R.id.imgQrCode);
        tvAmount = findViewById(R.id.tvAmount);
        tvContent = findViewById(R.id.tvContent);
        btnConfirmPayment = findViewById(R.id.btnConfirmPayment);
        btnCancelPayment = findViewById(R.id.btnCancelPayment);

        // ==========================================
        // 2. LOGIC NHẬN DỮ LIỆU ĐÃ FIX LỖI "FINAL"
        // ==========================================
        Intent intent = getIntent();

        // Lấy ID dứt khoát 1 lần bằng toán tử 3 ngôi (Cách này giúp biến trở thành effectively final)
        final int bookingId = intent.hasExtra("BOOKING_ID") ?
                intent.getIntExtra("BOOKING_ID", 0) :
                intent.getIntExtra("bookingId", 0);

        // Lấy Tiền
        int tempAmount = 0;
        if (intent.hasExtra("AMOUNT_STR")) {
            String amountStr = intent.getStringExtra("AMOUNT_STR");
            if (amountStr != null) {
                try {
                    tempAmount = (int) Double.parseDouble(amountStr);
                } catch (Exception ignored) {
                    // Bỏ qua lỗi, tempAmount mặc định vẫn là 0 (Fix luôn cái cảnh báo vàng dòng 57)
                }
            }
        } else if (intent.hasExtra("amount")) {
            tempAmount = intent.getIntExtra("amount", 0);
        }
        final int amount = tempAmount; // Chốt biến amount thành final

        // 3. Hiển thị thông tin lên màn hình để khách kiểm tra lại
        tvAmount.setText("Số tiền: " + String.format("%,d", amount) + " VNĐ");

        // Tạo nội dung chuyển khoản không dấu
        String noiDungChuyenKhoan = "THANH TOAN BOOKING " + bookingId;
        tvContent.setText("Nội dung: " + noiDungChuyenKhoan);

        // ==========================================
        // 4. TẠO LINK ẢNH VIETQR VÀ DÙNG GLIDE ĐỂ TẢI
        // ==========================================
        String maNganHang = "970422"; // MBBank
        String soTaiKhoan = "1430197425";

        String qrUrl = "https://img.vietqr.io/image/" + maNganHang + "-" + soTaiKhoan + "-compact2.png"
                + "?amount=" + amount
                + "&addInfo=" + noiDungChuyenKhoan
                + "&accountName=NGUYEN VAN KHAI";

        // Fix URL nếu có khoảng trắng dư thừa
        qrUrl = qrUrl.replace(" ", "%20");

        Glide.with(this)
                .load(qrUrl)
                .into(imgQrCode);

        // ==========================================
        // 5. TÍCH HỢP SOCKET.IO ĐỂ LẮNG NGHE THANH TOÁN
        // ==========================================
        try {
            mSocket = IO.socket("http://192.168.100.116:5000");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        if (mSocket != null) {
            mSocket.connect();

            // QUAN TRỌNG: Chỉ gửi lệnh khi Socket báo CONNECT thành công
            mSocket.on(Socket.EVENT_CONNECT, args -> {
                runOnUiThread(() -> {
                    // Do bookingId đã được chốt final ở trên nên ném vào đây vô tư không báo đỏ nữa
                    mSocket.emit("join_payment_room", String.valueOf(bookingId));
                    android.util.Log.d("SOCKET_DEBUG", "Đã kết nối và hóng đơn: " + bookingId);
                });
            });

            // Lắng nghe tín hiệu "thanh toán thành công" từ Server
            mSocket.on("payment_success", args -> {

                // 1. CHẠY NGẦM ĐỂ BÁO DATABASE CẬP NHẬT TRẠNG THÁI "ĐÃ THANH TOÁN"
                new Thread(() -> {
                    try {
                        // Gọi trực tiếp lên cổng 8000 của FastAPI
                        java.net.URL url = new java.net.URL("http://192.168.100.116:8000/bookings/update-status?booking_id=" + bookingId);
                        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("POST");
                        conn.getResponseCode(); // Lệnh này sẽ kích hoạt API đổi trạng thái trong DB
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // 2. KHI DATABASE CẬP NHẬT XONG RỒI MỚI CHUYỂN MÀN HÌNH
                    runOnUiThread(() -> {
                        Toast.makeText(PaymentActivity.this, "🎉 Thanh toán thành công!", Toast.LENGTH_LONG).show();

                        // Tự động chuyển về màn hình Lịch sử
                        Intent historyIntent = new Intent(PaymentActivity.this, HistoryActivity.class);
                        historyIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(historyIntent);
                        finish();
                    });
                }).start();

            });
        }

        // ==========================================
        // 6. XỬ LÝ CÁC NÚT BẤM
        // ==========================================
        btnConfirmPayment.setOnClickListener(v -> {
            Toast.makeText(this, "Hệ thống đang chờ xác nhận từ ngân hàng...", Toast.LENGTH_SHORT).show();
        });

        btnCancelPayment.setOnClickListener(v -> {
            finish();
        });
    }

    // ==========================================
    // 7. NGẮT KẾT NỐI SOCKET KHI THOÁT MÀN HÌNH
    // ==========================================
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSocket != null) {
            mSocket.disconnect();
        }
    }
}