"""自定义异常模块，定义应用中使用的所有业务异常"""


class AppException(Exception):
    """应用基础异常类

    Attributes:
        code: 错误码
        message: 错误消息
        detail: 错误详情
    """

    def __init__(self, code: int = 500, message: str = "服务器内部错误", detail: str | None = None):
        self.code = code
        self.message = message
        self.detail = detail
        super().__init__(self.message)


class NotFoundException(AppException):
    """资源未找到异常"""

    def __init__(self, message: str = "资源未找到", detail: str | None = None):
        super().__init__(code=404, message=message, detail=detail)


class UnauthorizedException(AppException):
    """未授权异常"""

    def __init__(self, message: str = "未授权", detail: str | None = None):
        super().__init__(code=401, message=message, detail=detail)


class ForbiddenException(AppException):
    """禁止访问异常"""

    def __init__(self, message: str = "禁止访问", detail: str | None = None):
        super().__init__(code=403, message=message, detail=detail)


class BadRequestException(AppException):
    """请求参数错误异常"""

    def __init__(self, message: str = "请求参数错误", detail: str | None = None):
        super().__init__(code=400, message=message, detail=detail)


class FileUploadException(AppException):
    """文件上传异常"""

    def __init__(self, message: str = "文件上传失败", detail: str | None = None):
        super().__init__(code=400, message=message, detail=detail)
