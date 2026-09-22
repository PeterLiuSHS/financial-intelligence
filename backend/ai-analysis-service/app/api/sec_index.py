from fastapi import APIRouter

from app.services.sec_index_service import index_risk_factors


router = APIRouter(
    prefix="/api/sec-index",
    tags=["SEC Index"]
)


@router.post("/{ticker}/risk-factors")
def index_company_risk_factors(
        ticker: str,
        fiscal_year: int
):
    return index_risk_factors(
        ticker=ticker,
        fiscal_year=fiscal_year
    )