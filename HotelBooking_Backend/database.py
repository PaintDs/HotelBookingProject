from sqlalchemy import create_engine
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker

# 1. Khai báo địa chỉ kết nối (hotel_db là tên database bạn vừa tạo)
# Cấu trúc: mysql+mysqlconnector://user:password@host/database_name
SQLALCHEMY_DATABASE_URL = "mysql+mysqlconnector://root@localhost/hotel_db"

# 2. Tạo Engine để thực hiện kết nối
engine = create_engine(SQLALCHEMY_DATABASE_URL)

# 3. Tạo Session để làm việc với dữ liệu
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

# 4. Lớp nền để định nghĩa các bảng dữ liệu
Base = declarative_base()

# Hàm bổ trợ để lấy session làm việc
def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()

print("Kết nối Database thành công!")