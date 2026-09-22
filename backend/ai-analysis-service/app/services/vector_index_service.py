from app.repositories.vector_repository import save_embedding
from app.services.embedding_service import generate_embedding
from app.services.financial_document_service import build_financial_documents

def index_company(ticker: str):
    ticker = ticker.upper()

    documents = build_financial_documents(ticker)

    for item in documents:
        document = item["document"]
        fiscal_year = item["fiscal_year"]

        embedding = generate_embedding(document)

        save_embedding(
            ticker=ticker,
            fiscal_year=fiscal_year,
            document=document,
            embedding=embedding
        )

    return {
        "ticker": ticker,
        "indexed_documents": len(documents)
    }
