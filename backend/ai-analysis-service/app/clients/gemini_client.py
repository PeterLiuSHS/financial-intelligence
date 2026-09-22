import time

from google import genai
from google.genai import errors

from app.core.config import settings
from app.exceptions.ai_exceptions import (
    AIServiceBusyException,
    AIRateLimitException,
    AIServiceException
)

client = genai.Client(
    api_key=settings.gemini_api_key
)


def generate_content_with_retry(**kwargs):
    max_attempts = 3

    for attempt in range(max_attempts):

        try:
            return client.models.generate_content(**kwargs)

        except errors.ClientError as exc:

            if exc.code != 429:
                raise AIServiceException(
                    message="AI provider rejected the request.",
                    status_code=502
                ) from exc

            # Last attempt: stop retrying
            if attempt == max_attempts - 1:
                raise AIRateLimitException() from exc

        except errors.ServerError as exc:

            if exc.code != 503:
                raise AIServiceException(
                    message="AI provider is temporarily unavailable.",
                    status_code=503
                ) from exc

            # Last attempt: stop retrying
            if attempt == max_attempts - 1:
                raise AIServiceBusyException() from exc

        # Exponential backoff:
        # attempt 0 -> 1 second
        # attempt 1 -> 2 seconds
        delay = 2 ** attempt

        time.sleep(delay)

def embed_content_with_retry(**kwargs):
    max_attempts = 3

    for attempt in range(max_attempts):

        try:
            return client.models.embed_content(**kwargs)

        except errors.ClientError as exc:

            if exc.code != 429:
                raise AIServiceException(
                    message="AI provider rejected the embedding request.",
                    status_code=502
                ) from exc

            if attempt == max_attempts - 1:
                raise AIRateLimitException() from exc

        except errors.ServerError as exc:

            if exc.code != 503:
                raise AIServiceException(
                    message="AI embedding provider is temporarily unavailable.",
                    status_code=503
                ) from exc

            if attempt == max_attempts - 1:
                raise AIServiceBusyException() from exc

        delay = 2 ** attempt
        time.sleep(delay)