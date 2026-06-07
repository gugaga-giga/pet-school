"""全局异常处理中间件，捕获所有异常并返回统一格式响应"""

import traceback

from fastapi import Request
from fastapi.responses import JSONResponse
from loguru import logger
from starlette.middleware.base import BaseHTTPMiddleware, RequestResponseEndpoint

from app.core.exceptions import AppException
from app.utils.response import error_response


class ErrorHandlerMiddleware(BaseHTTPMiddleware):
    """全局异常处理中间件

    捕获所有AppException及其子类异常，返回统一格式的JSON响应。
    对于未预期的异常，返回500错误并记录详细日志。
    """

    async def dispatch(self, request: Request, call_next: RequestResponseEndpoint) -> JSONResponse:
        try:
            response = await call_next(request)
            return response  # type: ignore
        except AppException as exc:
            # 业务异常，返回对应的错误码和消息
            logger.warning(
                f"业务异常 [{exc.code}]: {exc.message} | 详情: {exc.detail} | 路径: {request.url.path}"
            )
            return JSONResponse(
                status_code=exc.code,
                content=error_response(code=exc.code, message=exc.message),
            )
        except Exception as exc:
            # 未预期异常，记录详细日志并返回500
            logger.error(
                f"未预期异常 | 路径: {request.url.path} | 异常: {str(exc)}\n"
                f"{traceback.format_exc()}"
            )
            return JSONResponse(
                status_code=500,
                content=error_response(code=500, message="服务器内部错误"),
            )
