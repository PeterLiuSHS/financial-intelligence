from fastapi import APIRouter

from app.schemas.search import SearchRequest, SearchResponse
from app.services.semantic_search_service import (semantic_search, search_sec_risk_factors)

router = APIRouter(
    prefix="/api/search",
    tags=["Semantic Search"]
)

@router.post("", response_model=SearchResponse)
def search(request: SearchRequest):
    results = semantic_search(
        ticker=request.ticker,
        query=request.query,
        top_k=request.top_k
    )

    return SearchResponse(results=results)

@router.get("/sec/{ticker}/risk-factors")
def search_risk_factors(
        ticker: str,
        query: str,
        top_k: int = 5,
        fiscal_year: int | None = None
):
    return search_sec_risk_factors(
        ticker=ticker,
        query=query,
        top_k=top_k,
        fiscal_year=fiscal_year
    )