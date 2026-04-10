from fastapi import FastAPI, Depends, HTTPException, status
from sqlalchemy.orm import Session
from sqlalchemy import text
from pydantic import BaseModel
from math import cos, asin, sqrt, pi
from passlib.context import CryptContext
from datetime import datetime, timedelta, timezone
import jwt

import models, schemas 
from database import engine, get_db

# Tự động tạo bảng trong MySQL nếu chưa có
models.Base.metadata.create_all(bind=engine)

app = FastAPI(title="Hotel Booking API with Auth")

# ==========================================
# 1. CẤU HÌNH BẢO MẬT
# ==========================================
pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")
SECRET_KEY = "day_la_chia_khoa_bi_mat_cua_ban_khong_cho_ai_biet"
ALGORITHM = "HS256"

def get_password_hash(password):
    return pwd_context.hash(password)

def verify_password(plain_password, hashed_password):
    return pwd_context.verify(plain_password, hashed_password)

def create_access_token(data: dict):
    to_encode = data.copy()
    # Sử dụng timezone-aware datetime
    expire = datetime.now(timezone.utc) + timedelta(days=7)
    to_encode.update({"exp": expire})
    return jwt.encode(to_encode, SECRET_KEY, algorithm=ALGORITHM)

# ==========================================
# 2. CÁC HÀM HỖ TRỢ
# ==========================================
def calculate_distance(lat1, lon1, lat2, lon2):
    p = pi/180
    a = 0.5 - cos((lat2 - lat1) * p)/2 + cos(lat1 * p) * cos(lat2 * p) * (1 - cos((lon2 - lon1) * p)) / 2
    return 12742 * asin(sqrt(a)) 

# ==========================================
# 3. CÁC API VỀ TÀI KHOẢN (ACCOUNT)
# ==========================================
@app.post("/register")
def register_user(user: schemas.UserCreate, db: Session = Depends(get_db)):
    db_user = db.query(models.User).filter(models.User.email == user.email).first()
    if db_user:
        raise HTTPException(status_code=400, detail="Email này đã được đăng ký!")
    
    new_user = models.User(
        full_name=user.full_name, 
        email=user.email, 
        hashed_password=get_password_hash(user.password)
    )
    db.add(new_user)
    db.commit()
    db.refresh(new_user)
    return {"message": "Đăng ký thành công!", "user_email": new_user.email}

@app.post("/login", response_model=schemas.TokenInfo)
def login_user(user_data: schemas.UserLogin, db: Session = Depends(get_db)):
    user = db.query(models.User).filter(models.User.email == user_data.email).first()
    if not user or not verify_password(user_data.password, user.hashed_password):
        raise HTTPException(status_code=401, detail="Sai email hoặc mật khẩu!")
    
    access_token = create_access_token(data={"user_id": user.id, "sub": user.email})
    return {
        "access_token": access_token, 
        "token_type": "bearer",
        "full_name": user.full_name
    }

# ==========================================
# 4. CÁC API VỀ KHÁCH SẠN VÀ ĐẶT PHÒNG
# ==========================================
@app.get("/")
def root():
    return {"message": "Chào mừng bạn đến với API Đặt phòng Khách sạn!"}

@app.get("/hotels", response_model=list[schemas.HotelResponse])
def get_hotels(db: Session = Depends(get_db)):
    return db.query(models.Hotel).all()
@app.get("/hotels/nearby")
def get_nearby_hotels(my_lat: float, my_lng: float, db: Session = Depends(get_db)):
    all_hotels = db.query(models.Hotel).all()
    nearby_list = []
    for h in all_hotels:
        if h.lat is not None and h.lng is not None:
            dist = calculate_distance(my_lat, my_lng, h.lat, h.lng)
            nearby_list.append({
                "id": h.id, "name": h.name, "address": h.address,
                "description": getattr(h, 'description', ''),
                "price_per_night": h.price_per_night,  
                "image_url": getattr(h, 'image_url', ''),
                "lat": h.lat, "lng": h.lng, "distance": round(dist, 2)
            })
    nearby_list.sort(key=lambda x: x['distance'])
    return nearby_list

# MODEL NHẬN DỮ LIỆU TỪ ANDROID
class BookingCreate(BaseModel):
    hotel_id: int
    hotel_name: str
    customer_name: str
    cccd: str
    total_price: float
    user_email: str 

@app.post("/book")
def create_booking(booking: BookingCreate, db: Session = Depends(get_db)):
    # LOG DEBUG: Xem dữ liệu Android gửi lên thực tế là gì
    print(f"--- ĐẶT PHÒNG MỚI ---")
    print(f"Email: {booking.user_email} | Khách: {booking.customer_name} | KS: {booking.hotel_name}")
    
    try:
        # Sử dụng models.Booking trực tiếp thay vì SQL thuần để tránh lỗi NULL
        new_booking = models.Booking(
            hotel_id=booking.hotel_id,
            hotel_name=booking.hotel_name,
            customer_name=booking.customer_name,
            cccd=booking.cccd,
            total_price=booking.total_price,
            user_email=booking.user_email
        )
        db.add(new_booking)
        db.commit()
        return {"status": "success", "message": "Đặt phòng thành công!"}
    except Exception as e:
        print(f"LỖI DATABASE: {str(e)}")
        db.rollback()
        return {"status": "error", "message": "Lỗi hệ thống khi lưu đặt phòng"}

@app.get("/bookings")
def get_bookings(user_email: str, db: Session = Depends(get_db)):
    # Lọc chính xác theo email, xóa khoảng trắng thừa
    email_clean = user_email.strip()
    print(f"--- LẤY LỊCH SỬ CHO: {email_clean} ---")
    
    bookings = db.query(models.Booking).filter(models.Booking.user_email == email_clean).all()
    return bookings
from dotenv import load_dotenv
import os

load_dotenv() # Tải các biến từ file .env vào hệ thống
SECRET_KEY = os.getenv("SECRET_KEY")
SQLALCHEMY_DATABASE_URL = os.getenv("DB_URL")