from fastapi import APIRouter

from app.schemas.analysis import AnalysisResponse, AnalysisRequest
from app.schemas.risk_analysis import (
    RiskAnalysisRequest,
    FinancialRiskAnalysis
)
from app.services.ai_service import (generate_analysis, generate_risk_analysis)

router = APIRouter(
    prefix="/api/ai",
    tags=["AI Analysis"]
)

@router.post("/analyze", response_model=AnalysisResponse)
def analysis(request: AnalysisRequest):
    result = generate_analysis(request.prompt)

    return AnalysisResponse(result=result)

@router.post("/risk-analysis", response_model=FinancialRiskAnalysis)
def risk_analysis(request: RiskAnalysisRequest):
    return generate_risk_analysis(
        company=request.company,
        ticker=request.ticker,
        context=request.context
    )