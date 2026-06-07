"""认证服务模块，处理登录、注册、令牌刷新"""

from datetime import datetime, timezone

from sqlalchemy.orm import Session

from app.core.config import settings
from app.core.exceptions import BadRequestException, UnauthorizedException
from app.core.security import (
    create_access_token,
    create_refresh_token,
    get_password_hash,
    verify_password,
    verify_token,
)
from app.models.user import User
from app.schemas.auth import LoginResponse, RegisterResponse, TokenResponse


class AuthService:
    """认证服务类"""

    @staticmethod
    def login(db: Session, username: str, password: str) -> TokenResponse:
        """用户登录

        Args:
            db: 数据库会话
            username: 用户名
            password: 密码

        Returns:
            令牌响应

        Raises:
            UnauthorizedException: 用户名或密码错误
        """
        # 查找用户
        user = db.query(User).filter(User.username == username).first()
        if not user:
            raise UnauthorizedException(message="用户名或密码错误")

        # 验证密码
        if not verify_password(password, user.password_hash):
            raise UnauthorizedException(message="用户名或密码错误")

        # 检查用户状态
        if not user.is_active:
            raise UnauthorizedException(message="用户已被禁用")

        # 生成令牌
        token_data = {"sub": str(user.id), "username": user.username}
        access_token = create_access_token(token_data)
        refresh_token = create_refresh_token(token_data)

        return TokenResponse(
            access_token=access_token,
            refresh_token=refresh_token,
            token_type="bearer",
            expires_in=settings.ACCESS_TOKEN_EXPIRE_MINUTES * 60,
        )

    @staticmethod
    def register(db: Session, user_data: dict) -> RegisterResponse:
        """用户注册

        Args:
            db: 数据库会话
            user_data: 注册数据字典

        Returns:
            注册响应

        Raises:
            BadRequestException: 用户名已存在或密码不一致
        """
        # 验证密码一致性
        if user_data.get("password") != user_data.get("confirm_password"):
            raise BadRequestException(message="两次密码不一致")

        # 检查用户名是否已存在
        existing = db.query(User).filter(User.username == user_data["username"]).first()
        if existing:
            raise BadRequestException(message="用户名已存在")

        # 检查邮箱是否已存在
        if user_data.get("email"):
            existing = db.query(User).filter(User.email == user_data["email"]).first()
            if existing:
                raise BadRequestException(message="邮箱已被注册")

        # 创建用户
        user = User(
            username=user_data["username"],
            password_hash=get_password_hash(user_data["password"]),
            email=user_data.get("email"),
            phone=user_data.get("phone"),
            nickname=user_data.get("nickname"),
        )
        db.add(user)
        db.commit()
        db.refresh(user)

        return RegisterResponse(
            id=user.id,
            username=user.username,
            email=user.email,
            created_at=user.created_at,
        )

    @staticmethod
    def refresh_token(db: Session, refresh_token_str: str) -> TokenResponse:
        """刷新令牌

        Args:
            db: 数据库会话
            refresh_token_str: 刷新令牌

        Returns:
            新的令牌响应

        Raises:
            UnauthorizedException: 刷新令牌无效
        """
        # 验证刷新令牌
        payload = verify_token(refresh_token_str)
        if payload.get("type") != "refresh":
            raise UnauthorizedException(message="令牌类型错误")

        user_id = payload.get("sub")
        if not user_id:
            raise UnauthorizedException(message="令牌中缺少用户信息")

        # 检查用户是否存在且活跃
        user = db.query(User).filter(User.id == int(user_id)).first()
        if not user:
            raise UnauthorizedException(message="用户不存在")
        if not user.is_active:
            raise UnauthorizedException(message="用户已被禁用")

        # 生成新令牌
        token_data = {"sub": str(user.id), "username": user.username}
        new_access_token = create_access_token(token_data)
        new_refresh_token = create_refresh_token(token_data)

        return TokenResponse(
            access_token=new_access_token,
            refresh_token=new_refresh_token,
            token_type="bearer",
            expires_in=settings.ACCESS_TOKEN_EXPIRE_MINUTES * 60,
        )
