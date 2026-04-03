from fastapi import FastAPI, Depends, HTTPException
from sqlalchemy.orm import Session
from sqlalchemy import text
from pydantic import BaseModel
from math import cos, asin, sqrt, pi # Thư viện toán học để tính khoảng cách

# Thêm các file cấu hình database của bạn
import models, schemas  
from database import engine, get_db

# Lệnh này giúp tự động tạo bảng trong MySQL nếu chưa có
models.Base.metadata.create_all(bind=engine)

# Khởi tạo App
app = FastAPI(title="Hotel Booking API")

# ==========================================
# CÁC HÀM HỖ TRỢ (HELPER FUNCTIONS)
# ==========================================

# Hàm tính khoảng cách giữa 2 tọa độ (Công thức Haversine - Trả về đơn vị Km)
def calculate_distance(lat1, lon1, lat2, lon2):
    p = pi/180
    a = 0.5 - cos((lat2 - lat1) * p)/2 + cos(lat1 * p) * cos(lat2 * p) * (1 - cos((lon2 - lon1) * p)) / 2
    return 12742 * asin(sqrt(a)) 

# ==========================================
# CÁC API CHÍNH DÀNH CHO ANDROID
# ==========================================

# 1. API Kiểm tra Server có sống không
@app.get("/")
def root():
    return {"message": "Chào mừng bạn đến với API Đặt phòng Khách sạn!"}


# 2. API Đăng nhập
@app.post("/login")
def login(user_data: schemas.UserLogin, db: Session = Depends(get_db)):
    # Tìm xem có ai xài cái email này trong hệ thống không
    user = db.query(models.User).filter(models.User.email == user_data.email).first()
    
    # Nếu không tìm thấy user, HOẶC tìm thấy nhưng mật khẩu sai -> Báo lỗi 400
    if not user or user.password != user_data.password:
        raise HTTPException(status_code=400, detail="Sai email hoặc mật khẩu!")
    
    # Mọi thứ đều đúng -> Chào mừng vào hệ thống
    return {
        "status": "success", 
        "message": "Đăng nhập thành công!",
        "user_info": {
            "username": user.username,
            "email": user.email,
            "role": user.role
        }
    }


# 3. API Lấy TOÀN BỘ danh sách khách sạn
@app.get("/hotels", response_model=list[schemas.HotelResponse])
def get_hotels(db: Session = Depends(get_db)):
    # Tương đương: SELECT * FROM hotels
    hotels = db.query(models.Hotel).all()
    return hotels


# 4. API Lấy khách sạn GẦN ĐÂY NHẤT (Sắp xếp theo khoảng cách)
@app.get("/hotels/nearby")
def get_nearby_hotels(my_lat: float, my_lng: float, db: Session = Depends(get_db)):
    # Lấy toàn bộ khách sạn lên trước
    all_hotels = db.query(models.Hotel).all()
    
    nearby_list = []
    
    for h in all_hotels:
        # Chỉ tính toán nếu khách sạn đó có nhập tọa độ (lat, lng) trong DB
        if h.lat is not None and h.lng is not None:
            # Tính khoảng cách từ S23 đến khách sạn
            dist = calculate_distance(my_lat, my_lng, h.lat, h.lng)
            
           # Gom dữ liệu lại thành dictionary, lấy giá phòng từ cột price_per_night
            hotel_data = {
                
                "id": h.id,
                "name": h.name,
                "address": h.address,
                "description": getattr(h, 'description', ''),
                "price_per_night": h.price_per_night,  
                "image_url": getattr(h, 'image_url', ''),
                "lat": h.lat,
                "lng": h.lng,
                "distance": round(dist, 2)
            }
            nearby_list.append(hotel_data)
    
    # Sắp xếp danh sách: Khách sạn nào khoảng cách nhỏ (gần nhất) thì đẩy lên đầu
    nearby_list.sort(key=lambda x: x['distance'])
    
    return nearby_list


# 5. Cấu trúc dữ liệu để nhận thông tin Đặt phòng từ S23
class Booking(BaseModel):
    hotel_id: int
    hotel_name: str
    customer_name: str
    cccd: str
    total_price: float

# 6. API Gửi yêu cầu Đặt phòng mới
@app.post("/book")
def create_booking(booking: Booking, db: Session = Depends(get_db)):
    try:
        # Dùng SQL thuần để chèn dữ liệu vào bảng bookings
        sql = text("INSERT INTO bookings (hotel_id, hotel_name, customer_name, cccd, total_price) VALUES (:h_id, :h_name, :c_name, :cccd, :t_price)")
        
        db.execute(sql, {
            "h_id": booking.hotel_id,
            "h_name": booking.hotel_name,
            "c_name": booking.customer_name,
            "cccd": booking.cccd,
            "t_price": booking.total_price
        })
        db.commit() # Lưu vào Database
        return {"status": "success", "message": "Đặt phòng thành công!"}
    
    except Exception as e:
        print("Lỗi Database:", str(e))
        return {"status": "error", "message": str(e)}


# 7. API Lấy danh sách Lịch sử đặt phòng
@app.get("/bookings")
def get_bookings(db: Session = Depends(get_db)):
    # Tương đương: SELECT * FROM bookings
    bookings = db.query(models.Booking).all()
    return bookings