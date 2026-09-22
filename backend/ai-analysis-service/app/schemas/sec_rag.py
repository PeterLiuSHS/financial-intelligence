from pydantic import BaseModel

class SecRiskClaim(BaseModel):
    claim: str
    source_chunk_ids: list[int]

class SecRiskAnswer(BaseModel):
    summary: str
    claims: list[SecRiskClaim]

