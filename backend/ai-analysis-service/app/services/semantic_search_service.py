from app.services.embedding_service import generate_embedding
from app.repositories.vector_repository import search_similar
from app.repositories.vector_repository import search_sec_filing_chunks

def semantic_search(ticker: str, query: str, top_k: int = 3):
    query_embedding = generate_embedding(query)

    return search_similar(
        ticker=ticker,
        query_embedding=query_embedding,
        top_k=top_k,
    )

def search_sec_risk_factors(
        ticker: str,
        query: str,
        top_k: int = 5,
        fiscal_year: int | None = None
):
    query_embedding = generate_embedding(query)

    return search_sec_filing_chunks(
        ticker=ticker,
        query_embedding=query_embedding,
        top_k=top_k,
        fiscal_year=fiscal_year,
        form_type="10-K",
        section="RISK_FACTORS"
    )