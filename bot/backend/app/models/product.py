"""产品、推荐规则 ORM 模型 - 匹配数据库表结构"""

from sqlalchemy import BigInteger, Column, DateTime, Integer, String, Text, JSON, Numeric, func

from app.core.database import Base


class Product(Base):
    """产品模型，对应 products 表"""

    __tablename__ = "products"

    id = Column(BigInteger, primary_key=True, autoincrement=True, comment="产品ID")
    name = Column(String(128), nullable=False, comment="产品名称")
    category = Column(String(64), nullable=False, comment="产品分类")
    description = Column(Text, nullable=True, comment="产品描述")
    price = Column(Numeric(10, 2), default=0, comment="价格")
    features = Column(JSON, nullable=True, comment="特性(JSON)")
    specifications = Column(JSON, nullable=True, comment="规格参数(JSON)")
    image_url = Column(String(512), nullable=True, comment="主图URL")
    status = Column(Integer, default=1, comment="状态: 1上架 0下架")
    is_deleted = Column(Integer, default=0, comment="是否删除")
    created_at = Column(DateTime, default=func.now(), comment="创建时间")
    updated_at = Column(DateTime, default=func.now(), onupdate=func.now(), comment="更新时间")

    def __repr__(self) -> str:
        return f"<Product(id={self.id}, name='{self.name}')>"


class RecommendRule(Base):
    """推荐规则模型，对应 recommend_rules 表"""

    __tablename__ = "recommend_rules"

    id = Column(BigInteger, primary_key=True, autoincrement=True, comment="规则ID")
    name = Column(String(128), nullable=False, comment="规则名称")
    intent_type = Column(String(64), nullable=False, comment="意图类型")
    keywords = Column(JSON, nullable=True, comment="关键词列表(JSON)")
    product_ids = Column(JSON, nullable=True, comment="推荐产品ID列表(JSON)")
    priority = Column(Integer, default=0, comment="优先级")
    status = Column(Integer, default=1, comment="状态: 1启用 0禁用")
    is_deleted = Column(Integer, default=0, comment="是否删除")
    created_at = Column(DateTime, default=func.now(), comment="创建时间")
    updated_at = Column(DateTime, default=func.now(), onupdate=func.now(), comment="更新时间")

    def __repr__(self) -> str:
        return f"<RecommendRule(id={self.id}, name='{self.name}')>"
