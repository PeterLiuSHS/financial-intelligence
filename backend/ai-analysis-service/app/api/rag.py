from fastapi import FastAPI, APIRouter

from app.schemas.rag import RagRequest, RagResponse
from app.services.rag_service import answer_question
from app.services.rag_service import answer_sec_risk_question

router = APIRouter(
    prefix="/api/rag",
    tags=["Rag"]
)

@router.post("", response_model=RagResponse)
def rag(request: RagRequest):
    return answer_question(
        ticker=request.ticker,
        question=request.question,
        top_k=request.top_k
    )

@router.get("/sec/{ticker}/risk-factors")
def ask_sec_risk_question(
        ticker: str,
        question: str,
        fiscal_year: int | None = None,
        top_k: int = 4
):
    return answer_sec_risk_question(
        ticker=ticker,
        question=question,
        fiscal_year=fiscal_year,
        top_k=top_k
    )