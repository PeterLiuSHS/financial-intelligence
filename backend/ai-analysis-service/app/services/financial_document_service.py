from app.clients.financial_data_client import (get_company, get_financial_statements)

def build_financial_documents(ticker: str) -> list[str]:
    company = get_company(ticker)
    statements = get_financial_statements(ticker)

    company_name = company["name"]
    ticker = company["ticker"]

    documents = []

    for statement in statements:
        fiscal_year = statement["fiscalYear"]

        document = f"""
{company_name} ({ticker}) financial results for fiscal year {fiscal_year}.

Revenue: {statement.get("revenue")}
Gross profit: {statement.get("grossProfit")}
Operating income: {statement.get("operatingIncome")}
Net income: {statement.get("netIncome")}

Total assets: {statement.get("totalAssets")}
Total liabilities: {statement.get("totalLiabilities")}
Total debt: {statement.get("totalDebt")}
Cash and cash equivalents: {statement.get("cashAndCashEquivalents")}

Inventory: {statement.get("inventory")}
Accounts receivable: {statement.get("accountsReceivable")}
Current assets: {statement.get("currentAssets")}
Current liabilities: {statement.get("currentLiabilities")}

Operating cash flow: {statement.get("operatingCashFlow")}
Capital expenditure: {statement.get("capitalExpenditure")}
""".strip()

        documents.append({
            "ticker": ticker,
            "fiscal_year": fiscal_year,
            "document": document
        })

    return documents