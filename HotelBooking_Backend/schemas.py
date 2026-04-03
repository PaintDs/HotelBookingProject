from pydantic import BaseModel
from typing import Optional

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