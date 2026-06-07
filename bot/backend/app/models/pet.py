"""宠物 ORM 模型 - 匹配数据库表结构"""

from sqlalchemy import BigInteger, Column, DateTime, Integer, String, Numeric, Date, func
from sqlalchemy.orm import relationship

from app.core.database import Base


class Pet(Base):
    """宠物模型，对应 pets 表"""

    __tablename__ = "pets"

    id = Column(BigInteger, primary_key=True, autoincrement=True, comment="宠物ID")
    user_id = Column(BigInteger, nullable=False, index=True, comment="用户ID")
    name = Column(String(64), nullable=False, comment="宠物昵称")
    type = Column(String(32), nullable=False, comment="宠物类型: cat/dog/bird/rabbit/fish/hamster/other")
    breed = Column(String(64), nullable=True, comment="品种")
    birth_date = Column(Date, nullable=True, comment="出生日期")
    age = Column(Integer, nullable=True, comment="年龄")
    weight = Column(Numeric(5, 2), nullable=True, comment="体重(kg)")
    gender = Column(Integer, nullable=True, comment="性别: 1公 2母 0未知")
    is_neutered = Column(Integer, default=0, comment="是否绝育: 0否 1是")
    health_status = Column(String(32), default="healthy", comment="健康状况: healthy/sick/recovering")
    vaccine_status = Column(String(32), default="unknown", comment="疫苗状态: unknown/partial/complete")
    is_deleted = Column(Integer, default=0, comment="是否删除")
    created_at = Column(DateTime, default=func.now(), comment="创建时间")
    updated_at = Column(DateTime, default=func.now(), onupdate=func.now(), comment="更新时间")

    def __repr__(self) -> str:
        return f"<Pet(id={self.id}, name='{self.name}', type='{self.type}')>"
