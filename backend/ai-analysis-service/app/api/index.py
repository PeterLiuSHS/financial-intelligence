from fastapi import FastAPI, APIRouter

from app.services.vector_index_service import index_company

router = APIRouter(
    prefix="/api/index",
    tags=["Vector Index"]
)

@router.post("/{ticker}")
def create_index(ticker: str):
    return index_company(ticker)