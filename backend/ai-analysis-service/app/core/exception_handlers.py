from fastapi import Request
from fastapi.responses import JSONResponse

from app.exceptions.ai_exceptions import AIServiceException


async def ai_service_exception_handler(
        request: Request,
        exc: AIServiceException
):
    return JSONResponse(
        status_code=exc.status_code,
        content={
            "error": "AI_SERVICE_ERROR",
            "message": exc.message,
            "path": request.url.path
        }
    )