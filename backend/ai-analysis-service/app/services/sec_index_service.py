from app.clients.financial_data_client import get_latest_10k_risk_factors
from app.services.chunking_service import chunk_text
from app.services.embedding_service import generate_embedding
from app.repositories.vector_repository import save_sec_filing_chunk


def index_risk_factors(
        ticker: str,
        fiscal_year: int
) -> dict:

    # 1. Get clean Risk Factors text from Java service
    text = get_latest_10k_risk_factors(ticker)

    # 2. Split text into chunks
    chunks = chunk_text(
        text=text,
        chunk_size=2000
    )

    # 3. Generate and persist embedding for every chunk
    for chunk_index, chunk in enumerate(chunks):

        embedding = generate_embedding(chunk)

        save_sec_filing_chunk(
            ticker=ticker,
            fiscal_year=fiscal_year,
            form_type="10-K",
            section="RISK_FACTORS",
            chunk_index=chunk_index,
            document=chunk,
            embedding=embedding
        )

    return {
        "ticker": ticker.upper(),
        "fiscal_year": fiscal_year,
        "form_type": "10-K",
        "section": "RISK_FACTORS",
        "indexed_chunks": len(chunks)
    }