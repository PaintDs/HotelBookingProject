from pydantic import BaseModel, EmailStr
from typing import Optional

# ==========================================
# 1. KHUÔN CHO NGƯỜI DÙNG (USER)
# ==========================================
class UserCreate(BaseModel):
    full_name: str
    email: EmailStr
    password: str

class UserLogin(BaseModel):
    email: str
    password: str

class TokenInfo(BaseModel):
    access_token: str
    token_type: str
    full_name: str

# ==========================================
# 2. KHUÔN CHO KHÁCH SẠN (HOTEL)
# ==========================================
class HotelResponse(BaseModel):
    id: int
    name: str
    address: Optional[str] = "Chưa cập nhật địa chỉ" 
    price_per_night: float
    description: Optional[str] = None
    image_url: Optional[str] = None
    lat: Optional[float] = None
    lng: Optional[float] = None

    class Config:
        from_attributes = True

# ==========================================
# 3. KHUÔN CHO LỊCH SỬ ĐẶT PHÒNG (BOOKING) - QUAN TRỌNG
# ==========================================
class BookingResponse(BaseModel):
    id: int
    hotel_name: str
    customer_name: str
    cccd: str
    total_price: float
    status: str  # <--- BẮT BUỘC PHẢI CÓ DÒNG NÀY ĐỂ FIX LỖI

    class Config:
        from_attributes = True

# Khuôn nhận dữ liệu khi gọi API tạo QR thanh toán
class PaymentRequest(BaseModel):
    booking_id: int
    amount: int