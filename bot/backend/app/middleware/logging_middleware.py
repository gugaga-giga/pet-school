"""请求日志中间件，记录请求方法、路径、耗时"""

import time

from fastapi import Request
from loguru import logger
from starlette.middleware.base import BaseHTTPMiddleware, RequestResponseEndpoint


class LoggingMiddleware(BaseHTTPMiddleware):
    """请求日志中间件

    记录每个请求的方法、路径、查询参数和响应耗时。
    """

    async def dispatch(self, request: Request, call_next: RequestResponseEndpoint):
        # 记录请求开始时间
        start_time = time.time()

        # 构建请求信息
        method = request.method
        path = request.url.path
        query = str(request.query_params) if request.query_params else ""

        logger.info(f"请求开始 | {method} {path}" + (f"?{query}" if query else ""))

        # 调用下一个中间件或路由处理
        response = await call_next(request)

        # 计算耗时
        duration = time.time() - start_time
        duration_ms = round(duration * 1000, 2)

        # 记录响应信息
        status_code = response.status_code
        log_level = "INFO" if status_code < 400 else "WARNING" if status_code < 500 else "ERROR"
        logger.log(
            log_level,
            f"请求完成 | {method} {path} | 状态码: {status_code} | 耗时: {duration_ms}ms",
        )

        return response
