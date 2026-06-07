"""认证路由模块"""

from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app.core.database import get_db
from app.core.dependencies import get_current_active_user
from app.models.user import User
from app.schemas.auth import LoginRequest, LoginResponse, RefreshTokenRequest, RegisterResponse, TokenResponse
from app.services.auth_service import AuthService
from app.utils.response import success_response

router = APIRouter(prefix="/auth", tags=["认证"])


@router.post("/login", summary="用户登录")
def login(request: LoginRequest, db: Session = Depends(get_db)) -> dict:
    """用户登录接口"""
    result = AuthService.login(db, request.username, request.password)
    return success_response(
        data=LoginResponse(
            access_token=result.access_token,
            refresh_token=result.refresh_token,
            token_type=result.token_type,
            expires_in=result.expires_in,
        ).model_dump()
    )


@router.post("/register", summary="用户注册")
def register(request: dict, db: Session = Depends(get_db)) -> dict:
    """用户注册接口"""
    from app.schemas.auth import RegisterRequest

    register_data = RegisterRequest(**request)
    result = AuthService.register(db, register_data.model_dump())
    return success_response(
        data=RegisterResponse(
            id=result.id,
            username=result.username,
            email=result.email,
            created_at=result.created_at,
        ).model_dump(),
        message="注册成功",
    )


@router.post("/refresh", summary="刷新令牌")
def refresh_token(request: RefreshTokenRequest, db: Session = Depends(get_db)) -> dict:
    """刷新令牌接口"""
    result = AuthService.refresh_token(db, request.refresh_token)
    return success_response(
        data=TokenResponse(
            access_token=result.access_token,
            refresh_token=result.refresh_token,
            token_type=result.token_type,
            expires_in=result.expires_in,
        )
    )


@router.get("/profile", summary="获取当前用户信息")
def get_profile(current_user: User = Depends(get_current_active_user)) -> dict:
    """获取当前登录用户的详细信息"""
    # 获取用户角色和权限
    roles = [role.code for role in current_user.roles]
    permissions = []
    for role in current_user.roles:
        for perm in role.permissions:
            permissions.append(perm.code)

    return success_response(data={
        "id": current_user.id,
        "username": current_user.username,
        "email": current_user.email,
        "nickname": current_user.nickname or current_user.username,
        "avatar": current_user.avatar,
        "phone": current_user.phone,
        "status": current_user.status,
        "roles": roles,
        "permissions": permissions,
        "created_at": str(current_user.created_at) if current_user.created_at else None,
    })
