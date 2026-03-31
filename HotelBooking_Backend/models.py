from sqlalchemy import Column, Integer, String, Float, Text
from database import Base

# Bảng lưu trữ thông tin tài khoản người dùng
class User(Base):
    __tablename__ = "users"

    id = Column(Integer, primary_key=True, index=True)
    username = Column(String(50), nullable=False)
    email = Column(String(100), unique=True, index=True, nullable=False)
    password = Column(String(255), nullable=False)
    role = Column(String(20), default="customer")


# Bảng lưu trữ thông tin danh sách khách sạn
class Hotel(Base):
    __tablename__ = "hotels"

    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(100), nullable=False)
    address = Column(String(255), nullable=False)
    
    # Cột giá phòng (Sửa thành price_per_night để khớp hoàn toàn với MySQL)
    price_per_night = Column(Float, nullable=False) 
    
    description = Column(Text)
    image_url = Column(String(255))
    
    # ==========================================
    # CÁC CỘT DỮ LIỆU TỌA ĐỘ GPS
    # ==========================================
    lat = Column(Float, nullable=True) # Vĩ độ
    lng = Column(Float, nullable=True) # Kinh độ


# Bảng lưu trữ thông tin lịch sử đặt phòng của khách hàng
class Booking(Base):
    __tablename__ = "bookings"

    id = Column(Integer, primary_key=True, index=True)
    hotel_id = Column(Integer)
    hotel_name = Column(String(100), nullable=False)
    customer_name = Column(String(100), nullable=False)
    cccd = Column(String(20), nullable=False)
    total_price = Column(Float, nullable=False)