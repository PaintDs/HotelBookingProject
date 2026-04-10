from pydantic import BaseModel
from typing import Optional
from pydantic import BaseModel, EmailStr

# Khuôn cho đăng nhập
class UserLogin(BaseModel):
    email: str
    password: str

# Khuôn cho danh sách khách sạn - ĐÃ CẬP NHẬT PHÒNG THỦ
class HotelResponse(BaseModel):
    id: int
    name: str
    # Sử dụng Optional hoặc | None để tránh lỗi 500 nếu DB có dòng trống địa chỉ
    address: Optional[str] = "Chưa cập nhật địa chỉ" 
    price_per_night: float
    description: Optional[str] = None
    image_url: Optional[str] = None
    lat: Optional[float] = None
    lng: Optional[float] = None

    class Config:
        from_attributes = True
        # 1. Khung nhận data khi Đăng ký
class UserCreate(BaseModel):
    full_name: str
    email: EmailStr
    password: str

# 2. Khung nhận data khi Đăng nhập
class UserLogin(BaseModel):
    email: EmailStr
    password: str

# 3. Khung trả về Token
class TokenInfo(BaseModel):
    access_token: str
    token_type: str
    full_name: str