"""产品服务模块，处理产品CRUD"""

from typing import Optional

from sqlalchemy.orm import Session

from app.core.exceptions import NotFoundException
from app.models.product import Product, RecommendRule
from app.schemas.product import ProductCreate, ProductUpdate, RecommendRuleCreate, RecommendRuleUpdate
from app.utils.pagination import paginate


class ProductService:
    """产品服务类"""

    @staticmethod
    def get_product_by_id(db: Session, product_id: int) -> Product:
        """根据ID获取产品

        Args:
            db: 数据库会话
            product_id: 产品ID

        Returns:
            产品对象

        Raises:
            NotFoundException: 产品不存在
        """
        product = db.query(Product).filter(Product.id == product_id).first()
        if not product:
            raise NotFoundException(message="产品不存在")
        return product

    @staticmethod
    def get_product_list(
        db: Session,
        page: int = 1,
        page_size: int = 10,
        category: Optional[str] = None,
        is_available: Optional[bool] = None,
        keyword: Optional[str] = None,
    ) -> dict:
        """获取产品列表（分页）

        Args:
            db: 数据库会话
            page: 页码
            page_size: 每页数量
            category: 分类筛选
            is_available: 是否上架筛选
            keyword: 搜索关键词

        Returns:
            分页结果字典
        """
        query = db.query(Product)

        if category:
            query = query.filter(Product.category == category)

        if is_available is not None:
            query = query.filter(Product.is_available == is_available)

        if keyword:
            query = query.filter(
                (Product.name.like(f"%{keyword}%"))
                | (Product.brand.like(f"%{keyword}%"))
                | (Product.description.like(f"%{keyword}%"))
            )

        query = query.order_by(Product.created_at.desc())

        return paginate(query, page, page_size)

    @staticmethod
    def create_product(db: Session, product_data: ProductCreate) -> Product:
        """创建产品

        Args:
            db: 数据库会话
            product_data: 产品创建数据

        Returns:
            创建的产品对象
        """
        product = Product(**product_data.model_dump())
        db.add(product)
        db.commit()
        db.refresh(product)
        return product

    @staticmethod
    def update_product(db: Session, product_id: int, product_data: ProductUpdate) -> Product:
        """更新产品

        Args:
            db: 数据库会话
            product_id: 产品ID
            product_data: 产品更新数据

        Returns:
            更新后的产品对象

        Raises:
            NotFoundException: 产品不存在
        """
        product = ProductService.get_product_by_id(db, product_id)

        update_fields = product_data.model_dump(exclude_unset=True)
        for field, value in update_fields.items():
            setattr(product, field, value)

        db.commit()
        db.refresh(product)
        return product

    @staticmethod
    def delete_product(db: Session, product_id: int) -> None:
        """删除产品

        Args:
            db: 数据库会话
            product_id: 产品ID

        Raises:
            NotFoundException: 产品不存在
        """
        product = ProductService.get_product_by_id(db, product_id)
        db.delete(product)
        db.commit()

    # ==================== 推荐规则 ====================

    @staticmethod
    def get_recommend_rule_by_id(db: Session, rule_id: int) -> RecommendRule:
        """根据ID获取推荐规则"""
        rule = db.query(RecommendRule).filter(RecommendRule.id == rule_id).first()
        if not rule:
            raise NotFoundException(message="推荐规则不存在")
        return rule

    @staticmethod
    def get_recommend_rule_list(
        db: Session,
        page: int = 1,
        page_size: int = 10,
        rule_type: Optional[str] = None,
        is_active: Optional[bool] = None,
    ) -> dict:
        """获取推荐规则列表"""
        query = db.query(RecommendRule)

        if rule_type:
            query = query.filter(RecommendRule.rule_type == rule_type)

        if is_active is not None:
            query = query.filter(RecommendRule.is_active == is_active)

        query = query.order_by(RecommendRule.priority.desc(), RecommendRule.created_at.desc())

        return paginate(query, page, page_size)

    @staticmethod
    def create_recommend_rule(
        db: Session, rule_data: RecommendRuleCreate, user_id: Optional[int] = None
    ) -> RecommendRule:
        """创建推荐规则"""
        rule = RecommendRule(
            **rule_data.model_dump(),
            created_by=user_id,
        )
        db.add(rule)
        db.commit()
        db.refresh(rule)
        return rule

    @staticmethod
    def update_recommend_rule(
        db: Session, rule_id: int, rule_data: RecommendRuleUpdate
    ) -> RecommendRule:
        """更新推荐规则"""
        rule = ProductService.get_recommend_rule_by_id(db, rule_id)

        update_fields = rule_data.model_dump(exclude_unset=True)
        for field, value in update_fields.items():
            setattr(rule, field, value)

        db.commit()
        db.refresh(rule)
        return rule

    @staticmethod
    def delete_recommend_rule(db: Session, rule_id: int) -> None:
        """删除推荐规则"""
        rule = ProductService.get_recommend_rule_by_id(db, rule_id)
        db.delete(rule)
        db.commit()
