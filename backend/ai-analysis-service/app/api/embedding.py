from fastapi import FastAPI, APIRouter

from app.schemas.embedding import EmbeddingRequest, EmbeddingResponse
from app.services.embedding_service import generate_embedding

router = APIRouter(
    prefix="/api/embedding",
    tags=["embedding"]
)

@router.post("", response_model=EmbeddingResponse)
def create_embedding(request: EmbeddingRequest):
    embedding = generate_embedding(request.text)

    return EmbeddingResponse(
        dimentions=len(embedding),
        embedding=embedding
    )
