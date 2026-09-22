class AIServiceException(Exception):
    def __init__(
            self,
            message: str,
            status_code: int = 503
    ):
        self.message = message
        self.status_code = status_code
        super().__init__(message)

class AIServiceBusyException(AIServiceException):
    def __init__(self):
        super().__init__(
            message="AI service is temporarily busy. Please try again later.",
            status_code=503
        )

class AIRateLimitException(AIServiceException):
    def __init__(self):
        super().__init__(
            message="AI service reate limit exceeded. Please try again later.",
            status_code= 429
        )

class AIValidationException(AIServiceException):
    def __init__(self, message: str):
        super().__init__(
            message=message,
            status_code=502
        )