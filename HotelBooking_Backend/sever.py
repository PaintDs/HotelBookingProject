from flask import Flask, request, jsonify
from flask_socketio import SocketIO, emit, join_room

app = Flask(__name__)
# Khởi tạo SocketIO
socketio = SocketIO(app, cors_allowed_origins="*")

# 1. Android App kết nối vào và chui vào "phòng chờ" của đúng đơn hàng đó
# Đảm bảo tên sự kiện khớp với bên Java
@socketio.on('join_payment_room') 
def handle_join(data):
    # In ra toàn bộ dữ liệu nhận được để kiểm tra
    print(f"===> NHẬN ĐƯỢC LỆNH JOIN: {data}")
    
    # Nếu data là một dict (JSONObject)
    if isinstance(data, dict):
        booking_id = str(data.get('booking_id'))
    else:
        booking_id = str(data)
        
    join_room(booking_id)
    print(f"[SOCKET] App đang hóng thanh toán cho đơn hàng: {booking_id}")

# 2. MOCK WEBHOOK (Bạn sẽ dùng Postman để gọi API này, đóng vai Ngân hàng)
@app.route('/api/mock-bank-webhook', methods=['POST'])
def mock_bank_webhook():
    data = request.json
    booking_id = str(data.get('booking_id'))
    amount = data.get('amount')
    
    print(f"[WEBHOOK] Ngân hàng báo có tiền! Đơn {booking_id} nhận {amount}đ")
    
    # Tại đây ở dự án thật bạn sẽ Update Database (Trạng thái: Đã thanh toán)
    
    # Bắn tín hiệu Real-time báo thành công xuống đúng cái App đang mở đơn hàng đó
    socketio.emit('payment_success', {
        'status': 'SUCCESS', 
        'msg': 'Thanh toán thành công!'
    }, room=booking_id)
    
    return jsonify({'msg': 'Đã nhận webhook và báo cho Mobile'}), 200

if __name__ == '__main__':
    # Chạy server ở IP local để máy ảo Android có thể gọi được
    socketio.run(app, host='0.0.0.0', port=5000, debug=True)