
# Hướng dẫn Cài đặt & Chạy Backend - Hotel Booking App

Dự án này bao gồm hệ thống API quản lý đặt phòng, cơ sở dữ liệu MySQL và tích hợp Webhook/Socket.IO để xử lý thanh toán Real-time. Để chạy toàn bộ hệ thống, vui lòng làm theo các bước dưới đây.

##  Yêu cầu hệ thống (Prerequisites)
* Đã cài đặt **Python 3.x**.
* Đã cài đặt **XAMPP** (để chạy MySQL).
* Đã cài đặt **Ngrok** (để test Webhook thanh toán).

---

##  Các bước khởi động Server

### Bước 1: Cấu hình biến môi trường (.env)
Bảo mật thông tin là ưu tiên hàng đầu. Khi tải source code về, bạn cần tạo file cấu hình môi trường:
1. Tạo một file mới tinh có tên là `.env` đặt ở thư mục gốc của Backend (cùng chỗ với file `main.py`).
2. Mở file `.env` lên và điền các thông số cơ bản sau (thay đổi giá trị cho khớp với máy của bạn nếu cần):
   ```env
   # Ví dụ nội dung file .env
   SECRET_KEY=a7d8f9e0b1c2d3e4f5a6b7c8d9e0f1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8
   DB_URL=mysql+mysqlconnector://root@localhost/hotel_db

### Bước 2: Khởi động Cơ sở dữ liệu (Database)
1. Mở phần mềm **XAMPP Control Panel**.
2. Nhấn **Start** ở 2 module là `Apache` và `MySQL`.
3. Mở trình duyệt web và truy cập vào đường dẫn sau để kiểm tra:
   ```text
   http://localhost/phpmyadmin
   ```
   *(Hãy đảm bảo bạn đã tạo database và import dữ liệu mẫu nếu có).*

### Bước 3: Khởi động API Server chính (FastAPI)
Server này xử lý toàn bộ logic lấy danh sách khách sạn, lịch sử đặt phòng và cập nhật trạng thái.
1. Mở Terminal (hoặc Command Prompt) tại thư mục chứa source code Backend.
2. Cài đặt các thư viện cần thiết bằng lệnh: 
   ```bash
   pip install -r requirements.txt
   ```
3. Chạy lệnh sau để khởi động Server ở cổng `8000`:
   ```bash
   python -m uvicorn main:app --host 0.0.0.0 --port 8000 --reload
   ```
   *(Cờ `--host 0.0.0.0` giúp thiết bị di động dùng chung mạng Wifi có thể kết nối được tới máy tính).*

### Bước 4: Khởi động Socket Server (Lắng nghe thanh toán)
Server này chịu trách nhiệm đẩy thông báo "Ting ting" về điện thoại ngay lập tức khi khách hàng quét mã QR xong.
1. Mở một **tab Terminal mới** (không tắt tab ở Bước 3).
2. Chạy lệnh sau:
   ```bash
   python sever.py
   ```

### Bước 5: Mở cổng Ngrok cho Webhook
Vì Webhook của ngân hàng cần một đường link public (có Internet) để gửi thông báo biến động số dư về máy tính local, chúng ta cần dùng Ngrok.
1. Mở một **tab Terminal mới** (hoặc mở tool Ngrok).
2. Chạy lệnh sau để public cổng `8000`:
   ```bash
   ngrok http 8000
   ```
3. **Quan trọng:** Ngrok sẽ cấp cho bạn một đường link (Ví dụ: `[https://xxxx-xxx.ngrok-free.app](https://xxxx-xxx.ngrok-free.app)`). Hãy copy đường link này và dán vào phần cấu hình Webhook (SePay/Casso/VietQR...) để hệ thống nhận được thông báo tiền về.

---

##  Hướng dẫn Test trên App Android
Khi chạy thử App trên máy thật hoặc máy ảo, người test cần chú ý:
1. Mở Command Prompt trên máy tính, gõ `ipconfig` (với Windows) hoặc `ifconfig` (với Mac/Linux) để lấy địa chỉ **IPv4** (Ví dụ: `192.168.100.116`).
2. Mở source code Android Studio, tìm đến các file cấu hình API (`RetrofitClient.java` và `PaymentActivity.java`).
3. Thay thế các IP cũ thành IP hiện tại của máy tính để App có thể gọi đúng xuống Server.

***
