from typing import Literal

from pydantic import BaseModel

class ClaimVerification(BaseModel):
    claim: str
    status: Literal[
        "SUPPORTED",
        "PARTIALLY_SUPPORTED",
        "UNSUPPORTED"
    ]
    reasoning: str

class EvidenceVerificationResult(BaseModel):
    status: list[ClaimVerification]