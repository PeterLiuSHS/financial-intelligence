from pydantic import BaseModel

class RagRequest(BaseModel):
    ticker: str
    question: str
    top_k: int=3

class RagResponse(BaseModel):
    answer: str
    sources: list[str]