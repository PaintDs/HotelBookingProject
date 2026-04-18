from sqlalchemy import Column, Integer, String, Float, Text
from database import Base

# ==========================================
# 1. Bảng lưu trữ thông tin tài khoản
# ==========================================
class User(Base):
    __tablename__ = "users"

    id = Column(Integer, primary_key=True, index=True)
    full_name = Column(String(100))
    email = Column(String(100), unique=True, index=True)
    hashed_password = Column(String(255))

# ==========================================
# 2. Bảng lưu trữ danh sách khách sạn
# ==========================================
class Hotel(Base):
    __tablename__ = "hotels"

    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(100), nullable=False)
    address = Column(String(255), nullable=False)
    price_per_night = Column(Float, nullable=False) 
    description = Column(Text)
    image_url = Column(String(255))
    lat = Column(Float, nullable=True) 
    lng = Column(Float, nullable=True) 

# ==========================================
# 3. Bảng lịch sử đặt phòng (TRỌNG TÂM LỖI Ở ĐÂY)
# ==========================================
class Booking(Base):
    __tablename__ = "bookings"

    id = Column(Integer, primary_key=True, index=True)
    hotel_id = Column(Integer)
    hotel_name = Column(String(100), nullable=False)
    customer_name = Column(String(100), nullable=False)
    cccd = Column(String(20), nullable=False)
    total_price = Column(Float, nullable=False)
    
    # FIX: Đảm bảo tên biến là user_email (viết thường, gạch dưới) 
    # để khớp 100% với @SerializedName("user_email") bên Android
    user_email = Column(String(255), nullable=True)