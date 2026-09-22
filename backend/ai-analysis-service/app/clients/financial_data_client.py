import httpx

from app.core.config import settings

def get_company(ticker: str):
    url = f"{settings.financial_data_service_url}/api/companies/{ticker}"

    response = httpx.get(url, timeout=10.0)

    response.raise_for_status()

    return response.json()

def get_financial_statements(ticker: str):
    url = (
        f"{settings.financial_data_service_url}"
        f"/api/companies/{ticker}/financial-statements"
    )

    response = httpx.get(
        url,
        timeout=10.0
    )

    response.raise_for_status()

    return response.json()

def get_latest_10k_risk_factors(ticker: str) -> str:
    url = (
        f"{settings.financial_data_service_url}"
        f"/api/companies/{ticker}/filings/latest-10-k/risk-factors"
    )

    response = httpx.get(
        url, timeout=30.0
    )

    response.raise_for_status()

    return response.text