from fastapi import APIRouter
from app.services.financial_document_service import build_financial_documents

from app.clients.financial_data_client import (
    get_company,
    get_financial_statements
)

router = APIRouter(
    prefix="/api/companies",
    tags=["Company"]
)

@router.get("/{ticker}")
def company(ticker: str):
    company_data = get_company(ticker)
    statements = get_financial_statements(ticker)

    return {
        "company": company_data,
        "financial_statements": statements
    }

@router.get("/{ticker}/documents")
def financial_doucuments(ticker: str):
    documents = build_financial_documents(ticker)

    return {
        "ticker": ticker.upper(),
        "document_count": len(documents),
        "documents": documents
    }
