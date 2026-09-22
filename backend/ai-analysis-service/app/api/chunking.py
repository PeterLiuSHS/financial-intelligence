from fastapi import APIRouter

from app.clients.financial_data_client import get_latest_10k_risk_factors
from app.services.chunking_service import chunk_text

router = APIRouter(
    prefix="/api/chunking",
    tags=["chunking"]
)

@router.get("/{ticker}/risk-factors")
def get_risk_factor_chunks(ticker: str):

    text = get_latest_10k_risk_factors(ticker)

    chunks = chunk_text(
        text = text,
        chunk_size = 2000
    )

    return {
        "ticker": ticker.upper(),
        "original_length": len(text),
        "chunk_count": len(chunks),
        "chunks": [
            {
                "chunk_index": index,
                "length": len(chunk),
                "test": chunk
            }
            for index, chunk in enumerate(chunks)
        ]
    }