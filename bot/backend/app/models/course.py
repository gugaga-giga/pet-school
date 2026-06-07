"""课程、订单 ORM 模型"""

from sqlalchemy import BigInteger, Column, DateTime, Integer, String, Text, JSON, Numeric, func
from sqlalchemy.orm import relationship

from app.core.database import Base


class Course(Base):
    """课程模型，对应 courses 表"""

    __tablename__ = "courses"

    id = Column(BigInteger, primary_key=True, autoincrement=True, comment="课程ID")
    name = Column(String(128), nullable=False, comment="课程名称")
    category = Column(String(64), nullable=False, comment="课程分类: obedience/agility/behavior/puppy/advanced")
    description = Column(Text, nullable=True, comment="课程描述")
    price = Column(Numeric(10, 2), default=0, comment="价格")
    duration = Column(String(64), nullable=True, comment="课时")
    max_students = Column(Integer, default=10, comment="最大学员数")
    current_students = Column(Integer, default=0, comment="当前学员数")
    trainer = Column(String(64), nullable=True, comment="训练师")
    difficulty = Column(String(32), default="beginner", comment="难度: beginner/intermediate/advanced")
    status = Column(Integer, default=1, comment="状态: 1上架 0下架")
    is_deleted = Column(Integer, default=0, comment="是否删除")
    created_at = Column(DateTime, default=func.now(), comment="创建时间")
    updated_at = Column(DateTime, default=func.now(), onupdate=func.now(), comment="更新时间")

    def __repr__(self) -> str:
        return f"<Course(id={self.id}, name='{self.name}')>"


class Order(Base):
    """订单模型，对应 orders 表"""

    __tablename__ = "orders"

    id = Column(BigInteger, primary_key=True, autoincrement=True, comment="订单ID")
    user_id = Column(BigInteger, nullable=False, index=True, comment="用户ID")
    course_id = Column(BigInteger, nullable=True, comment="课程ID")
    product_id = Column(BigInteger, nullable=True, comment="产品ID")
    order_no = Column(String(64), nullable=False, comment="订单号")
    amount = Column(Numeric(10, 2), default=0, comment="金额")
    status = Column(String(32), default="pending", comment="状态: pending/paid/completed/cancelled/refunded")
    payment_method = Column(String(32), nullable=True, comment="支付方式")
    remark = Column(Text, nullable=True, comment="备注")
    is_deleted = Column(Integer, default=0, comment="是否删除")
    created_at = Column(DateTime, default=func.now(), comment="创建时间")
    updated_at = Column(DateTime, default=func.now(), onupdate=func.now(), comment="更新时间")

    def __repr__(self) -> str:
        return f"<Order(id={self.id}, order_no='{self.order_no}')>"
