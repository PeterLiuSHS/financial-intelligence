from app.clients.gemini_client import embed_content_with_retry

def generate_embedding(text: str) -> list[float]:
    response = embed_content_with_retry(
        model="gemini-embedding-001",
        contents=text
    )

    return response.embeddings[0].values