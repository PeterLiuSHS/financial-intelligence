from typing import Literal, List

from pydantic import BaseModel

class RiskFactor(BaseModel):
    category: str
    severity: Literal["high", "medium", "low"]
    description: str

class FinancialRiskAnalysis(BaseModel):
    company: str
    ticker: str
    overall_risk: Literal["high", "medium", "low"]
    summary: str
    risk_factors: List[RiskFactor]

class RiskAnalysisRequest(BaseModel):
    company: str
    ticker: str
    context: str