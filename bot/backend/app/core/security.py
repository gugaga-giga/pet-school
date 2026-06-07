"""JWT令牌和密码加密工具模块"""

from datetime import datetime, timedelta, timezone
from typing import Any, Dict

from jose import JWTError, jwt
from passlib.context import CryptContext

from app.core.config import settings
from app.core.exceptions import UnauthorizedException

# 密码加密上下文
pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")


def create_access_token(data: Dict[str, Any]) -> str:
    """创建访问令牌

    Args:
        data: 要编码的数据字典，通常包含 sub(用户ID) 等字段

    Returns:
        编码后的JWT令牌字符串
    """
    to_encode = data.copy()
    expire = datetime.now(timezone.utc) + timedelta(minutes=settings.ACCESS_TOKEN_EXPIRE_MINUTES)
    to_encode.update({"exp": expire, "type": "access"})
    encoded_jwt: str = jwt.encode(to_encode, settings.SECRET_KEY, algorithm=settings.ALGORITHM)
    return encoded_jwt


def create_refresh_token(data: Dict[str, Any]) -> str:
    """创建刷新令牌

    Args:
        data: 要编码的数据字典，通常包含 sub(用户ID) 等字段

    Returns:
        编码后的JWT刷新令牌字符串
    """
    to_encode = data.copy()
    expire = datetime.now(timezone.utc) + timedelta(days=settings.REFRESH_TOKEN_EXPIRE_DAYS)
    to_encode.update({"exp": expire, "type": "refresh"})
    encoded_jwt: str = jwt.encode(to_encode, settings.SECRET_KEY, algorithm=settings.ALGORITHM)
    return encoded_jwt


def verify_token(token: str) -> Dict[str, Any]:
    """验证JWT令牌

    Args:
        token: JWT令牌字符串

    Returns:
        解码后的数据字典

    Raises:
        UnauthorizedException: 令牌无效或已过期
    """
    try:
        payload: Dict[str, Any] = jwt.decode(
            token, settings.SECRET_KEY, algorithms=[settings.ALGORITHM]
        )
        return payload
    except JWTError as e:
        raise UnauthorizedException(message="令牌无效或已过期", detail=str(e))


def get_password_hash(password: str) -> str:
    """生成密码哈希值

    Args:
        password: 明文密码

    Returns:
        哈希后的密码字符串
    """
    return pwd_context.hash(password)


def verify_password(plain_password: str, hashed_password: str) -> bool:
    """验证密码是否匹配

    Args:
        plain_password: 明文密码
        hashed_password: 哈希后的密码

    Returns:
        密码是否匹配
    """
    return pwd_context.verify(plain_password, hashed_password)
