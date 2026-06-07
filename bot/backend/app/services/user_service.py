"""用户服务模块，处理用户CRUD和分页"""

from typing import Optional

from sqlalchemy.orm import Session

from app.core.exceptions import BadRequestException, NotFoundException
from app.core.security import get_password_hash
from app.models.user import User
from app.schemas.user import UserCreate, UserUpdate
from app.utils.pagination import paginate


class UserService:
    """用户服务类"""

    @staticmethod
    def get_user_by_id(db: Session, user_id: int) -> User:
        """根据ID获取用户

        Args:
            db: 数据库会话
            user_id: 用户ID

        Returns:
            用户对象

        Raises:
            NotFoundException: 用户不存在
        """
        user = db.query(User).filter(User.id == user_id).first()
        if not user:
            raise NotFoundException(message="用户不存在")
        return user

    @staticmethod
    def get_user_list(
        db: Session,
        page: int = 1,
        page_size: int = 10,
        keyword: Optional[str] = None,
        is_active: Optional[bool] = None,
    ) -> dict:
        """获取用户列表（分页）

        Args:
            db: 数据库会话
            page: 页码
            page_size: 每页数量
            keyword: 搜索关键词（用户名/昵称/邮箱）
            is_active: 是否激活筛选

        Returns:
            分页结果字典
        """
        query = db.query(User)

        # 关键词搜索
        if keyword:
            query = query.filter(
                (User.username.like(f"%{keyword}%"))
                | (User.nickname.like(f"%{keyword}%"))
                | (User.email.like(f"%{keyword}%"))
            )

        # 状态筛选
        if is_active is not None:
            query = query.filter(User.is_active == is_active)

        # 按创建时间倒序
        query = query.order_by(User.created_at.desc())

        return paginate(query, page, page_size)

    @staticmethod
    def create_user(db: Session, user_data: UserCreate) -> User:
        """创建用户

        Args:
            db: 数据库会话
            user_data: 用户创建数据

        Returns:
            创建的用户对象

        Raises:
            BadRequestException: 用户名已存在
        """
        # 检查用户名是否已存在
        existing = db.query(User).filter(User.username == user_data.username).first()
        if existing:
            raise BadRequestException(message="用户名已存在")

        # 创建用户
        user = User(
            username=user_data.username,
            hashed_password=get_password_hash(user_data.password),
            email=user_data.email,
            phone=user_data.phone,
            nickname=user_data.nickname,
            is_active=user_data.is_active,
        )
        db.add(user)
        db.commit()
        db.refresh(user)
        return user

    @staticmethod
    def update_user(db: Session, user_id: int, user_data: UserUpdate) -> User:
        """更新用户

        Args:
            db: 数据库会话
            user_id: 用户ID
            user_data: 用户更新数据

        Returns:
            更新后的用户对象

        Raises:
            NotFoundException: 用户不存在
        """
        user = UserService.get_user_by_id(db, user_id)

        # 更新字段
        update_fields = user_data.model_dump(exclude_unset=True)
        for field, value in update_fields.items():
            if field == "role_ids":
                continue  # 角色单独处理
            setattr(user, field, value)

        db.commit()
        db.refresh(user)
        return user

    @staticmethod
    def delete_user(db: Session, user_id: int) -> None:
        """删除用户

        Args:
            db: 数据库会话
            user_id: 用户ID

        Raises:
            NotFoundException: 用户不存在
        """
        user = UserService.get_user_by_id(db, user_id)
        db.delete(user)
        db.commit()

    @staticmethod
    def get_user_count(db: Session) -> int:
        """获取用户总数

        Args:
            db: 数据库会话

        Returns:
            用户总数
        """
        return db.query(User).count()
