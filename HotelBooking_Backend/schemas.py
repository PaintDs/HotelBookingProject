from pydantic import BaseModel
from typing import Optional

# Khuôn cho đăng nhập
class UserLogin(BaseModel):
    email: str
    password: str

# Khuôn cho danh sách khách sạn (ĐÂY LÀ PHẦN BẠN ĐANG THIẾU)
class HotelResponse(BaseModel):
    lat: float | None = None
    lng: float | None = None
    id: int
    name: str
    address: str
    price_per_night: float
    description: Optional[str] = None
    image_url: Optional[str] = None
    

    class Config:
        from_attributes = True