from http.client import responses

from app.clients.gemini_client import client
from app.schemas.risk_analysis import FinancialRiskAnalysis


def generate_analysis(prompt: str) -> str:
    response = client.models.generate_content(
        model="gemini-3.6-flash",
        contents=prompt
    )

    return response.text


def generate_risk_analysis(
        company: str,
        ticker: str,
        context: str
) -> FinancialRiskAnalysis:
    prompt = f"""
    You are a financial risk analyst.

    Analyze the company using ONLY the information provided below.

    Company: {company}
    Ticker: {ticker}

    Context: {context}

    Identify the major financial or business risk.
    Do not invent facts that are not supported by the context.
    """

    response = client.models.generate_content(
        model="gemini-3.6-flash",
        contents=prompt,
        config={
            "response_mime_type": "application/json",
            "response_schema": FinancialRiskAnalysis,
        }
    )


    return response.parsed
