from pydantic import BaseModel

class EmbeddingRequest(BaseModel):
    text: str

class EmbeddingResponse(BaseModel):
    dimentions: int
    embedding: list[float]